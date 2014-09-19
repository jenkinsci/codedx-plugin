package org.jenkinsci.plugins.codedx;

import hudson.FilePath;

import java.io.IOException;
import java.io.InputStream;

import org.jenkinsci.plugins.codedx.client.AnalysisArtifact;

public class JenkinsFileArtifact implements AnalysisArtifact {

	private FilePath path;

	public JenkinsFileArtifact(FilePath path){
		
		this.path = path;
	}

	public InputStream createInputStream() {
		// TODO Auto-generated method stub
		try {
			return path.read();
		} catch (IOException e) {

			return null;
		}
	}

}
