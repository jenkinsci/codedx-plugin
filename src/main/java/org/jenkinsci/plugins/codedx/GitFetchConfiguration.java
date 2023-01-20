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

import org.kohsuke.stapler.DataBoundConstructor;

public class GitFetchConfiguration {
	private String specificBranch;

	@DataBoundConstructor
	public GitFetchConfiguration(String specificBranch) {
		this.specificBranch = specificBranch;
	}

	public String getSpecificBranch() {
		if (specificBranch != null && specificBranch.trim().length() > 0) {
			return specificBranch;
		} else {
			return null;
		}
	}

	public void setSpecificBranch(String branch) {
		specificBranch = branch;
	}
}
