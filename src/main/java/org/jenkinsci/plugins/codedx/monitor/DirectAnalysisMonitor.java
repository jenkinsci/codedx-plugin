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
	public int waitForStart(CodeDxClient client) throws IOException, InterruptedException {
		return analysisResponse.getAnalysisId();
	}

	// returns job status
	public String waitForFinish(CodeDxClient client) throws IOException, InterruptedException {
		String status = null;
		String oldStatus = null;
		try {
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
		} catch (CodeDxClientException e) {
			throw new IOException("Fatal Error! There was a problem querying for the analysis status.", e);
		}

		return status;
	}
}
