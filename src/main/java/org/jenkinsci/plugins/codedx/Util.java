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
import hudson.util.FormValidation;

import java.io.IOException;

/**
 * Contains string and GLOB pattern matching utility methods.
 * @author anthonyd
 *
 */
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
					if(path.length() == 0 || ! new FilePath(workspace, path).exists()){
						
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
