package org.jenkinsci.plugins.codedx;

import org.kohsuke.stapler.DataBoundConstructor;

public class GitFetchConfiguration {
	private String specificBranch;

	@DataBoundConstructor
	public GitFetchConfiguration(String specificBranch) {
		this.specificBranch = specificBranch;
	}

	public String getSpecificBranch() {
		if (specificBranch != null && specificBranch.trim().length() > 0) {
			return specificBranch;
		} else {
			return null;
		}
	}

	public void setSpecificBranch(String branch) {
		specificBranch = branch;
	}
}
