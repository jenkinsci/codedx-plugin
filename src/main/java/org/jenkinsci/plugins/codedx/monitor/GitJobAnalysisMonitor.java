package org.jenkinsci.plugins.codedx.monitor;

import com.codedx.api.client.*;
import hudson.AbortException;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class GitJobAnalysisMonitor implements AnalysisMonitor {
	ProjectContext project;
	StartAnalysisResponse originalAnalysisResponse, newAnalysisResponse;
	PrintStream logger;

	public GitJobAnalysisMonitor(
		ProjectContext project,
		StartAnalysisResponse originalAnalysisResponse,
		PrintStream logger
	) {
		this.project = project;
		this.originalAnalysisResponse = originalAnalysisResponse;
		this.logger = logger;
	}

	public int waitForStart(CodeDxClient client) throws InterruptedException, IOException {
		String gitJobId = originalAnalysisResponse.getJobId();
		boolean isRunning = true;
		logger.println("Monitoring git clone job...");
		try {
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

			newAnalysisResponse = client.getGitJobResult(gitJobId);
		} catch (CodeDxClientException e) {
			throw new IOException("Fatal Error! There was a problem querying for the Git clone job status.", e);
		}

		logger.println("Git clone job completed");

		return newAnalysisResponse.getAnalysisId();
	}

	public String waitForFinish(CodeDxClient client) throws IOException, InterruptedException {
		DirectAnalysisMonitor monitor = new DirectAnalysisMonitor(newAnalysisResponse, logger);
		return monitor.waitForFinish(client);
	}
}
