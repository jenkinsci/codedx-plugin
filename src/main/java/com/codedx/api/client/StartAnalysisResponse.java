/*
 * © 2023 Synopsys, Inc. All rights reserved worldwide.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package com.codedx.api.client;

/**
 * Represents the JSON response data when starting an analysis run.
 *
 * @author anthonyd
 *
 */
public class StartAnalysisResponse {

	private int analysisId;
	private String jobId;

	StartAnalysisResponse() {
		analysisId = -1;
		jobId = null;
	}

	public Boolean hasAnalysisId() {
		return analysisId != -1;
	}

	public int getAnalysisId(){ return analysisId; }
	public void setAnalysisId(int analysisId) { this.analysisId = analysisId; }

	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
}
