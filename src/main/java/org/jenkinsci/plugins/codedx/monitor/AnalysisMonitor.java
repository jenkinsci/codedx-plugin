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
package org.jenkinsci.plugins.codedx.monitor;

import com.codedx.api.client.CodeDxClient;
import com.codedx.api.client.CodeDxClientException;

import java.io.IOException;

public interface AnalysisMonitor {
	int waitForStart(CodeDxClient client) throws IOException, InterruptedException, CodeDxClientException;
	String waitForFinish(CodeDxClient client) throws IOException, InterruptedException, CodeDxClientException;
}
