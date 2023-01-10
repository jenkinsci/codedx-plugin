/*
 * © 2023 Synopsys, Inc. All rights reserved worldwide.
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
 */
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
