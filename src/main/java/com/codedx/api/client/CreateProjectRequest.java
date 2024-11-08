package com.codedx.api.client;

public class CreateProjectRequest {
	private String name;
	private String defaultBranchName;

	public CreateProjectRequest() {}

	public CreateProjectRequest(String projectName, String defaultBranchName) {
		setName(projectName);
		setDefaultBranchName(defaultBranchName);
	}

	public String getDefaultBranchName() {
		return defaultBranchName;
	}

	public void setDefaultBranchName(String defaultBranchName) {
		if (defaultBranchName != null && !defaultBranchName.trim().isEmpty())
			this.defaultBranchName = defaultBranchName;
		else
			this.defaultBranchName = null;
	}

	public String getName() {
		return name;
	}

	public void setName(String projectName) {
		this.name = projectName;
	}
}
