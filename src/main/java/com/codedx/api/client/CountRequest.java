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

/**
 * Represents the request JSON data to get a count.
 *
 * @author anthonyd
 *
 */
public class CountRequest {

	private Filter filter;

	public CountRequest(){

	}

	public CountRequest(Filter filter){

		this.filter = filter;
	}

	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}
}
