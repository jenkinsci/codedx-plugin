package com.codedx.api.client;

import java.time.Instant;

public class IncompleteWork {
	private String jobId;
	private String type;
	private int analysisId;
	private String creationTime;

	public String getJobId() { return jobId; }
	public void setJobId(String jobId) { this.jobId = jobId; }

	public Boolean isAnalysis() { return "analysis".equals(type); }
	public String getType() { return type; }
	public void setType(String type) { this.type = type; }

	public int getAnalysis() { return analysisId; }
	public void setAnalysis(int analysisId) { this.analysisId = analysisId; }

	public Instant getCreationTimeInstant() { return Instant.parse(creationTime); }
	public String getCreationTime() { return creationTime; }
	public void setCreationTime(String creationTime) { this.creationTime = creationTime; }
}
