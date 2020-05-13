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

import com.google.gson.annotations.SerializedName;
import com.secdec.codedx.util.CodeDxVersion;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

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
	@SerializedName("~cwe")
	private String[] notCwe;
	@SerializedName("~finding")
	private String[] notFinding;
	@SerializedName("~path")
	private String[] notPath;
	@SerializedName("~rule")
	private String[] notRule;
	@SerializedName("~severity")
	private String[] notSeverity;
	@SerializedName("~status")
	private String[] notStatus;
	@SerializedName("~toolOverlap")
	private String[] notToolOverlap;

	public static class DateRange {
		// ISO time strings
		private String min;
		private String max;

		public DateRange(Date minDate, Date maxDate){
			SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ", Locale.ENGLISH);
			this.min = isoFormat.format(minDate);
			this.max = isoFormat.format(maxDate);
		}

		@Override
		public String toString() {
			return "DateRange[" + min + " to " + max + "]";
		}
	}
	private DateRange firstSeen;
	public DateRange getFirstSeen(){ return firstSeen; }
	public void setFirstSeen(DateRange firstSeen){ this.firstSeen = firstSeen; }


	public String[] getNotCwe() {
		return notCwe;
	}

	public void setNotCwe(String[] notCwe) {
		this.notCwe = notCwe;
	}

	public String[] getNotFinding() {
		return notFinding;
	}

	public void setNotFinding(String[] notFinding) {
		this.notFinding = notFinding;
	}

	public String[] getNotPath() {
		return notPath;
	}

	public void setNotPath(String[] notPath) {
		this.notPath = notPath;
	}

	public String[] getNotRule() {
		return notRule;
	}

	public void setNotRule(String[] notRule) {
		this.notRule = notRule;
	}

	public String[] getNotSeverity() {
		return notSeverity;
	}

	public void setNotSeverity(String[] notSeverity) {
		this.notSeverity = notSeverity;
	}

	public String[] getNotStatus() {
		return notStatus;
	}

	public void setNotStatus(String[] notStatus) {
		this.notStatus = notStatus;
	}

	public String[] getNotToolOverlap() {
		return notToolOverlap;
	}

	public void setNotToolOverlap(String[] notToolOverlap) {
		this.notToolOverlap = notToolOverlap;
	}

	/**
	 * Constant indicating the "New" status in Code Dx.
	 * Note that for Code Dx versions 2.4.0 and 2.4.1, the "New" status was removed.
	 * Implementations wishing to construct a filter with "New" as a criteria should
	 * check the server's version before doing so; if it is one of these versions,
	 * the implementation should use the "First Seen" filter instead.
	 * see CodeDxVersion#supportsTriageNew
	 */
	public static final String STATUS_NEW = "new";
	public static final String STATUS_ESCALATED = "escalated";
	public static final String STATUS_IGNORED = "ignored";
	public static final String STATUS_FALSE_POSITIVE = "false-positive";
	public static final String STATUS_FIXED = "fixed";
	public static final String STATUS_MITIGATED = "mitigated";
	public static final String STATUS_UNRESOLVED = "unresolved";
	public static final String STATUS_GONE = "gone";
	public static final String STATUS_ASSIGNED = "assigned";
	public static final String STATUS_REOPENED = "reopened";

	public static final String SEVERITY_INFO = "Info";
	public static final String SEVERITY_LOW = "Low";
	public static final String SEVERITY_MEDIUM = "Medium";
	public static final String SEVERITY_HIGH = "High";
	public static final String SEVERITY_CRITICAL = "Critical";
	public static final String SEVERITY_UNSPECIFIED = "Unspecified";

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
				+ Arrays.toString(status) + ", firstSeen=" + firstSeen
				+ ", toolOverlap=" + Arrays.toString(toolOverlap)
				+ ", ~cwe=" + Arrays.toString(notCwe) + ", ~finding="
				+ Arrays.toString(notFinding) + ", ~path=" + Arrays.toString(notPath)
				+ ", ~rule=" + Arrays.toString(notRule) + ", ~severity="
				+ Arrays.toString(notSeverity) + ", ~status="
				+ Arrays.toString(notStatus) + ", ~toolOverlap="
				+ Arrays.toString(notToolOverlap)
				+ "]";
	}

}
