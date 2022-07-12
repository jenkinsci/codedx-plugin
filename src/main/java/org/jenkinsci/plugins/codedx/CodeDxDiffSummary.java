/*
 *
 * Copyright 2022 Synopsys, Inc
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

package org.jenkinsci.plugins.codedx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jenkinsci.plugins.codedx.model.CodeDxReportStatistics;
import org.jenkinsci.plugins.codedx.model.CodeDxGroupStatistics;

/**
 *
 * @author ademartini This file is heavily derived from the sloccount-plugin (author: Michal Turek)
 *
 */
public class CodeDxDiffSummary extends CodeDxDiff{

	private final List<CodeDxDiffGroup> groupDiffs;
	private String name;

	public CodeDxDiffSummary(List<CodeDxDiffGroup> groupDiffs, int findings, int findingsDelta, String name) {
		super(findings, findingsDelta);
		this.groupDiffs = groupDiffs;
		this.name = name;
		// TODO Auto-generated constructor stub
	}


	public List<CodeDxDiffGroup> getGroupDiffs() {
		return groupDiffs;
	}


	public String getName(){

		return name;
	}

	public static CodeDxDiffSummary getDiffSummary(
			CodeDxReportStatistics previous,
			CodeDxReportStatistics current,
			String name,
			Comparator<CodeDxDiffGroup> comparator,
			Map<String,String> iconMap) {

		if(previous == null) {
			return getDiffSummary(current,name,comparator,iconMap);
		}

		Set<String> groups = new HashSet<String>();
		groups.addAll(previous.getAllGroups());
		groups.addAll(current.getAllGroups());

		List<CodeDxDiffGroup> result = new ArrayList<CodeDxDiffGroup>();
		int findings = 0;
		int findingsDelta = 0;

		for(String group: groups) {
			// Quadratic complexity can be optimized, but groups count is small
			CodeDxGroupStatistics curStats = current.getGroup(group);
			CodeDxGroupStatistics prevStats = previous.getGroup(group);

			result.add(new CodeDxDiffGroup(curStats.getGroup(),
					curStats.getFindings(),
					curStats.getFindings() - prevStats.getFindings(),iconMap.get(curStats.getGroup())));

			findings += curStats.getFindings();
			findingsDelta += curStats.getFindings() - prevStats.getFindings();
		}

		Collections.sort(result, comparator);
		return new CodeDxDiffSummary(result, findings,findingsDelta, name);
	}

	private static CodeDxDiffSummary getDiffSummary(
			CodeDxReportStatistics current,
			String name,
			Comparator<CodeDxDiffGroup> comparator,
			Map<String,String> iconMap) {

		if(current == null) {
			return getDiffSummary(name);
		}

		List<CodeDxDiffGroup> result = new ArrayList<CodeDxDiffGroup>();
		int findings = 0;

		for(CodeDxGroupStatistics groupStats: current.getStatistics()) {
			result.add(new CodeDxDiffGroup(groupStats.getGroup(),
					groupStats.getFindings(), 0,iconMap.get(groupStats.getGroup())));

			findings += groupStats.getFindings();
		}

		Collections.sort(result,comparator);
		return new CodeDxDiffSummary(result, findings, 0, name);
	}


	private static CodeDxDiffSummary getDiffSummary(String name) {
		return new CodeDxDiffSummary(
				Collections.<CodeDxDiffGroup>emptyList(), 0, 0, name);
	}
}
