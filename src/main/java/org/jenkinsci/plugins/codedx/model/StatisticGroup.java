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

import java.util.HashSet;
import java.util.Set;

public class StatisticGroup {
	public static final String Unspecified = "Unspecified";
	public static final String Info = "Info";
	public static final String Low = "Low";
	public static final String Medium = "Medium";
	public static final String High = "High";
	public static final String Critical = "Critical";

	public static final String Gone = "Gone";
	public static final String New = "New";
	public static final String Assigned = "Assigned";
	public static final String Escalated = "Escalated";
	public static final String Unresolved = "Unresolved";
	public static final String FalsePositive = "False Positive";
	public static final String Ignored = "Ignored";
	public static final String Mitigated = "Mitigated";
	public static final String Fixed = "Fixed";
	public static final String Reopened = "Reopened";

	public static Set<String> valuesForStatistic(String statisticName) {
		Set<String> values = new HashSet<String>();
		if ("severity".equals(statisticName)) {
			values.add(Unspecified);
			values.add(Info);
			values.add(Low);
			values.add(Medium);
			values.add(High);
			values.add(Critical);
		} else if ("status".equals(statisticName)) {
			values.add(Fixed);
			values.add(Mitigated);
			values.add(Ignored);
			values.add(FalsePositive);
			values.add(Unresolved);
			values.add(Escalated);
			values.add(Assigned);
			values.add(New);
			values.add(Reopened);
		}

		return values;
	}
}
