package org.jenkinsci.plugins.codedx;

/**
 * 
 * @author ademartini This file is heavily derived from the sloccount-plugin (author: Michal Turek)
 *
 */
public class CodeDxDiffGroup extends CodeDxDiff{

	private final String name;

	public CodeDxDiffGroup(String name, int findings, int findingsDelta) {
		super(findings, findingsDelta);
		// TODO Auto-generated constructor stub
		
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
