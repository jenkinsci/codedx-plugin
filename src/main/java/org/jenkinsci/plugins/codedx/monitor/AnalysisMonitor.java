package org.jenkinsci.plugins.codedx.monitor;

import com.codedx.api.client.CodeDxClient;

import java.io.IOException;

public interface AnalysisMonitor {
	int waitForStart(CodeDxClient client) throws IOException, InterruptedException;
	String waitForFinish(CodeDxClient client) throws IOException, InterruptedException;
}
