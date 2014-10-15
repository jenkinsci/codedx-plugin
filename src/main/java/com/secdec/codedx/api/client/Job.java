package com.secdec.codedx.api.client;

public class Job {

	private String jobId;
	private String status;
	
	public static final String QUEUED = "queued";
	public static final String RUNNING = "running";
	public static final String COMPLETED = "completed";
	public static final String FAILED = "failed";
	
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
}
