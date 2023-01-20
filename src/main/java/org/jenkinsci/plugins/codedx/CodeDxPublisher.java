/*
 * © 2023 Synopsys, Inc. All rights reserved worldwide.
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
 */
package org.jenkinsci.plugins.codedx;

import com.codedx.api.client.*;
import com.codedx.api.client.Job;
import com.codedx.api.client.Project;
import com.codedx.security.JenkinsSSLConnectionSocketFactoryFactory;
import com.codedx.util.CodeDxVersion;
import hudson.AbortException;
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
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jenkinsci.plugins.codedx.model.CodeDxReportStatistics;
import org.jenkinsci.plugins.codedx.model.CodeDxGroupStatistics;
import org.jenkinsci.plugins.codedx.monitor.AnalysisMonitor;
import org.jenkinsci.plugins.codedx.monitor.DirectAnalysisMonitor;
import org.jenkinsci.plugins.codedx.monitor.GitJobAnalysisMonitor;
import org.kohsuke.stapler.*;

import javax.net.ssl.SSLHandshakeException;
import javax.servlet.ServletException;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

/**
 * Jenkins publisher that publishes project source, binaries, and
 * analysis tool output files to a CodeDx server.
 *
 * @author anthonyd
 */
public class CodeDxPublisher extends Recorder implements SimpleBuildStep {

	private final String url;
	private final String key;
	private final String projectId;

	// Comma separated list of source/binary file Ant GLOB patterns
	private String sourceAndBinaryFiles;
	// List of paths to tool output files
	private String toolOutputFiles;
	// Comma separated list of source/binary file Ant GLOB patterns to exclude
	private String excludedSourceAndBinaryFiles;

	private String analysisName;

	// Contains the fields applicable when the user chooses to have Jenkins wait for
	// analysis runs to complete.
	private AnalysisResultConfiguration analysisResultConfiguration;

	private transient CodeDxClient client;

	private String selfSignedCertificateFingerprint;

	private String targetBranchName, baseBranchName;

	private BuildEffectBehavior errorHandlingBehavior;

	private GitFetchConfiguration gitFetchConfiguration;

	private final static Logger logger = Logger.getLogger(CodeDxPublisher.class.getName());

	/**
	 * @param url                          URL of the Code Dx server
	 * @param key                          API key of the Code Dx server
	 * @param projectId                    Code Dx project ID
	 */
	@DataBoundConstructor
	public CodeDxPublisher(
			final String url,
			final String key,
			final String projectId,
			final String analysisName
	) {
		this.projectId = projectId;
		this.url = url;
		this.key = key;
		this.analysisName = analysisName.trim();

		this.sourceAndBinaryFiles = "";
		this.excludedSourceAndBinaryFiles = "";
		this.toolOutputFiles = "";
		this.analysisResultConfiguration = null;
		this.selfSignedCertificateFingerprint = null;
		this.targetBranchName = null;
		this.baseBranchName = null;
		this.errorHandlingBehavior = BuildEffectBehavior.MarkFailed;

		this.gitFetchConfiguration = null;

		setupClient();
	}

	private void setupClient() {
		if (this.client == null) {
			this.client = buildClient(url, key, selfSignedCertificateFingerprint);
		}
	}

	public AnalysisResultConfiguration getAnalysisResultConfiguration() {
		return analysisResultConfiguration;
	}

