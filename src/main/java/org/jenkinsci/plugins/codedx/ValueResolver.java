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

import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;

import java.io.IOException;
import java.io.PrintStream;

public class ValueResolver {
	Run<?, ?> run;
	FilePath workspace;
	TaskListener listener;
	PrintStream logger;

	public ValueResolver(Run<?, ?> run, FilePath workspace, TaskListener listener, PrintStream logger) {
		this.run = run;
		this.workspace = workspace;
		this.listener = listener;
		this.logger = logger;
	}

	public String resolve(String label, String value) throws IOException, InterruptedException {
		try {
			logger.println("Expanding '" + label + "' value: " + value + " (raw)");
			return TokenMacro.expandAll(run, workspace, listener, value);
		} catch (MacroEvaluationException e) {
			logger.println("Macro expansion for '" + label + "' failed, falling back to default behavior");
			e.printStackTrace(logger);
			return run.getEnvironment(listener).expand(value);
		}
	}
}
