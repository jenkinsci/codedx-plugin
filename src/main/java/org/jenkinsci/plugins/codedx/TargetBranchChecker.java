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
package org.jenkinsci.plugins.codedx;

import com.codedx.api.client.Branch;
import com.codedx.api.client.CodeDxClient;
import com.codedx.api.client.CodeDxClientException;
import com.codedx.api.client.ProjectContext;
import com.codedx.util.CodeDxVersion;
import hudson.AbortException;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class TargetBranchChecker {
	final private ProjectContext project;
	final private CodeDxClient client;
	final private ValueResolver resolver;
	final private PrintStream logger;

	private String targetBranchName, baseBranchName;

	public TargetBranchChecker(ProjectContext project, CodeDxClient client, ValueResolver resolver, PrintStream logger) {
		this.project = project;
		this.client = client;
		this.resolver = resolver;
		this.logger = logger;

		this.targetBranchName = this.baseBranchName = null;
	}

	public String getTargetBranchName() {
		return this.targetBranchName;
	}

	public String getBaseBranchName() {
		return this.baseBranchName;
	}

	public void validate(CodeDxVersion codedxVersion, String targetBranch, String baseBranch) throws IOException, InterruptedException {
		if (targetBranch == null) {
			// no target branch, nothing branch-related to do here
			return;
		}

		if (codedxVersion.compareTo(CodeDxVersion.MIN_FOR_BRANCHING) < 0) {
			logger.println(
				"The connected Code Dx server with version " + codedxVersion + " does not support project branches. " +
				"The minimum required version is " + CodeDxVersion.MIN_FOR_BRANCHING + ". The target branch and base " +
				"branch options will be ignored."
			);
			return;
		}

		this.targetBranchName = resolver.resolve("target branch", targetBranch);
		if (baseBranch != null) {
			this.baseBranchName = resolver.resolve("base branch", baseBranch);
		}

		logger.println("Validating base branch selection...");
		List<Branch> availableBranches;
		try {
			availableBranches = client.getProjectBranches(project);
		} catch (CodeDxClientException e) {
			throw new IOException("An error occurred when fetching available branches for project " + project.getProjectId(), e);
		}

		boolean targetBranchExists = false;
		boolean baseBranchExists = false;
		for (Branch branch : availableBranches) {
			if (branch.getName().equals(this.baseBranchName)) {
				baseBranchExists = true;
			} else if (branch.getName().equals(this.targetBranchName)) {
				targetBranchExists = true;
			}
		}
		if (targetBranchExists) {
			logger.println("Using existing Code Dx branch: " + this.targetBranchName);
			// not necessary, base branch is currently ignored in the backend if the target
			// branch already exists. just setting to null to safeguard in case of future changes
			this.baseBranchName = null;
		} else {
			if (this.baseBranchName == null) {
				throw new AbortException("A parent branch must be specified when using a target branch");
			}

			if (!baseBranchExists) {
				throw new AbortException("The specified parent branch does not exist: " + this.baseBranchName);
			}

			logger.println(
				"Analysis will create a new branch named '" +
				this.targetBranchName + "' based on the branch '" + this.baseBranchName + "'"
			);
		}
	}
}
