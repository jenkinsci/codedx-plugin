package org.jenkinsci.plugins.codedx;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import hudson.FilePath;

public class Archiver {

	public static FilePath Archive(FilePath workspace, PathEntry[] paths, String prefix) throws IOException, InterruptedException{
		
		final Set<String> bigSetOFiles = new HashSet<String>();
		
		for(PathEntry path : paths){
			
			if(path != null && path.getValue().length() > 0){
				
				for(FilePath match : workspace.list(path.getValue())){
					
					bigSetOFiles.add(match.getRemote());
				};
			}
		}
		
		if(bigSetOFiles.size() == 0){
			
			return null;
		}
		
		final FilePath result = workspace.createTempFile(prefix, ".zip");
		
		workspace.zip(result.write(), new FileFilter(){

			public boolean accept(File file) {

				try {
					
					return bigSetOFiles.contains(file.getCanonicalPath());

				} catch (IOException e) {
					
					e.printStackTrace();
					
					return false;
				}			
			}
			
		});
		
		return result;
	}
}
