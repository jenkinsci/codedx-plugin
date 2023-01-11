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
import java.util.LinkedList;
import java.util.List;

import org.kohsuke.stapler.export.Exported;

/**
 *
 * @author anthonyd
 *
 */
public class CodeDxReportStatistics implements Serializable{

	/** Serial version UID. */
	private static final long serialVersionUID = 0L;

	private List<CodeDxGroupStatistics> statistics;

	public CodeDxReportStatistics(List<CodeDxGroupStatistics> statistics) {

		System.out.println("Statistics is: " + statistics);
		this.statistics = statistics;
	}

	@Exported(name="groups")
	public List<CodeDxGroupStatistics> getStatistics() {
		return statistics;
	}

	@Exported(name="totalFindings")
	public int getFindings() {
		int findings = 0;

		for(CodeDxGroupStatistics it : statistics) {
			findings += it.getFindings();
		}

		return findings;
	}

	public List<String> getAllGroups() {
		List<String> groups = new LinkedList<String>();

		for(CodeDxGroupStatistics it : statistics) {
			groups.add(it.getGroup());
		}

		return groups;
	}

	public CodeDxGroupStatistics getGroup(String group) {
		for(CodeDxGroupStatistics it : statistics) {
			if(it.getGroup().equals(group)) {
				return it;
			}
		}

		return new CodeDxGroupStatistics(group, 0);
	}
}