	@DataBoundSetter
	public void setAnalysisResultConfiguration(AnalysisResultConfiguration analysisResultConfiguration) {
		this.analysisResultConfiguration = analysisResultConfiguration;
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

	@DataBoundSetter
	public void setSourceAndBinaryFiles(String sourceAndBinaryFiles) {
		this.sourceAndBinaryFiles = sourceAndBinaryFiles;
	}

	public String getToolOutputFiles() {
		return toolOutputFiles;
	}

	@DataBoundSetter
	public void setToolOutputFiles(String toolOutputFiles) {
		this.toolOutputFiles = toolOutputFiles;
	}

	public String getExcludedSourceAndBinaryFiles() {
		return excludedSourceAndBinaryFiles;
	}

	@DataBoundSetter
	public void setExcludedSourceAndBinaryFiles(String excludedSourceAndBinaryFiles) {
		this.excludedSourceAndBinaryFiles = excludedSourceAndBinaryFiles;
	}

	public String getSelfSignedCertificateFingerprint() {
		return selfSignedCertificateFingerprint;
	}

	@DataBoundSetter
	public void setSelfSignedCertificateFingerprint(String selfSignedCertificateFingerprint) {
		this.selfSignedCertificateFingerprint = selfSignedCertificateFingerprint;

		this.client = null;
		setupClient();
	}

	public String getAnalysisName(){ return analysisName; }

	private String getLatestAnalysisUrl() {
		if (projectId.length() != 0 && !projectId.equals("-1")) {
			setupClient();
			return client.buildLatestFindingsUrl(Integer.parseInt(projectId));
		} else {
			return null;
		}
	}

	public String getTargetBranchName() {
		return targetBranchName;
	}

	@DataBoundSetter
	public void setTargetBranchName(String targetBranchName) {
		if (targetBranchName != null) targetBranchName = targetBranchName.trim();

		if (targetBranchName != null && targetBranchName.length() > 0)
			this.targetBranchName = targetBranchName;
		else
			this.targetBranchName = null;
	}

	public String getBaseBranchName() {
		return baseBranchName;
	}

	@DataBoundSetter
	public void setBaseBranchName(String baseBranchName) {
		if (baseBranchName != null) baseBranchName = baseBranchName.trim();

		if (baseBranchName != null && baseBranchName.length() > 0)
			this.baseBranchName = baseBranchName;
		else
			this.baseBranchName = null;
	}

	public BuildEffectBehavior getErrorHandlingBehavior() {
		return errorHandlingBehavior;
	}

	@DataBoundSetter
	public void setErrorHandlingBehavior(BuildEffectBehavior behavior) {
		errorHandlingBehavior = behavior;
	}

	public GitFetchConfiguration getGitFetchConfiguration() {
		return gitFetchConfiguration;
	}

	@DataBoundSetter
	public void setGitFetchConfiguration(GitFetchConfiguration config) {
		gitFetchConfiguration = config;
	}

	// Sets the build status based on `errorHandlingBehavior`. Only meant to be used for Code Dx-specific
	// errors, eg analysis failure and connection issues. Configuration errors will be thrown as usual
	// since those issues aren't related to Code Dx itself.
	//
	// returns true if we ignore the error, false if we want to exit prematurely
	private Boolean handleCodeDxError(Run<?, ?> build, PrintStream buildOutput, String cause) throws AbortException {
		buildOutput.println(cause);
		if (errorHandlingBehavior != BuildEffectBehavior.None) {
			build.setResult(errorHandlingBehavior.getEquivalentResult());
			return false;
		} else {
			buildOutput.println("This will be ignored since errorHandlingBehavior is set to " + errorHandlingBehavior.getLabel());
			return true;
		}
	}

	@Override
	public void perform(
			final Run<?, ?> build,
			final FilePath workspace,
			final Launcher launcher,
			final TaskListener listener) throws InterruptedException, IOException {

		Date startingDate = new Date();

		setupClient();
		final Map<String, InputStream> toSend = new HashMap<String, InputStream>();
		final PrintStream buildOutput = listener.getLogger();

		buildOutput.println("Publishing build to Code Dx:");
		buildOutput.println("Error handling set to: " + errorHandlingBehavior.getLabel());

		if (projectId.length() == 0 || projectId.equals("-1")) {
			buildOutput.println("No project has been selected");
			return;
		}

		ProjectContext project;
		try {
			project = new ProjectContext(Integer.parseInt(projectId));
		} catch (NumberFormatException e) {
			throw new AbortException("Invalid project ID: " + projectId);
		}

		buildOutput.println(String.format("Publishing to Code Dx server at %s to Code Dx project %s: ", url, projectId));

		final CodeDxClient repeatingClient = new CodeDxRepeatingClient(this.client, buildOutput);

		CodeDxVersion cdxVersion = null;
		try {
			cdxVersion = repeatingClient.getCodeDxVersion();
			buildOutput.println("Got Code Dx version: " + cdxVersion);
		} catch (CodeDxClientException e) {
			e.printStackTrace(buildOutput);
			if (handleCodeDxError(build, buildOutput, "An error occurred fetching the Code Dx version")) {
				buildOutput.println("The Code Dx plugin cannot continue without verifying the Code Dx version, exiting");
			}
			// we can't continue without a version since we don't want to accidentally put data into the wrong target
			// by eg using a placeholder version which skips over the Code Dx branch
			return;
		}

		ValueResolver valueResolver = new ValueResolver(build, workspace, listener, buildOutput);
		TargetBranchChecker branchChecker = new TargetBranchChecker(project, repeatingClient, valueResolver, buildOutput);
		try {
			branchChecker.validate(cdxVersion, targetBranchName, baseBranchName);
		} catch (AbortException e) {
			// configuration error
			throw e;
		} catch (IOException e) {
			e.printStackTrace(buildOutput);
			handleCodeDxError(build, buildOutput, "An error occurred while validating branch selection");
			// connection error; this will only happen if a target branch was specified. we can't continue without
			// applying that target branch, or we might cause data to be written to an unexpected target branch
			return;
		}

		project = project.withBranch(branchChecker.getTargetBranchName());

		buildOutput.println("Creating source/binary zip...");

		FilePath sourceAndBinaryZip = Archiver.archive(workspace,
				Util.commaSeparatedToArray(sourceAndBinaryFiles),
				Util.commaSeparatedToArray(excludedSourceAndBinaryFiles),
				"source", buildOutput);

		if (sourceAndBinaryZip != null) {
			buildOutput.println("Adding source/binary zip...");
			toSend.put("Jenkins-SourceAndBinary.zip", new DeferredFilePathInputStream(sourceAndBinaryZip));
		} else {
			buildOutput.println("No matching source/binary files.");
		}

		String[] files = Util.commaSeparatedToArray(toolOutputFiles);

		for (String file : files) {
			if (file.length() != 0) {
				FilePath path = workspace.child(file);

				if (path.exists()) {
					buildOutput.println("Add tool output file " + path.getRemote() + " to request.");
					toSend.put(path.getName(), new DeferredFilePathInputStream(path));
				} else {
					buildOutput.println("Path specified but could not be found: " + path);
				}
			}
		}

		GitFetchConfiguration effectiveGitConfig = gitFetchConfiguration;
		if (effectiveGitConfig != null) {
			buildOutput.println("Verifying git config for Code Dx project...");
			try {
				GitConfigResponse response = repeatingClient.getProjectGitConfig(project);
				if (response.getUrl().isEmpty()) {
					buildOutput.println("'Include Git Source' was enabled but the project does not have a Git config assigned. 'Include Git Source' will be disabled for this run.");
					effectiveGitConfig = null;
				}
			} catch (Exception e) {
				if (!handleCodeDxError(build, buildOutput, "There was a problem fetching the project's Git config")) {
					return;
				}
				buildOutput.println("'Include Git Source' will be disabled for this run.");
				effectiveGitConfig = null;
			}
		}

		if (toSend.size() > 0 || effectiveGitConfig != null) {
			try {
				buildOutput.println("Submitting files to Code Dx for analysis");

				AnalysisMonitor analysisMonitor;

				StartAnalysisResponse response;
				try {
					boolean includeGitSource = effectiveGitConfig != null;
					String targetGitBranch = null;
					if (includeGitSource) {
						targetGitBranch = effectiveGitConfig.getSpecificBranch();
						if (targetGitBranch != null) {
							targetGitBranch = valueResolver.resolve("Git branch", targetGitBranch);
							buildOutput.println("Using Git branch for Code Dx: " + targetGitBranch);
						} else {
							buildOutput.println("No Git branch specified, using Code Dx project's default Git branch");
						}
					}

					response = repeatingClient.startAnalysis(
							project.getProjectId(),
							includeGitSource,
							targetGitBranch,
							branchChecker.getBaseBranchName(),
							branchChecker.getTargetBranchName(),
							toSend
					);

					// (shouldn't be necessary, but this condition was checked in previous impl., so it will be preserved)
					if (response == null) {
						handleCodeDxError(build, buildOutput, "Received null data for the analysis job");
						// everything else has to do with checking for the analysis indicated by the response, nothing
						// else to do if this fails
						return;
					}

					analysisMonitor = effectiveGitConfig != null
							? new GitJobAnalysisMonitor(project, response, buildOutput)
							: new DirectAnalysisMonitor(response, buildOutput);

				} catch (CodeDxClientException e) {
					String errorSpecificMessage;

					switch(e.getHttpCode()) {
						case 400:
							errorSpecificMessage =
									" (Bad Request: have you included files from unsupported Tools? " +
											"Code Dx Standard Edition does not support uploading tool results)";
							break;
						case 403:
							errorSpecificMessage = " (Forbidden: have you configured your key and permissions correctly?)";
							break;
						case 404:
							errorSpecificMessage = " (Project Not Found: is it possible it was deleted?)";
							break;
						case 500:
							errorSpecificMessage = " (Internal Server Error: Please check your Code Dx server logs for more details)";
							break;
						default:
							errorSpecificMessage = "";
					}

					String message =
						String.format("Failed to start analysis%s.", errorSpecificMessage) + '\n' +
						String.format("Response Status: %d: %s", e.getHttpCode(), e.getResponseMessage()) + '\n' +
						String.format("Response Content: %s", e.getResponseContent()) + '\n' +
						Util.getStackTrace(e);

					buildOutput.println(message);
					handleCodeDxError(build, buildOutput, "Failed to start analysis");
					return; // nothing else to do if we can't start the analysis, everything else relies on it
				} finally {
					// close streams after we're done sending them
					for(Map.Entry<String, InputStream> entry : toSend.entrySet()){
						IOUtils.closeQuietly(entry.getValue());
					}
				}

				buildOutput.println("Code Dx accepted files for analysis");

				int analysisId = analysisMonitor.waitForStart(repeatingClient);

				// Set the analysis name on the server
				if(analysisName == null || analysisName.length() == 0) {
					buildOutput.println("No 'Analysis Name' was chosen.");
				} else if (analysisId == -1) {
					buildOutput.println("Code Dx did not provide an analysis ID - the 'Analysis Name' will not be applied.");
				} else {
					String expandedAnalysisName = valueResolver.resolve("analysis name", analysisName);
					buildOutput.println("Analysis Name: " + expandedAnalysisName);
					buildOutput.println("Analysis Id: " + analysisId);

					if(cdxVersion.compareTo(CodeDxVersion.MIN_FOR_ANALYSIS_NAMES) < 0){
						buildOutput.println("The connected Code Dx server is only version " + cdxVersion +
								", which doesn't support naming analyses (minimum supported version is " +
								CodeDxVersion.MIN_FOR_ANALYSIS_NAMES + "). The analysis name will not be set.");
					} else {
						try {
							repeatingClient.setAnalysisName(project, analysisId, expandedAnalysisName);
							buildOutput.println("Successfully updated analysis name.");
						} catch (CodeDxClientException e) {
							e.printStackTrace(buildOutput);
							// this doesn't affect anything else in the plugin, only exit early if we're meant to fail
							if (!handleCodeDxError(build, buildOutput, "Got error from Code Dx API Client while trying to set the analysis name")) {
								return;
							}
						}
					}
				}

				if (analysisResultConfiguration == null) {
					logger.info("Project not configured to wait on analysis results");
					return;
				}

				String status;
				try {
					status = analysisMonitor.waitForFinish(repeatingClient);
				} catch (CodeDxClientException e) {
					e.printStackTrace(buildOutput);
					handleCodeDxError(build, buildOutput, "There was an error querying for the analysis status");
					return; // nothing else to do if the analysis failed
				}

				if (Job.COMPLETED.equals(status)) {
					try {
						buildOutput.println("Analysis succeeded");

						buildOutput.println("Fetching severity counts");

						Filter notGoneFilter = new Filter();
						notGoneFilter.setNotStatus(new String[]{Filter.STATUS_GONE});
						List<CountGroup> severityCounts = repeatingClient.getFindingsGroupedCounts(project, notGoneFilter, "severity");

						buildOutput.println("Fetching status counts");

						Filter notAssignedFilter = new Filter();
						notAssignedFilter.setNotStatus(new String[]{ Filter.STATUS_ASSIGNED, Filter.STATUS_GONE });

						List<CountGroup> statusCounts = repeatingClient.getFindingsGroupedCounts(project, notAssignedFilter, "status");

						Filter assignedFilter = new Filter();
						assignedFilter.setStatus(new String[]{Filter.STATUS_ASSIGNED});

						buildOutput.println("Fetching assigned count");

						//Since CodeDx splits assigned status into different statuses (one per user),
						//we need to get the total assigned count and add our own CountGroup.
						int assignedCount = repeatingClient.getFindingsCount(project, assignedFilter);

						if (assignedCount > 0) {

							CountGroup assignedGroup = new CountGroup();
							assignedGroup.setName("Assigned");
							assignedGroup.setCount(assignedCount);
							statusCounts.add(assignedGroup);
						}

						buildOutput.println("Building table and charts");

						Map<String, CodeDxReportStatistics> statMap = new HashMap<String, CodeDxReportStatistics>();

						statMap.put("severity", createStatistics(severityCounts));
						statMap.put("status", createStatistics(statusCounts));

						CodeDxResult result = new CodeDxResult(statMap, build);

						buildOutput.println("Adding CodeDx build action");
						build.addAction(new CodeDxBuildAction(build, analysisResultConfiguration, getLatestAnalysisUrl(), result));

						AnalysisResultChecker checker = new AnalysisResultChecker(repeatingClient,
								cdxVersion,
								analysisResultConfiguration.getFailureSeverity(),
								analysisResultConfiguration.getUnstableSeverity(),
								startingDate, // the time this process started is the "new" threshold for filtering
								analysisResultConfiguration.isFailureOnlyNew(),
								analysisResultConfiguration.isUnstableOnlyNew(),
								analysisResultConfiguration.getPolicyBreakBuildBehavior(),
								project,
								buildOutput);
						Result buildResult = checker.checkResult();
						build.setResult(buildResult);
					} catch (CodeDxClientException e) {
						e.printStackTrace(buildOutput);
						handleCodeDxError(build, buildOutput, "There was an error fetching/processing analysis results");
					}
				} else {
					buildOutput.println("Analysis status: " + status);
					handleCodeDxError(build, buildOutput, "Analysis status was non-success");
				}
			}
			catch (AbortException e) {
				// re-throw any "expected" exceptions; if we didn't want these to be thrown due to error
				// handling behavior, they wouldn't have been thrown in the first place
				throw e;
			} catch (Exception e) {
				// any remaining exceptions from this `try` block should be related to comms with Code Dx
				e.printStackTrace(buildOutput);
				handleCodeDxError(build, buildOutput, "An unexpected error occurred");
			} finally {
				if(sourceAndBinaryZip != null){
					sourceAndBinaryZip.delete();
				}
			}
		} else {
			throw new AbortException("Nothing to send, this doesn't seem right! Please check your 'Code Dx > Source and Binary Files' configuration.");
		}
	}

	public static CodeDxClient buildClient(String url, String key, String fingerprint) {
		CodeDxClient client = new CodeDxClient(url, key);
		try {
			if (fingerprint != null) {
				fingerprint = fingerprint.replaceAll("[^a-fA-F0-9]", "");
			}
			URL parsedUrl = new URL(url);
			SSLConnectionSocketFactory socketFactory = JenkinsSSLConnectionSocketFactoryFactory.getFactory(fingerprint, parsedUrl.getHost());
			HttpClientBuilder builder = HttpClientBuilder.create();
			builder.setSSLSocketFactory(socketFactory);
			client = new CodeDxClient(url, key, builder);
		} catch (MalformedURLException e) {
			logger.warning("A valid CodeDxClient could not be built. Malformed URL: " + url);
		} catch (GeneralSecurityException e) {
			logger.warning("A valid CodeDxClient could not be built. GeneralSecurityException: url: " + url + ", fingerprint: " + fingerprint);
		} catch (Exception e) {
			logger.warning("An exception was thrown while building the client " + e);
			e.printStackTrace();
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
						logger.warning("When retrieving projects: " + e);
						e.printStackTrace();
						if (isFingerprintMismatch((SSLHandshakeException)e)) {
							return FormValidation.warning("The fingerprint doesn't match the fingerprint of the certificate presented by the server");
						} else {
							return FormValidation.warning("A secure connection to the server could not be established");
						}
					}
				}
			}
			return FormValidation.ok();
		}

		public FormValidation doCheckSourceAndBinaryFiles(@QueryParameter final String value, @QueryParameter final String toolOutputFiles, @AncestorInPath AbstractProject project) {
			if (project == null) {
				return FormValidation.ok();
			}

			if (value.length() == 0) {

				if (toolOutputFiles.length() == 0)
					return FormValidation.error("You must specify \"Tool Output Files\" and/or \"Source and Binary Files\"");
				else
					return FormValidation.warning("It is recommended that at least source files are provided to Code Dx.");
			}

			return Util.checkCSVGlobMatches(value, project.getSomeWorkspace());
		}

		public FormValidation doCheckExcludedSourceAndBinaryFiles(@QueryParameter final String value, @AncestorInPath AbstractProject project) {
			if (project == null) {
				return FormValidation.ok();
			} else {
				return Util.checkCSVGlobMatches(value, project.getSomeWorkspace());
			}
		}

		public FormValidation doCheckToolOutputFiles(@QueryParameter final String value, @QueryParameter final String sourceAndBinaryFiles, @AncestorInPath AbstractProject project) {

			if (project == null) {
				return FormValidation.ok();
			}

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
				logger.warning("Exception when populating projects dropdown " + e);
				listBox.add("", "-1");
			}

			return listBox;
		}

		private ListBoxModel getErrorBehaviorItems() {
			ListBoxModel listBox = new ListBoxModel();
			listBox.add(BuildEffectBehavior.None.getLabel(), BuildEffectBehavior.None.name());
			listBox.add(BuildEffectBehavior.MarkUnstable.getLabel(), BuildEffectBehavior.MarkUnstable.name());
			listBox.add(BuildEffectBehavior.MarkFailed.getLabel(), BuildEffectBehavior.MarkFailed.name());
			return listBox;
		}

		public ListBoxModel doFillErrorHandlingBehaviorItems() {
			return getErrorBehaviorItems();
		}

		public ListBoxModel doFillPolicyBreakBuildBehaviorItems() {
			return getErrorBehaviorItems();
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
			listBox.add("High or Higher", "High");
			listBox.add("Critical", "Critical");

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

	private static boolean isFingerprintMismatch(SSLHandshakeException exception) {
		return exception.getMessage().contains("None of the TrustManagers trust this certificate chain");
	}
}

