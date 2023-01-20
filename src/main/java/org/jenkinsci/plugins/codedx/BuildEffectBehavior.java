package org.jenkinsci.plugins.codedx;

import hudson.model.Result;

public enum BuildEffectBehavior {
	MarkFailed("Mark Build as Failed", Result.FAILURE),
	MarkUnstable("Mark Build as Unstable", Result.UNSTABLE),
	None("Ignore Errors", Result.SUCCESS);

	private String label;
	private Result equivalentResult;
	BuildEffectBehavior(String label, Result equivalentResult) {
		this.label = label;
		this.equivalentResult = equivalentResult;
	}

	public String getLabel() {
		return label;
	}
	public Result getEquivalentResult() {
		return equivalentResult;
	}
}
