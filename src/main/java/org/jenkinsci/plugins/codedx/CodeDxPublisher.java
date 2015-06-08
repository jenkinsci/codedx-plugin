/*
 * 
 * Copyright 2014 Applied Visions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 *  
 */

package org.jenkinsci.plugins.codedx;

import com.secdec.codedx.api.client.*;
import com.secdec.codedx.api.client.Job;
import com.secdec.codedx.api.client.Project;
import com.secdec.codedx.security.JenkinsSSLConnectionSocketFactoryFactory;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Extension;
import hudson.model.*;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import net.sf.json.JSONObject;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jenkinsci.plugins.codedx.model.CodeDxReportStatistics;
import org.jenkinsci.plugins.codedx.model.CodeDxGroupStatistics;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocketFactory;
import javax.servlet.ServletException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * Jenkins publisher that publishes project source, binaries, and
 * analysis tool output files to a CodeDx server.
 *
 * @author anthonyd
 */
public class CodeDxPublisher extends Recorder {

	private final String url;
	private final String key;
	private final String projectId;

	private final String sourceAndBinaryFiles;
	private final String toolOutputFiles;
	private final String excludedSourceAndBinaryFiles;

	private final AnalysisResultConfiguration analysisResultConfiguration;

	private final CodeDxClient client;

	private final String selfSignedCertificateFingerprint;

	/**
	 * @param url                          URL of the Code Dx server
	 * @param key                          API key of the Code Dx server
	 * @param projectId                    Code Dx project ID
	 * @param sourceAndBinaryFiles         Comma separated list of source/binary file Ant GLOB patterns
	 * @param toolOutputFiles              List of paths to tool output files
	 * @param excludedSourceAndBinaryFiles Comma separated list of source/binary file Ant GLOB patterns to exclude
	 * @param analysisResultConfiguration  Contains the fields applicable when the user chooses to have Jenkins wait for
	 *                                     analysis runs to complete.
	 */
	@DataBoundConstructor
	public CodeDxPublisher(final String url,
						   final String key,
						   final String projectId,
						   final String sourceAndBinaryFiles,
						   final String toolOutputFiles,
						   final String excludedSourceAndBinaryFiles,
						   final AnalysisResultConfiguration analysisResultConfiguration,
						   final String selfSignedCertificateFingerprint) {
		this.projectId = projectId;
		this.url = url;
		this.key = key;
		this.sourceAndBinaryFiles = sourceAndBinaryFiles;
		this.excludedSourceAndBinaryFiles = excludedSourceAndBinaryFiles;
		this.toolOutputFiles = toolOutputFiles;
		this.analysisResultConfiguration = analysisResultConfiguration;
		this.selfSignedCertificateFingerprint = selfSignedCertificateFingerprint;
		this.client = buildClient(url, key, selfSignedCertificateFingerprint);
	}

	public AnalysisResultConfiguration getAnalysisResultConfiguration() {
		return analysisResultConfiguration;
	}

	public String getProjectId() {
		return projectId;
	}

	public String getUrl() {

		return url;
	}

	public String getKey() {

		return key;
	}

	public String getSourceAndBinaryFiles() {
		return sourceAndBinaryFiles;
	}


	public String getToolOutputFiles() {
		return toolOutputFiles;
	}

	public String getExcludedSourceAndBinaryFiles() {
		return excludedSourceAndBinaryFiles;
	}

	public String getSelfSignedCertificateFingerprint() {
		return selfSignedCertificateFingerprint;
	}


	@Override
	public Action getProjectAction(AbstractProject<?, ?> project) {

		String latestUrl = null;

		if (projectId.length() != 0 && !projectId.equals("-1")) {

			latestUrl = client.buildLatestAnalysisRunUrl(Integer.parseInt(projectId));
		}

		return new CodeDxProjectAction(project, analysisResultConfiguration, latestUrl);
	}

