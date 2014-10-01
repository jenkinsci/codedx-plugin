package org.jenkinsci.plugins.codedx;

import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.util.FormValidation;

import java.io.IOException;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;

public class Util {

    public static String[] commaSeparatedToArray(String str){
    	
    	return str.split("\\s*,\\s*");
    }
    
    public static FormValidation checkCSVGlobMatches(final String value, final FilePath workspace){
    	

    	if(value.length() != 0  && workspace != null){
    		
    		for(String path : Util.commaSeparatedToArray(value)){

    			try {
					if(path.length() == 0 || workspace.list(path).length == 0){
						
						return FormValidation.warning(path + " doesn't match anything in the workspace.");
					}
				} catch (Exception e) {

					return FormValidation.error(path + " is not a valid Ant GLOB pattern. Note that patterns must not begin with a '/'");
				} 
    		}
    	}
    	
    	return FormValidation.ok();
    }
    
    public static FormValidation checkCSVFileMatches(final String value, final FilePath workspace){
    	

    	if(value.length() != 0  && workspace != null){
    		
    		for(String path : Util.commaSeparatedToArray(value)){

    			try {
					if(path.length() == 0 || new FilePath(workspace,value).exists() == false){
						
						return FormValidation.warning(path + " does not exist in the workspace.");
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}
    	
    	return FormValidation.ok();
    }
}
