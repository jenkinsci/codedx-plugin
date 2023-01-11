package com.codedx.api.client;

public class AnalysisInputInfo {
	private int id;
	private String name;

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public Boolean isFromGitSource() { return "git source".equals(this.name); }
}
