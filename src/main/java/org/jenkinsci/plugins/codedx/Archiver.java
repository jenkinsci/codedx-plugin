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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import hudson.FilePath;

/**
 * Archiving utility class
 * 
 * @author anthonyd
 *
 */
public class Archiver {

	/**
	 * 
     * Creates zips of the workspace using an array of path filters (in Ant GLOB format)
     * to determine which files to include in the zip.  The zip file will have a random
     * name with a specified prefix.
     * 
	 * @param workspace Workspace for the job.
	 * @param paths Paths in the ANT Glob format to include.
	 * @param excludePaths Paths in the ANT Glob format to exclude.
	 * @param prefix Prefix of the zip file to create.
	 * @return The zip archive FilePath.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static FilePath Archive(FilePath workspace, String[] paths, String[] excludePaths, String prefix, final PrintStream logger) throws IOException, InterruptedException{
		
		final Set<String> includeFiles = new HashSet<String>();
		final Set<String> excludeFiles = new HashSet<String>();
		
		//Build up a set of all files to include
		for(String path : paths){
			
			if(path != null && path.length() > 0){
				
				for(FilePath match : workspace.list(path)){
					
					includeFiles.add(match.getRemote());
				};
			}
		}
		
		//Build up a set of all files to excluded
		for(String path : excludePaths){
			
			if(path != null && path.length() > 0){
				
				for(FilePath match : workspace.list(path)){
					
					excludeFiles.add(match.getRemote());
				};
			}
		}
		
		if(includeFiles.size() == 0){
			
			return null;
		}
		
		//Create our zip file
		final FilePath result = workspace.createTempFile(prefix, ".zip");
		
		//Zip up the workspace filtering on files to include.
		workspace.zip(result.write(), new FileFilter(){

			public boolean accept(File file) {

				boolean include = includeFiles.contains(file.getAbsolutePath()) && !excludeFiles.contains(file.getAbsolutePath());
				
				if(include){
				
					logger.println("Adding " + file + " to zip.");
				}
				
				return include; 		
			}
			
		});
		
		return result;
	}
}
