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
import hudson.FilePath;
import hudson.Launcher;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import net.sf.json.JSONObject;

import org.jenkinsci.plugins.codedx.model.CodeDxReportStatistics;
import org.jenkinsci.plugins.codedx.model.CodeDxSeverityStatistics;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import com.secdec.codedx.api.client.CodeDxClient;
import com.secdec.codedx.api.client.CodeDxClientException;
import com.secdec.codedx.api.client.CountGroup;
import com.secdec.codedx.api.client.Job;
import com.secdec.codedx.api.client.Project;
import com.secdec.codedx.api.client.StartAnalysisResponse;

import javax.servlet.ServletException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Jenkins publisher that publishes project source, binaries, and 
 * analysis tool output files to a CodeDx server.
 * @author anthonyd
 *
 */
public class CodeDxPublisher extends Recorder {

    private final String url;
    private final String key;
	private final String projectId;

	private final String sourceAndBinaryFiles;
	private final String toolOutputFiles;
	private final String excludedSourceAndBinaryFiles;
	
/**
 * 
 * @param url URL of the Code Dx server
 * @param key API key of the Code Dx server
 * @param projectId Code Dx project ID
 * @param sourceAndBinaryFiles Comma separated list of source/binary file Ant GLOB patterns
 * @param toolOutputFiles List of paths to tool output files
 * @param excludedSourceAndBinaryFiles Comma separated list of source/binary file Ant GLOB patterns to exclude
 */
    @DataBoundConstructor
    public CodeDxPublisher(final String url, 
    		final String key, 
    		final String projectId, 
    		final String sourceAndBinaryFiles, 
    		final String toolOutputFiles, 
    		final String excludedSourceAndBinaryFiles) {
        this.projectId = projectId;
        this.url = url;
        this.key = key;
        this.sourceAndBinaryFiles = sourceAndBinaryFiles;
        this.excludedSourceAndBinaryFiles = excludedSourceAndBinaryFiles;
        this.toolOutputFiles = toolOutputFiles;
    }	
    
    public String getProjectId() {
        return projectId;
    }
    
    public String getUrl(){
    	
    	return url;
    }
    