	@Override
	public boolean perform(
			final AbstractBuild<?, ?> build,
			final Launcher launcher,
			final BuildListener listener) throws InterruptedException, IOException {

		final List<InputStream> toSend = new ArrayList<InputStream>();

		listener.getLogger().println("Starting Code Dx Publish");

		if (projectId.length() == 0 || projectId.equals("-1")) {

			listener.getLogger().println("No project has been selected");
			return true;
		}

		listener.getLogger().println("Code Dx Project ID: " + projectId);
		listener.getLogger().println("Code Dx URL: " + url);

		listener.getLogger().println("Creating source/binary zip...");

		FilePath sourceAndBinaryZip = Archiver.Archive(build.getWorkspace(),
				Util.commaSeparatedToArray(sourceAndBinaryFiles),
				Util.commaSeparatedToArray(excludedSourceAndBinaryFiles),
				"source", listener.getLogger());


		if (sourceAndBinaryZip != null) {

			try {

				listener.getLogger().println("Adding source/binary zip...");

				toSend.add(sourceAndBinaryZip.read());
			} catch (IOException e) {

				listener.getLogger().println("Failed to add source/binary zip.");
			}

		} else {

			listener.getLogger().println("No matching source/binary files.");
		}

		String[] files = Util.commaSeparatedToArray(toolOutputFiles);

		for (String file : files) {

			if (file.length() != 0) {

				FilePath path = build.getWorkspace().child(file);

				if (path.exists()) {

					try {

						listener.getLogger().println("Add tool output file " + path.getRemote() + " to request.");
						toSend.add(path.read());
					} catch (IOException e) {

						listener.getLogger().println("Failed to add tool output file: " + path);
					}
				}
			}
		}

		if (toSend.size() > 0) {

			final CodeDxClient repeatingClient = new CodeDxRepeatingClient(this.client, listener.getLogger());

			try {
				listener.getLogger().println("Sending analysis request");

				StartAnalysisResponse response = repeatingClient.startAnalysis(Integer.parseInt(projectId), toSend.toArray(new InputStream[0]));

				listener.getLogger().println("Analysis request succeeded");

				if (analysisResultConfiguration == null) {

					listener.getLogger().println("No need to wait for analysis to complete.  We are done here.");
					return true;
				}

				listener.getLogger().println("Waiting for analysis to complete");

				String status = null;

				do {

					Thread.sleep(3000);
					status = repeatingClient.getJobStatus(response.getJobId());

					listener.getLogger().println("The STATUS IS: " + status);

				} while (Job.QUEUED.equals(status) || Job.RUNNING.equals(status));

				if (Job.COMPLETED.equals(status)) {

					listener.getLogger().println("Analysis succeeded");

					listener.getLogger().println("Fetching severity counts");

					List<CountGroup> severityCounts = repeatingClient.getFindingsGroupedCounts(response.getRunId(), null, "severity");

					listener.getLogger().println("Got severity counts");

					listener.getLogger().println("Fetching status counts");

					Filter notAssignedFilter = new Filter();
					notAssignedFilter.setStatus(new String[]{
							Filter.STATUS_ESCALATED,
							Filter.STATUS_FALSE_POSITIVE,
							Filter.STATUS_FIXED,
							Filter.STATUS_GONE,
							Filter.STATUS_IGNORED,
							Filter.STATUS_NEW,
							Filter.STATUS_UNRESOLVED});

					List<CountGroup> statusCounts = repeatingClient.getFindingsGroupedCounts(response.getRunId(), notAssignedFilter, "status");

					listener.getLogger().println("Got status counts");

					Filter assignedFilter = new Filter();
					assignedFilter.setStatus(new String[]{Filter.STATUS_ASSIGNED});

					listener.getLogger().println("Fetching assigned count");

					//Since CodeDx splits assigned status into different statuses (one per user),
					//we need to get the total assigned count and add our own CountGroup.
					int assignedCount = repeatingClient.getFindingsCount(response.getRunId(), assignedFilter);

					if (assignedCount > 0) {

						CountGroup assignedGroup = new CountGroup();
						assignedGroup.setName("Assigned");
						assignedGroup.setCount(assignedCount);
						statusCounts.add(assignedGroup);
					}

					listener.getLogger().println("Got assigned count");


					Map<String, CodeDxReportStatistics> statMap = new HashMap<String, CodeDxReportStatistics>();

					statMap.put("severity", createStatistics(severityCounts));
					statMap.put("status", createStatistics(statusCounts));

					CodeDxResult result = new CodeDxResult(statMap, build);

					listener.getLogger().println("Adding CodeDx build action");
					build.addAction(new CodeDxBuildAction(build, result));

					AnalysisResultChecker checker = new AnalysisResultChecker(repeatingClient,
							analysisResultConfiguration.getFailureSeverity(),
							analysisResultConfiguration.getUnstableSeverity(),
							analysisResultConfiguration.isFailureOnlyNew(),
							analysisResultConfiguration.isUnstableOnlyNew(),
							response.getRunId(),
							listener.getLogger());
					build.setResult(checker.checkResult());

					return true;
				} else {
					listener.getLogger().println("Analysis status: " + status);
					return false;
				}

			} catch (NumberFormatException e) {

				listener.getLogger().println("Invalid project Id");

			} catch (CodeDxClientException e) {

				listener.getLogger().println("Fatal Error!");
				e.printStackTrace(listener.getLogger());

			} finally {

				sourceAndBinaryZip.delete();
			}
		} else {

			listener.getLogger().println("Nothing to send, this doesn't seem right! Please check your 'Code Dx > Source and Binary Files' configuration.");
		}

		return false;
	}

