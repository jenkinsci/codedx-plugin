package org.jenkinsci.plugins.codedx;

public enum BuildEffectBehavior {
	MarkFailed("Mark Failed"),
	MarkUnstable("Mark Unstable"),
	None("Ignore");

	private String label;
	BuildEffectBehavior(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
