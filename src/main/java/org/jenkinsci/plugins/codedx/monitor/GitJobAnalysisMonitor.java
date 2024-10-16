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

import com.codedx.api.client.*;
import hudson.AbortException;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class GitJobAnalysisMonitor implements AnalysisMonitor {
	StartAnalysisResponse originalAnalysisResponse, newAnalysisResponse;
	PrintStream logger;

	public GitJobAnalysisMonitor(
		StartAnalysisResponse originalAnalysisResponse,
		PrintStream logger
	) {
		this.originalAnalysisResponse = originalAnalysisResponse;
		this.logger = logger;
	}

	public int waitForStart(CodeDxClient client) throws InterruptedException, IOException, CodeDxClientException {
		String gitJobId = originalAnalysisResponse.getJobId();
		boolean isRunning = true;
		logger.println("Monitoring git clone job...");

		do {
			Thread.sleep(3000);
			String status = client.getJobStatus(gitJobId);
			if (status != null) {
				if (Job.QUEUED.equals(status) || Job.RUNNING.equals(status)) {
					logger.println("Git clone status is " + status);
					continue;
				}

				if (Job.FAILED.equals(status)) {
					throw new AbortException("Fatal Error! The Code Dx git clone job failed.");
				}

				isRunning = false;
			}
		} while (isRunning);

		logger.println("Git clone job completed");

		newAnalysisResponse = client.getGitJobResult(gitJobId);
		return newAnalysisResponse.getAnalysisId();
	}

	public String waitForFinish(CodeDxClient client) throws IOException, InterruptedException, CodeDxClientException {
		DirectAnalysisMonitor monitor = new DirectAnalysisMonitor(newAnalysisResponse, logger);
		return monitor.waitForFinish(client);
	}
}
