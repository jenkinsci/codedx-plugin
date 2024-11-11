package org.jenkinsci.plugins.codedx;

import com.codedx.api.client.CodeDxClient;
import com.codedx.api.client.CodeDxClientException;
import com.codedx.api.client.Project;
import hudson.AbortException;

import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

public class ProjectResolver {
	PrintStream log;
	CodeDxClient client;

	public ProjectResolver(PrintStream log, CodeDxClient client) {
		this.log = log;
		this.client = client;
	}

	private int resolveSpecificProject(CodeDxPublisher.SpecificProject selection) throws IOException, CodeDxClientException {
		try {
			log.println("Using Specific Project ID");
			return Integer.parseInt(selection.getProjectId());
		} catch (NumberFormatException e) {
			throw new AbortException("Invalid project ID: " + selection.getProjectId());
		}
	}

	private int resolveNamedProject(CodeDxPublisher.NamedProject selection, String newProjectDefaultBranch) throws IOException, CodeDxClientException {
		log.println("Using Named Project");

		String projectName = selection.getProjectName();

		if (projectName == null || projectName.trim().isEmpty()) {
			throw new AbortException("Project name was not specified.");
		}

		List<Project> matches = new LinkedList<>();

		log.println("Fetching list of projects");
		for (Project project : client.getProjects()) {
			if (project.getName().equals(projectName)) {
				matches.add(project);
			}
		}
		log.println(String.format("Found %d total projects", matches.size()));

		switch (matches.size()) {
			case 0:
				log.println("Did not find any matching projects");
				if (selection.isAutoCreate()) {
					log.println("Auto-create is enabled, creating project with default branch");
					return client.createProject(projectName, newProjectDefaultBranch).getId();
				} else {
					log.println("Auto-create is NOT enabled");
				}
				break;

			case 1:
				return matches.get(0).getId();
		}

		throw new AbortException(String.format("Expected to find 1 project named '%s', but found %d.", projectName, matches.size()));
	}

	public int resolveProjectId(CodeDxPublisher.ProjectSelection selection, String newProjectDefaultBranch) throws IOException, CodeDxClientException {
		if (selection == null) {
			throw new NullPointerException("Project selection was 'null'");
		}
		else if (selection instanceof CodeDxPublisher.SpecificProject) {
			return resolveSpecificProject((CodeDxPublisher.SpecificProject) selection);
		}
		else if (selection instanceof CodeDxPublisher.NamedProject) {
			return resolveNamedProject((CodeDxPublisher.NamedProject) selection, newProjectDefaultBranch);
		} else {
			throw new UnsupportedOperationException("Unexpected project selection type");
		}
	}
}
