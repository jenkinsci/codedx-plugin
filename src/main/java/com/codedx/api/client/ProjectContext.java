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
package com.codedx.api.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

public class ProjectContext {
	int projectId;
	String branchName;

	String contextString;

	public ProjectContext(int projectId) {
		this(projectId, null);
	}

	public ProjectContext(int projectId, String branchName) {
		this.projectId = projectId;
		this.branchName = branchName;

		this.contextString = Integer.toString(projectId);
		if (branchName != null) {
			try {
				this.contextString += ";branch=" + URLEncoder.encode(branchName, Charset.defaultCharset().name());
			} catch (UnsupportedEncodingException ignored) {
				// using platform-reported default encoding
			}
		}
	}

	public int getProjectId() {
		return projectId;
	}

	public String getBranchName() {
		return branchName;
	}

	public ProjectContext withBranch(String branchName) {
		return new ProjectContext(projectId, branchName);
	}

	@Override
	public String toString() {
		return contextString;
	}
}