    public String getKey(){
    	
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

	

    @Override
    public boolean perform(
			final AbstractBuild<?, ?> build,
			final Launcher launcher, 
			final BuildListener listener) throws InterruptedException, IOException {
    	
    	final List<InputStream> toSend = new ArrayList<InputStream>();
    	
    	listener.getLogger().println("Starting Code Dx Publish");
    	
    	if(projectId.length() == 0 || projectId.equals("-1")){
    		
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
        
        
        if(sourceAndBinaryZip != null){
        	
    		try { 
    			
    			listener.getLogger().println("Adding source/binary zip...");
    			
    			toSend.add(sourceAndBinaryZip.read()); 
    		} 
    		catch (IOException e) { 
    			
    			listener.getLogger().println("Failed to add source/binary zip.");
    		}
    	
        }
        else{
        	
        	listener.getLogger().println("No matching source/binary files.");
        }
           
        String[] files = Util.commaSeparatedToArray(toolOutputFiles);
        
    	for(String file : files){
    		
    		if(file.length() != 0){
    			
    			FilePath path = build.getWorkspace().child(file);
    			
    			if(path.exists()){
    				
    	    		try { 
    	    			
    	    			listener.getLogger().println("Add tool output file " + path.getRemote() + " to request.");
    	    			toSend.add(path.read()); 
    	    		} 
    	    		catch (IOException e) { 
    	    			
    	    			listener.getLogger().println("Failed to add tool output file: " + path);
    	    		}
    			}
    		}
    	}
        
        if(toSend.size() > 0){
        	
        	final CodeDxClient client = new CodeDxClient(url,key);
        	
        	try {
        		listener.getLogger().println("Sending analysis request");
				
        		StartAnalysisResponse response = client.startAnalysis(Integer.parseInt(projectId), toSend.toArray(new InputStream[0]));
				
				listener.getLogger().println("Analysis request succeeded");
				
				listener.getLogger().println("Waiting for analysis to complete");
				
				String status = null;
				
				do{
					
					Thread.sleep(3000);
					status = client.getJobStatus(response.getJobId());
					
					listener.getLogger().println("The STATUS IS: " + status);
					
				} while(Job.QUEUED.equals(status) || Job.RUNNING.equals(status));

				if(Job.COMPLETED.equals(status)){
					
					listener.getLogger().println("Analysis succeeded");
					
					listener.getLogger().println("Fetching severity counts");
					
					List<CountGroup> counts = client.getFindingsGroupedCounts(response.getRunId(), null, "severity");
					
					listener.getLogger().println("Got severity counts");
					
					List<CodeDxSeverityStatistics> severities = new ArrayList<CodeDxSeverityStatistics>();
					
					for(CountGroup group : counts){
					
						CodeDxSeverityStatistics stats = new CodeDxSeverityStatistics(group.getName(),group.getCount());
						listener.getLogger().println(stats);
						severities.add(stats);
					}
					
			        CodeDxResult result = new CodeDxResult(new CodeDxReportStatistics(severities),build);
			        
			        listener.getLogger().println("Adding CodeDx build action");
			        build.addAction(new CodeDxBuildAction(build, result));
			        return true;
				}
				else{
					listener.getLogger().println("Analysis status: " + status);
					return false;
				}
		        
			} catch (NumberFormatException e) {

				listener.getLogger().println("Invalid project Id");
				
			} catch (CodeDxClientException e) {

				listener.getLogger().println("Fatal Error!");
				e.printStackTrace(listener.getLogger());
			}
        }
        else{
        	
        	listener.getLogger().println("Nothing to send, this doesn't seem right!");
        }
        
        return false;
    }
    
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE; // NONE since this is not dependent on the last step
	}
	
    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    /**
     * Descriptor for {@link CodeDxPublisher}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
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
                return FormValidation.error("Please set a project. If none are shown above, then be sure that system settings are configured.");
            if (Integer.parseInt(value) == -1)
                return FormValidation.error("Failed to get available projects, please ensure systems settings are configured.");

            return FormValidation.ok();
        }
        
        public FormValidation doCheckKey(@QueryParameter final String value)
                throws IOException, ServletException {
           
        	if (value.length() == 0)
                return FormValidation.error("Please set a Key.");

            return FormValidation.ok();
        }
        
        public FormValidation doCheckUrl(@QueryParameter final String value)
                throws IOException, ServletException {
            
        	if (value.length() == 0)
                return FormValidation.error("Please set a URL.");

            try {
                new URL(value);

            } catch (MalformedURLException malformedURLException) {
            	return FormValidation.error("Malformed URL");
            }
            
            if(value.toLowerCase().startsWith("http:")){
            	
            	return FormValidation.warning("HTTP is considered insecure, it is recommended that you use HTTPS.");
            }
            else if(value.toLowerCase().startsWith("https:")){
            	
                return FormValidation.ok(); 
            }
            else{
            	
            	return FormValidation.error("Invalid protocol, please use HTTPS or HTTP.");
            }
        }
        
        public FormValidation doCheckSourceAndBinaryFiles(@QueryParameter final String value, @QueryParameter final String toolOutputFiles, @AncestorInPath AbstractProject project){
        	
        	if(value.length() == 0){
        		
        		if(toolOutputFiles.length() == 0)
        			return FormValidation.error("You must specify \"Tool Output Files\" and/or \"Source and Binary Files\"");
        		else
        			return FormValidation.warning("It is recommended that at least source files are provided to Code Dx.");
        	}
        	
        	return Util.checkCSVGlobMatches(value, project.getSomeWorkspace());
        }
        
        public FormValidation doCheckExcludedSourceAndBinaryFiles(@QueryParameter final String value, @AncestorInPath AbstractProject project){

        	return Util.checkCSVGlobMatches(value, project.getSomeWorkspace());
        }
        
        public FormValidation doCheckToolOutputFiles(@QueryParameter final String value, @QueryParameter final String sourceAndBinaryFiles, @AncestorInPath AbstractProject project){

        	if(value.length() == 0 && sourceAndBinaryFiles.length() == 0){
        		
        		return FormValidation.error("You must specify \"Tool Output Files\" and/or \"Source and Binary Files\"");
        	}
        	
        	return Util.checkCSVFileMatches(value, project.getSomeWorkspace());
        }
        
    	public ListBoxModel doFillProjectIdItems(@QueryParameter final String url, @QueryParameter final String key) {
			final ListBoxModel listBox = new ListBoxModel();
		
			final CodeDxClient client = new CodeDxClient(url,key);
				
			try{
				final List<Project> projects = client.getProjects();

				for (final Project proj : projects) {

					listBox.add(proj.getName() + " (id=" + proj.getId() +")", Integer.toString(proj.getId()));
				}
			}
			catch(Exception e){
				
				listBox.add("","-1");
			}

            return listBox;
        }
    	
        @Override
        public boolean configure(final StaplerRequest req, final JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            
        	save();
            return super.configure(req,formData);
        } 	
    }
}

