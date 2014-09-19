package org.jenkinsci.plugins.codedx.client;

import java.util.List;

public class GetProjectsResponse {

	private List<Project> projects;

	public List<Project> getProjects() {
		return projects;
	}

	public void setProjects(List<Project> projects) {
		this.projects = projects;
	}
}
