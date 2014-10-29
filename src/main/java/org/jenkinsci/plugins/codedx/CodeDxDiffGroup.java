package org.jenkinsci.plugins.codedx;

/**
 * 
 * @author ademartini This file is heavily derived from the sloccount-plugin (author: Michal Turek)
 *
 */
public class CodeDxDiffGroup extends CodeDxDiff{

	private final String name;
	private String icon;

	public CodeDxDiffGroup(String name, int findings, int findingsDelta, String icon) {
		super(findings, findingsDelta);
		// TODO Auto-generated constructor stub
		
		this.name = name;
		this.icon = icon;
	}

	public String getName() {
		return name;
	}

	public String getIcon() {
		return icon;
	}
}
