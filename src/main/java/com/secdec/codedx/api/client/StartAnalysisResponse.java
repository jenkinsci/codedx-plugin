package com.secdec.codedx.api.client;

public class StartAnalysisResponse {

	private int runId;
	private String jobId;

	public int getRunId() {
		return runId;
	}
	public void setRunId(int runId) {
		this.runId = runId;
	}
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
}
