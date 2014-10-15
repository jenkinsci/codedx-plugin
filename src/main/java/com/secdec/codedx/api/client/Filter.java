package com.secdec.codedx.api.client;

public class Filter {

	private String[] cwe;
	private String[] finding;
	private String[] path;
	private String[] rule;
	private String[] severity;
	private String[] status;
	private String[] toolOverlap;
	
	public String[] getCwe() {
		return cwe;
	}
	public void setCwe(String[] cwe) {
		this.cwe = cwe;
	}
	public String[] getFinding() {
		return finding;
	}
	public void setFinding(String[] finding) {
		this.finding = finding;
	}
	public String[] getPath() {
		return path;
	}
	public void setPath(String[] path) {
		this.path = path;
	}
	public String[] getRule() {
		return rule;
	}
	public void setRule(String[] rule) {
		this.rule = rule;
	}
	public String[] getSeverity() {
		return severity;
	}
	public void setSeverity(String[] severity) {
		this.severity = severity;
	}
	public String[] getStatus() {
		return status;
	}
	public void setStatus(String[] status) {
		this.status = status;
	}
	public String[] getToolOverlap() {
		return toolOverlap;
	}
	public void setToolOverlap(String[] toolOverlap) {
		this.toolOverlap = toolOverlap;
	}

	
}
