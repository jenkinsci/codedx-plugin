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
package com.codedx.api.client;

/**
 * Represents the JSON request data to get grouped counts
 * 
 * @author anthonyd
 *
 */
public class GroupedCountRequest extends CountRequest{

	public GroupedCountRequest() {

	}

	public GroupedCountRequest(Filter filter, String countBy) {
		super(filter);
		this.countBy = countBy;
	}

	private String countBy;

	public String getCountBy() {
		return countBy;
	}

	public void setCountBy(String countBy) {
		this.countBy = countBy;
	}
}
