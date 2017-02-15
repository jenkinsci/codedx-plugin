package org.jenkinsci.plugins.codedx.model;

import java.io.Serializable;

import org.kohsuke.stapler.export.Exported;

/**
 *
 * @author anthonyd
 *
 */
public class CodeDxGroupStatistics implements Serializable{

	/** Serial version UID. */
	private static final long serialVersionUID = 0L;


	private String group;
	private int findings;

	public CodeDxGroupStatistics(String group, int count) {

		this.group = group;
		this.findings = count;
	}

	@Exported(name="group")
	public String getGroup() {
		return group;
	}

	public void setSeverity(String severity) {
		this.group = severity;
	}

	@Exported(name="findings")
	public int getFindings() {
		return findings;
	}

	public void setFindings(int findings) {
		this.findings = findings;
	}

	@Override
	public String toString() {
		return "CodeDxSeverityStatistics [group=" + group + ", findings="
				+ findings + "]";
	}


}