	public static CodeDxClient buildClient(String url, String key, String fingerprint) {
		CodeDxClient client = new CodeDxClient(url, key);
		try {
			if (fingerprint != null) {
				fingerprint = fingerprint.replaceAll("\\s", ":");
			}
			URL parsedUrl = new URL(url);
			SSLConnectionSocketFactory socketFactory = JenkinsSSLConnectionSocketFactoryFactory.getFactory(fingerprint, parsedUrl.getHost());
			HttpClientBuilder builder = HttpClientBuilder.create();
			builder.setSSLSocketFactory(socketFactory);
			client = new CodeDxClient(url, key, builder);
		} catch (MalformedURLException e) {

		} catch (GeneralSecurityException e) {

		}
		return client;
	}

	private String[] getUsers(Map<String, TriageStatus> assignedStatuses) {

		List<String> users = new ArrayList<String>();


		for (TriageStatus status : assignedStatuses.values()) {

			if (status.getType().equals(TriageStatus.TYPE_USER)) {
				users.add(status.getDisplay());
			}
		}

		return users.toArray(new String[0]);
	}

	private CodeDxReportStatistics createStatistics(List<CountGroup> countGroups) {

		List<CodeDxGroupStatistics> groupStatsList = new ArrayList<CodeDxGroupStatistics>();


		for (CountGroup group : countGroups) {

			CodeDxGroupStatistics stats = new CodeDxGroupStatistics(group.getName(), group.getCount());
			groupStatsList.add(stats);
		}

		return new CodeDxReportStatistics(groupStatsList);
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE; // NONE since this is not dependent on the last step
	}

	// Overridden for better type safety.
	// If your plugin doesn't really define any property on Descriptor,
	// you don't have to do this.
	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	/**
	 * Descriptor for {@link CodeDxPublisher}. Used as a singleton.
	 * The class is marked as public so that it can be accessed from views.
	 */
	@Extension // This indicates to Jenkins that this is an implementation of an extension point.
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		/**
		 * To persist global configuration information,
		 * simply store it in a field and call save().
		 *
		 * <p>
		 * If you don't want fields to be persisted, use <tt>transient</tt>.
		 */

