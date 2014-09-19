package org.jenkinsci.plugins.codedx.client;

import java.util.List;

public class Project {


	private int id;
	private String name;
	private List<AnalysisRun> analysisRuns;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public List<AnalysisRun> getAnalysisRuns() {
		return analysisRuns;
	}
	public void setAnalysisRuns(List<AnalysisRun> analysisRuns) {
		this.analysisRuns = analysisRuns;
	}

}
