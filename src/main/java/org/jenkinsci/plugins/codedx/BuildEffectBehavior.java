package org.jenkinsci.plugins.codedx;

public enum BuildEffectBehavior {
	MarkFailed("Mark Build as Failed"),
	MarkUnstable("Mark Build as Unstable"),
	None("Ignore Errors");

	private String label;
	BuildEffectBehavior(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
