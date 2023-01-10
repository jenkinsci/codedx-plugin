package org.jenkinsci.plugins.codedx;

import com.secdec.codedx.api.client.Branch;
import com.secdec.codedx.api.client.CodeDxClient;
import com.secdec.codedx.api.client.CodeDxClientException;
import com.secdec.codedx.util.CodeDxVersion;
import hudson.AbortException;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class TargetBranchChecker {
	final private int projectId;
	final private CodeDxClient client;
	final private ValueResolver resolver;
	final private PrintStream logger;

	private String targetBranchName, baseBranchName;

	public TargetBranchChecker(int projectId, CodeDxClient client, ValueResolver resolver, PrintStream logger) {
		this.projectId = projectId;
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
			this.targetBranchName = null;
			this.baseBranchName = null;
			return;
		}

		if (codedxVersion.compareTo(CodeDxVersion.MIN_FOR_BRANCHING) < 0) {
			throw new AbortException(
				"The connected Code Dx server with version " + codedxVersion + " does not support project branches. " +
				"The minimum required version is " + CodeDxVersion.MIN_FOR_BRANCHING + ". Remove " +
				"the target branch name or upgrade to a more recent version of Code Dx."
			);
		}

		this.targetBranchName = resolver.resolve("target branch", targetBranch);
		if (baseBranch != null) {
			this.baseBranchName = resolver.resolve("base branch", baseBranch);
		}

		logger.println("Validating base branch selection...");
		List<Branch> availableBranches;
		try {
			availableBranches = client.getProjectBranches(projectId);
		} catch (CodeDxClientException e) {
			throw new IOException("An error occurred when fetching available branches for project " + projectId, e);
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
		} if (!targetBranchExists) {
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
