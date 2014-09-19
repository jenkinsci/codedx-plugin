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
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import net.sf.json.JSONObject;

import org.jenkinsci.plugins.codedx.client.AnalysisArtifact;
import org.jenkinsci.plugins.codedx.client.CodeDxClient;
import org.jenkinsci.plugins.codedx.client.CodeDxClientException;
import org.jenkinsci.plugins.codedx.client.Project;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class CodeDxPublisher extends Recorder {

	private final String projectId;
	private final String bin;
	private final PathEntry[] sourcePathEntries;
	private final PathEntry[] binaryPathEntries;
	private final PathEntry[] outputFileEntries;
	
    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public CodeDxPublisher(final String projectId, final String bin, final PathEntry[] sourcePathEntries,final PathEntry[] binaryPathEntries, final PathEntry[] outputFileEntries) {
        this.projectId = projectId;
        this.bin = bin;
        this.sourcePathEntries = sourcePathEntries;
        this.binaryPathEntries = binaryPathEntries;
        this.outputFileEntries = outputFileEntries;
    }	

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getProjectId() {
        return projectId;
    }
    
    public String getBin() {
        return bin;
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
    	
    	final List<AnalysisArtifact> toSend = new ArrayList<AnalysisArtifact>();
    	
    	final String key = getDescriptor().getKey();
    	final String url = getDescriptor().getUrl();
    	
        listener.getLogger().println("CodeDx URL: " + url);
        
        listener.getLogger().println("Creating source zip...");
        FilePath sourceZip = Archiver.Archive(build.getWorkspace(), sourcePathEntries, "source");
        
        if(sourceZip != null){
        	
        	toSend.add(new JenkinsFileArtifact(sourceZip));
        }
        else{
        	
        	listener.getLogger().println("No matching source files");
        }
        
        listener.getLogger().println("Creating binary zip...");
        FilePath binaryZip = Archiver.Archive(build.getWorkspace(), binaryPathEntries, "binary");
     
        if(binaryZip != null){
        	
        	toSend.add(new JenkinsFileArtifact(binaryZip));
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
        				
        				toSend.add(new JenkinsFileArtifact(path));
        			}
        		}
        	}
        }
        
        if(toSend.size() > 0){
        	
        	final CodeDxClient client = new CodeDxClient(url,key);
        	
        	try {
				client.startAnalysis(Integer.parseInt(projectId), toSend.toArray(new AnalysisArtifact[0]));
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
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
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
        private String url;
        private String key;

        /**
         * In order to load the persisted global configuration, you have to 
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
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

        @Override
        public boolean configure(final StaplerRequest req, final JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            url = formData.getString("url");
            key = formData.getString("key");
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            save();
            return super.configure(req,formData);
        }

    	public ListBoxModel doFillProjectIdItems() {
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
    	
        
        public String getUrl() {
            return url;
        }
        
        public String getKey() {
            return key;
        }
    }
}

