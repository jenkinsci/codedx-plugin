/*
 * © 2024 Black Duck Software, Inc. All rights reserved worldwide.
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
package org.jenkinsci.plugins.codedx;

/**
 *
 * @author ademartini This file is heavily derived from the sloccount-plugin (author: Michal Turek)
 *
 */
public class CodeDxDiff implements Comparable<CodeDxDiff>{

	private final int findings;
	private final int findingsDelta;

	public CodeDxDiff(int findings, int findingsDelta) {

		this.findings = findings;
		this.findingsDelta = findingsDelta;
	}

	public int getFindings() {
		return findings;
	}
	public int getFindingsDelta() {
		return findingsDelta;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + findings;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CodeDxDiff other = (CodeDxDiff) obj;
		if (findings != other.findings)
			return false;
		return true;
	}

	public int compareTo(CodeDxDiff o){

		return o.findings - findings;
	}


	public String getFindingsString() {
		return String.format("%,d", findings);
	}

	public String getFindingsDeltaString() {
		if(findingsDelta == 0) {
			return "";
		}

		// Negative prefix '-' is added automatically
		String result = String.format("%,d", findingsDelta);
		return (findingsDelta > 0) ? "+" + result : result;
	}
}
