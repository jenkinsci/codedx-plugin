/*
 * 
 * Copyright 2014 Applied Visions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 *  
 */

package com.secdec.codedx.api.client;

import java.util.Arrays;

/**
 * Represents the JSON data for a Filter
 * 
 * @author anthonyd
 *
 */
public class Filter {

	private String[] cwe;
	private String[] finding;
	private String[] path;
	private String[] rule;
	private String[] severity;
	private String[] status;
	private String[] toolOverlap;

	public static final String STATUS_NEW = "1";
	public static final String STATUS_ESCALATED = "2";
	public static final String STATUS_IGNORED = "3";
	public static final String STATUS_FALSE_POSITIVE = "4";
	public static final String STATUS_FIXED = "5";
	public static final String STATUS_UNRESOLVED = "6";
	public static final String STATUS_GONE = "7";
	
	public static final String SEVERITY_INFO = "Info";
	public static final String SEVERITY_LOW = "Low";
	public static final String SEVERITY_MEDIUM = "Medium";
	public static final String SEVERITY_HIGH = "High";
	
	public String[] getCwe() {
		return cwe;
	}
	public void setCwe(String[] cwe) {
		this.cwe = cwe;
	}
	public String[] getFinding() {
		return finding;
	}
	public void setFinding(String[] finding) {
		this.finding = finding;
	}
	public String[] getPath() {
		return path;
	}
	public void setPath(String[] path) {
		this.path = path;
	}
	public String[] getRule() {
		return rule;
	}
	public void setRule(String[] rule) {
		this.rule = rule;
	}
	public String[] getSeverity() {
		return severity;
	}
	public void setSeverity(String[] severity) {
		this.severity = severity;
	}
	public String[] getStatus() {
		return status;
	}
	public void setStatus(String[] status) {
		this.status = status;
	}
	public String[] getToolOverlap() {
		return toolOverlap;
	}
	public void setToolOverlap(String[] toolOverlap) {
		this.toolOverlap = toolOverlap;
	}

	@Override
	public String toString() {
		return "Filter [cwe=" + Arrays.toString(cwe) + ", finding="
				+ Arrays.toString(finding) + ", path=" + Arrays.toString(path)
				+ ", rule=" + Arrays.toString(rule) + ", severity="
				+ Arrays.toString(severity) + ", status="
				+ Arrays.toString(status) + ", toolOverlap="
				+ Arrays.toString(toolOverlap) + "]";
	}
	
}
