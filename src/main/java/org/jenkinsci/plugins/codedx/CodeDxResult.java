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

import java.io.Serializable;
import java.util.Map;

import hudson.model.AbstractBuild;

import hudson.model.Run;
import org.jenkinsci.plugins.codedx.model.CodeDxReportStatistics;

/**
 *
 * @author ademartini This file is heavily derived from the sloccount-plugin (author: lordofthepigs)
 *
 */
public class CodeDxResult {

	/** Serial version UID. */
	private static final long serialVersionUID = 0L;

	private final Run<?,?> owner;

	private Map<String, CodeDxReportStatistics> statisticsMap;

	public CodeDxResult(Map<String,CodeDxReportStatistics> statisticsMap, Run<?,?> owner){

		this.owner = owner;
		this.statisticsMap = statisticsMap;
	}

	public Run<?,?> getOwner() {
		return owner;
	}

	public CodeDxReportStatistics getStatistics(String name) {

		return statisticsMap.get(name);
	}

	public Map<String, CodeDxReportStatistics> getStatisticsMap() {

		return statisticsMap;
	}

	public boolean isEmpty(){

		for(CodeDxReportStatistics stats: statisticsMap.values()){

			if(stats.getFindings() > 0){

				return false;
			}
		}

		return true;
	}
}
