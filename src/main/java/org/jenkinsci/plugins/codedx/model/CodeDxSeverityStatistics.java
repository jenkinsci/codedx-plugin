package org.jenkinsci.plugins.codedx.model;

import java.io.Serializable;

import org.kohsuke.stapler.export.Exported;

/**
 * 
 * @author anthonyd
 *
 */
public class CodeDxSeverityStatistics implements Serializable{
	
    /** Serial version UID. */
    private static final long serialVersionUID = 0L;

    
	private String severity;
	private int findings;
	
	public CodeDxSeverityStatistics(String severity, int count) {

		this.severity = severity;
		this.findings = count;
	}
	
	@Exported(name="severity")
	public String getSeverity() {
		return severity;
	}
	
	public void setSeverity(String severity) {
		this.severity = severity;
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
		return "CodeDxSeverityStatistics [severity=" + severity + ", findings="
				+ findings + "]";
	}
	
	
}
