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

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Date;

import com.codedx.api.client.ProjectContext;
import com.codedx.util.CodeDxVersion;
import org.apache.http.client.ClientProtocolException;

import hudson.model.Result;

import com.codedx.api.client.CodeDxClient;
import com.codedx.api.client.CodeDxClientException;
import com.codedx.api.client.Filter;

/**
 * Used to to determine if a build should be considered unstable or a failure,
 * based on Code Dx analysis results.
 *
 * @author anthonyd
 *
 */
public class AnalysisResultChecker {

	private CodeDxClient client;
	private CodeDxVersion cdxVersion;
	private String failureSeverity;
	private String unstableSeverity;
	private Date newThreshold;
	private boolean failureOnlyNew;
	private boolean unstableOnlyNew;
	private BuildPolicyBehavior policyBehavior;
	private PrintStream logger;
	private ProjectContext project;

	public AnalysisResultChecker(CodeDxClient client, CodeDxVersion cdxVersion, String failureSeverity,
								 String unstableSeverity, Date newThreshold, boolean failureOnlyNew,
								 boolean unstableOnlyNew, BuildPolicyBehavior policyBehavior, ProjectContext project, PrintStream logger) {

		this.client = client;
		this.cdxVersion = cdxVersion;
		this.failureSeverity = failureSeverity;
		this.unstableSeverity = unstableSeverity;
		this.newThreshold = newThreshold;
		this.failureOnlyNew = failureOnlyNew;
		this.unstableOnlyNew = unstableOnlyNew;
		this.policyBehavior = policyBehavior;
		this.project = project;
		this.logger = logger;

		if (this.policyBehavior == null) {
			this.policyBehavior = BuildPolicyBehavior.Default;
		}

		if (this.policyBehavior != BuildPolicyBehavior.NoAction && cdxVersion.compareTo(CodeDxVersion.MIN_FOR_POLICIES) < 0) {
			logger.println(
				"The discovered Code Dx version " + cdxVersion.toString() + " is older than the minimum required " +
				"version for Policies (" + CodeDxVersion.MIN_FOR_POLICIES + "), policy-related options will be ignored."
			);
			this.policyBehavior = BuildPolicyBehavior.NoAction;
		}
	}

	public Result checkResult() throws ClientProtocolException, CodeDxClientException, IOException {
		if (policyBehavior != BuildPolicyBehavior.NoAction) {
			logger.println("Checking for build-breaking policy violations...");
			if (client.projectPolicyShouldBreakTheBuild(project)) {
				logger.println("Failure: At least one Policy is violated and set to 'Break build'");
				return policyBehavior.getEquivalentResult();
			}
		}

		logger.println("Checking for findings that indicate build failure...");
		if(!"None".equalsIgnoreCase(failureSeverity) && client.getFindingsCount(project, createFilter(failureSeverity, failureOnlyNew)) > 0){

			logger.println(String.format("Failure: Code Dx reported %s or higher severity issues.", failureSeverity));
			return Result.FAILURE;
		}

		logger.println("Checking for findings that indicate unstable build.");
		if(!"None".equalsIgnoreCase(unstableSeverity) && client.getFindingsCount(project, createFilter(unstableSeverity, unstableOnlyNew)) > 0){

			logger.println("Unstable!");
			return Result.UNSTABLE;
		}

		logger.println("CodeDx results indicate success!");

		return Result.SUCCESS;
	}

	private Filter createFilter(String minSeverity, boolean onlyNew){
		Filter filter = new Filter();
		filter.setSeverity(getSeverities(minSeverity));

		// ignore any findings that are "finished", by excluding certain statuses
		filter.setNotStatus(new String[]{
				Filter.STATUS_FALSE_POSITIVE,
				Filter.STATUS_FIXED,
				Filter.STATUS_GONE,
				Filter.STATUS_IGNORED,
				Filter.STATUS_MITIGATED
		});

		// if "onlyNew", we filter down to findings which were first seen *after* the `newThreshold` date.
		if(onlyNew){
			logger.println("Using the 'only consider new findings' option to decide build failure/instability.");
			if(cdxVersion.supportsTriageNew()){
				logger.println("Code Dx version is " + cdxVersion + ": the 'New' status is available for filtering.");
				filter.setStatus(new String[]{ Filter.STATUS_NEW });
			} else {
				logger.println("Code Dx version is " + cdxVersion + ": using 'firstSeen' filter to decide 'new' findings");
				filter.setFirstSeen(new Filter.DateRange(this.newThreshold, new Date()));
			}
		}

		logger.println("Using filter: " + filter.toString());

		return filter;
	}

	private String[] getSeverities(String minSeverity){
		String[] possibleSeverities = {
				Filter.SEVERITY_INFO,
				Filter.SEVERITY_LOW,
				Filter.SEVERITY_MEDIUM,
				Filter.SEVERITY_HIGH,
				Filter.SEVERITY_CRITICAL,
				Filter.SEVERITY_UNSPECIFIED
		};

		for(int i = 0; i < possibleSeverities.length; i++){
			if(possibleSeverities[i].equalsIgnoreCase(minSeverity)){
				return Arrays.copyOfRange(possibleSeverities, i, possibleSeverities.length);
			}
		}

		return new String[]{};
	}

}
