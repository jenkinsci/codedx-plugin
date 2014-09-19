package org.jenkinsci.plugins.codedx.client;

import java.io.InputStream;

public interface AnalysisArtifact {

	public InputStream createInputStream();
}