		/**
		 * In order to load the persisted global configuration, you have to
		 * call load() in the constructor.
		 */
		public DescriptorImpl() {
			load();
		}

		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			// Indicates that this builder can be used with all kinds of project types
			return true;
		}

		/**
		 * This human readable name is used in the configuration screen.
		 */
		public String getDisplayName() {
			return "Publish to Code Dx";
		}

		public FormValidation doCheckProjectId(@QueryParameter final String value)
				throws IOException, ServletException {
			if (value.length() == 0)
				return FormValidation.error("Please set a project. If none are shown above, then be sure that system settings are configured correctly.");
			if (Integer.parseInt(value) == -1)
				return FormValidation.error("Failed to get available projects, please ensure systems settings are configured correctly.");

			return FormValidation.ok();
		}

		public FormValidation doCheckKey(@QueryParameter final String value)
				throws IOException, ServletException {

			if (value.length() == 0)
				return FormValidation.error("Please set a Key.");

			return FormValidation.ok();
		}

		public FormValidation doCheckUrl(@QueryParameter final String value, @QueryParameter final String selfSignedCertificateFingerprint)
				throws IOException, ServletException {

			CodeDxClient client = buildClient(value, "", selfSignedCertificateFingerprint);

			if (value.length() == 0)
				return FormValidation.error("Please set a URL.");

			try {
				new URL(value);

			} catch (MalformedURLException malformedURLException) {
				return FormValidation.error("Malformed URL");
			}

			if (value.toLowerCase().startsWith("http:")) {

				return FormValidation.warning("HTTP is considered insecure, it is recommended that you use HTTPS.");
			} else if (value.toLowerCase().startsWith("https:")) {
				try {
					client.getProjects();
				} catch (Exception e) {
					if (e instanceof SSLHandshakeException) {
						return FormValidation.warning("The SSL Certificate presented by the server is invalid. If this is expected, please input an SHA1 Fingerprint in the \"Advanced\" option");
					}
				}
				return FormValidation.ok();
			} else {

				return FormValidation.error("Invalid protocol, please use HTTPS or HTTP.");
			}
		}

		public FormValidation doCheckSelfSignedCertificateFingerprint(@QueryParameter final String value, @QueryParameter final String url) {
			if (url != null && ! url.isEmpty() && value != null && ! value.isEmpty()) {
				CodeDxClient client = buildClient(url, "", value);

				try {
					client.getProjects();
				} catch (Exception e) {
					if (e instanceof SSLHandshakeException) {
						return FormValidation.warning("The fingerprint doesn't match the fingerprint of the certifcate presented by the server");
					}
				}
			}
			return FormValidation.ok();
		}

		public FormValidation doCheckSourceAndBinaryFiles(@QueryParameter final String value, @QueryParameter final String toolOutputFiles, @AncestorInPath AbstractProject project) {

			if (value.length() == 0) {

				if (toolOutputFiles.length() == 0)
					return FormValidation.error("You must specify \"Tool Output Files\" and/or \"Source and Binary Files\"");
				else
					return FormValidation.warning("It is recommended that at least source files are provided to Code Dx.");
			}

			return Util.checkCSVGlobMatches(value, project.getSomeWorkspace());
		}

		public FormValidation doCheckExcludedSourceAndBinaryFiles(@QueryParameter final String value, @AncestorInPath AbstractProject project) {

			return Util.checkCSVGlobMatches(value, project.getSomeWorkspace());
		}

		public FormValidation doCheckToolOutputFiles(@QueryParameter final String value, @QueryParameter final String sourceAndBinaryFiles, @AncestorInPath AbstractProject project) {

			if (value.length() == 0 && sourceAndBinaryFiles.length() == 0) {

				return FormValidation.error("You must specify \"Tool Output Files\" and/or \"Source and Binary Files\"");
			}

			return Util.checkCSVFileMatches(value, project.getSomeWorkspace());
		}

		public ListBoxModel doFillProjectIdItems(@QueryParameter final String url, @QueryParameter final String selfSignedCertificateFingerprint, @QueryParameter final String key, @AncestorInPath AbstractProject project) {
			ListBoxModel listBox = new ListBoxModel();

			CodeDxClient client = buildClient(url, key, selfSignedCertificateFingerprint);



			try {
				final List<Project> projects = client.getProjects();

				Map<String, Boolean> duplicates = new HashMap<String, Boolean>();

				for (Project proj : projects) {

					if (!duplicates.containsKey(proj.getName())) {
						duplicates.put(proj.getName(), false);
					} else {

						duplicates.put(proj.getName(), true);
					}
				}
				for (Project proj : projects) {

					if (!duplicates.get(proj.getName())) {

						listBox.add(proj.getName(), Integer.toString(proj.getId()));
					} else {

						listBox.add(proj.getName() + " (id:" + proj.getId() + ")", Integer.toString(proj.getId()));
					}

				}
			} catch (Exception e) {

				listBox.add("", "-1");
			}

			return listBox;
		}

		public ListBoxModel doFillFailureSeverityItems() {

			return getSeverityItems();
		}

		public ListBoxModel doFillUnstableSeverityItems() {

			return getSeverityItems();
		}

		private ListBoxModel getSeverityItems() {

			final ListBoxModel listBox = new ListBoxModel();

			listBox.add("None", "None");
			listBox.add("Info or Higher", "Info");
			listBox.add("Low or Higher", "Low");
			listBox.add("Medium or Higher", "Medium");
			listBox.add("High", "High");

			return listBox;
		}

		@Override
		public boolean configure(final StaplerRequest req, final JSONObject formData) throws FormException {
			// To persist global configuration information,
			// set that to properties and call save().
			// ^Can also use req.bindJSON(this, formData);
			//  (easier when there are many fields; need set* methods for this, like setUseFrench)

			save();
			System.out.println("Code Dx descriptor configure method");
			return super.configure(req, formData);
		}

		@Override
		public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
			return super.newInstance(req, formData);
		}
	}
}

