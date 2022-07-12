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

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Contains the plugin configuration options that are applicable when the user
 * chooses to have Jenkins wait for analysis runs to complete.
 * @author anthonyd
 *
 */
public class AnalysisResultConfiguration {

	private String failureSeverity;
	private String unstableSeverity;
	private boolean failureOnlyNew;
	private boolean unstableOnlyNew;
	private int numBuildsInGraph;
	
	@DataBoundConstructor
	public AnalysisResultConfiguration(String failureSeverity,
			String unstableSeverity, boolean failureOnlyNew,
			boolean unstableOnlyNew, int numBuildsInGraph) {
	
		this.failureSeverity = failureSeverity;
		this.unstableSeverity = unstableSeverity;
		this.failureOnlyNew = failureOnlyNew;
		this.unstableOnlyNew = unstableOnlyNew;
		this.numBuildsInGraph = numBuildsInGraph;
	}
	public String getFailureSeverity() {
		return failureSeverity;
	}
	public void setFailureSeverity(String failureSeverity) {
		this.failureSeverity = failureSeverity;
	}
	public String getUnstableSeverity() {
		return unstableSeverity;
	}
	public void setUnstableSeverity(String unstableSeverity) {
		this.unstableSeverity = unstableSeverity;
	}
	public boolean isFailureOnlyNew() {
		return failureOnlyNew;
	}
	public void setFailureOnlyNew(boolean failureOnlyNew) {
		this.failureOnlyNew = failureOnlyNew;
	}
	public boolean isUnstableOnlyNew() {
		return unstableOnlyNew;
	}
	public void setUnstableOnlyNew(boolean unstableOnlyNew) {
		this.unstableOnlyNew = unstableOnlyNew;
	}
	public int getNumBuildsInGraph() {
		return numBuildsInGraph;
	}
	public void setNumBuildsInGraph(int numBuildsInGraph) {
		this.numBuildsInGraph = numBuildsInGraph;
	}
}
