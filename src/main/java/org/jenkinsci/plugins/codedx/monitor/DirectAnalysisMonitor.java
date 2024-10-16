/*
 * © 2024 Black Duck Software, Inc. All rights reserved worldwide.
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
package org.jenkinsci.plugins.codedx.monitor;

import com.codedx.api.client.CodeDxClient;
import com.codedx.api.client.CodeDxClientException;
import com.codedx.api.client.Job;
import com.codedx.api.client.StartAnalysisResponse;
import com.codedx.util.CodeDxVersion;

import java.io.IOException;
import java.io.PrintStream;

public class DirectAnalysisMonitor implements AnalysisMonitor {
	StartAnalysisResponse analysisResponse;
	PrintStream logger;

	public DirectAnalysisMonitor(StartAnalysisResponse analysisResponse, PrintStream logger) {
		this.analysisResponse = analysisResponse;
		this.logger = logger;
	}

	// returns analysis ID
	public int waitForStart(CodeDxClient client) {
		return analysisResponse.getAnalysisId();
	}

	// returns job status
	public String waitForFinish(CodeDxClient client) throws IOException, InterruptedException, CodeDxClientException {
		String status = null;
		String oldStatus = null;

		do {
			Thread.sleep(3000);
			oldStatus = status;
			status = client.getJobStatus(analysisResponse.getJobId());

			if (status != null && !status.equals(oldStatus)) {
				if (Job.QUEUED.equals(status)) {
					logger.println("Code Dx analysis is queued");
				} else if (Job.RUNNING.equals(status)) {
					logger.println("Code Dx analysis is running");
				}
			}
		} while (Job.QUEUED.equals(status) || Job.RUNNING.equals(status));

		return status;
	}
}
