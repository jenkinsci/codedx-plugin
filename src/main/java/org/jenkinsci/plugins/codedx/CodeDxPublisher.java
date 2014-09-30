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
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import com.secdec.codedx.api.client.CodeDxClient;
import com.secdec.codedx.api.client.CodeDxClientException;
import com.secdec.codedx.api.client.Project;

import javax.servlet.ServletException;

import java.io.IOException;
import java.io.InputStream;
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
	private final PathEntry[] sourcePathEntries;
	private final PathEntry[] binaryPathEntries;
	private final PathEntry[] outputFileEntries;
	
	/**
	 * 
	 * @param projectId The CodeDx project ID to publish to
	 * @param sourcePathEntries An array of source path filters in Ant GLOB format
	 * @param binaryPathEntries An array of binary path filters in Ant GLOB format
	 * @param outputFileEntries An array of analysis tool output file paths
	 */
    @DataBoundConstructor
    public CodeDxPublisher(final String url, final String key, final String projectId, final PathEntry[] sourcePathEntries,final PathEntry[] binaryPathEntries, final PathEntry[] outputFileEntries) {
        this.projectId = projectId;
        this.sourcePathEntries = sourcePathEntries;
        this.binaryPathEntries = binaryPathEntries;
        this.outputFileEntries = outputFileEntries;
        this.url = url;
        this.key = key;
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
    
    public PathEntry[] getSourcePathEntries() {
        return sourcePathEntries;
    }
    
    public PathEntry[] getBinaryPathEntries() {
        return binaryPathEntries;
    }
    
    public PathEntry[] getOutputFileEntries() {
        return outputFileEntries;
    }

    
    @Override
    public boolean perform(
			final AbstractBuild<?, ?> build,
			final Launcher launcher, 
			final BuildListener listener) throws InterruptedException, IOException {
    	
    	final List<InputStream> toSend = new ArrayList<InputStream>();
    	
        listener.getLogger().println("CodeDx URL: " + url);
        
        listener.getLogger().println("Creating source zip...");
        FilePath sourceZip = Archiver.Archive(build.getWorkspace(), sourcePathEntries, "source");
        
        
        if(sourceZip != null){
        	
    		try { 
    			
    			toSend.add(sourceZip.read()); 
    		} 
    		catch (IOException e) { 
    			
    			listener.getLogger().println("Failed to add source zip");
    		}
    	
        }
        else{
        	
        	listener.getLogger().println("No matching source files");
        }
        
        listener.getLogger().println("Creating binary zip...");
        FilePath binaryZip = Archiver.Archive(build.getWorkspace(), binaryPathEntries, "binary");
     
        if(binaryZip != null){
        	
    		try { 
    			
    			toSend.add(binaryZip.read()); 
    		} 
    		catch (IOException e) { 
    			
    			listener.getLogger().println("Failed to add binary zip");
    		}
        }
        else{
        	
        	listener.getLogger().println("No matching binary files");
        }
        
        if(outputFileEntries.length != 0){
        	
        	for(PathEntry entry : outputFileEntries){
        		
        		String value = entry.getValue();
        		
        		if(value != null && value.length() != 0){
        			
        			FilePath path = build.getWorkspace().child(value);
        			
        			if(path.exists()){
        				
        	    		try { 
        	    			
        	    			toSend.add(path.read()); 
        	    		} 
        	    		catch (IOException e) { 
        	    			
        	    			listener.getLogger().println("Failed to add tool output file: " + path);
        	    		}
        			}
        		}
        	}
        }
        
        if(toSend.size() > 0){
        	
        	final CodeDxClient client = new CodeDxClient(url,key);
        	
        	try {
				client.startAnalysis(Integer.parseInt(projectId), toSend.toArray(new InputStream[0]));
			} catch (NumberFormatException e) {

				listener.getLogger().println("Invalid project Id");
				
			} catch (CodeDxClientException e) {

				e.printStackTrace();
			}
        }
        return true;
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
            return "Publish to CodeDx";
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

            return FormValidation.ok();
        }

		public FormValidation doTestConnection(@QueryParameter final String url, @QueryParameter final String key) throws IOException, ServletException {

			final CodeDxClient client = new CodeDxClient(url,key);
			
			try{
			
				client.getProjects();
				
			} catch(Exception e){
			    e.printStackTrace();
				return FormValidation.error("Unable to connect to CodeDx server.");
			}
			
			return FormValidation.ok("CodeDx connection success!");
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
				
				listBox.add("ERROR RETRIEVING PROJECTS", "-1");
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

