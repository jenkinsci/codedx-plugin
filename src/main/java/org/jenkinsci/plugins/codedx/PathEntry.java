/*
 * 
 * Copyright 2014 Applied Visions
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

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

import java.io.Serializable;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Represents a Jenkins CodeDx plugin path configuration option.  One of these
 * will be created for each binary path filter, source path filter, and tool output path.
 * @author anthonyd
 *
 */
public class PathEntry extends AbstractDescribableImpl<PathEntry> implements Serializable{

	private final String value;

	@DataBoundConstructor
	public PathEntry(final String value){
		
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	/**
	 * This is added to prevent Jenkins complaining exception.
	 * @author anthonyd
	 *
	 */
    @Extension
    public static class DescriptorImpl extends Descriptor<PathEntry> {
        public String getDisplayName() { return ""; }
    }
}
