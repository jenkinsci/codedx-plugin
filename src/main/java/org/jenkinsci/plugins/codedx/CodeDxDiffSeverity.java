package org.jenkinsci.plugins.codedx;

/**
 * 
 * @author anthonyd
 *
 */
public class CodeDxDiffSeverity extends CodeDxDiff{

	private final String name;

	public CodeDxDiffSeverity(String name, int findings, int findingsDelta) {
		super(findings, findingsDelta);
		// TODO Auto-generated constructor stub
		
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
