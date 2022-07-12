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

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Run;
import hudson.util.Area;
import hudson.util.ChartUtil;

import java.awt.Color;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import hudson.util.Graph;
import org.jenkinsci.plugins.codedx.model.StatisticGroup;
import org.jfree.chart.JFreeChart;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.secdec.codedx.api.client.Filter;

/**
 *
 * @author ademartini This file is heavily derived from the sloccount-plugin (author: lordofthepigs)
 */
public class CodeDxProjectAction implements Action {

	public static final String URL_NAME = "codedxResult";

	public static final int CHART_WIDTH = 500;
	public static final int CHART_HEIGHT = 200;

	private final Run<?, ?> run;

	private final String latestAnalysisUrl;

	private AnalysisResultConfiguration analysisResultConfiguration;

	public CodeDxProjectAction(final Run<?, ?> run, AnalysisResultConfiguration analysisResultConfiguration, String latestAnalysisUrl) {
		this.run = run;
		this.analysisResultConfiguration = analysisResultConfiguration;
		this.latestAnalysisUrl = latestAnalysisUrl;
	}

	public String getIconFileName() {
		return null;
	}

	public String getDisplayName() {
		return null;
	}

	public String getUrlName() {
		return URL_NAME;
	}

	public String getLatestAnalysisUrl(){

		return latestAnalysisUrl;
	}

	public AnalysisResultConfiguration getAnalysisResultConfiguration(){

		return analysisResultConfiguration;
	}

	public boolean showTablesAndCharts(){

		return analysisResultConfiguration != null;
	}
	/**
	 *
	 * Redirects the index page to the last result.
	 *
	 * @param request Stapler request
	 * @param response Stapler response
	 * @throws IOException in case of an error
	 */
	public void doIndex(final StaplerRequest request, final StaplerResponse response) throws IOException {
		Run<?, ?> build = getLastFinishedBuild();
		if (build != null) {
			response.sendRedirect2(String.format("../%d/%s", build.getNumber(), CodeDxBuildAction.URL_NAME));
		}else{
			// Click to the link in menu on the job page before the first build
			response.sendRedirect2("..");
		}
	}

	/**
	 * Returns the last finished build.
	 *
	 * @return the last finished build or <code>null</code> if there is no
	 *         such build
	 */
	public Run<?, ?> getLastFinishedBuild() {
		Run<?, ?> lastBuild = run;
		while (lastBuild != null && (lastBuild.isBuilding() || lastBuild.getAction(CodeDxBuildAction.class) == null)) {
			lastBuild = lastBuild.getPreviousBuild();
		}
		return lastBuild;
	}

	/**
	 * Get build action of the last finished build.
	 *
	 * @return the build action or null
	 */
	public CodeDxBuildAction getLastFinishedBuildAction() {
		Run<?, ?> lastBuild = getLastFinishedBuild();
		return (lastBuild != null) ? lastBuild.getAction(CodeDxBuildAction.class) : null;
	}

	public final boolean hasValidResults() {
		Run<?, ?> build = getLastFinishedBuild();

		if (build != null) {
			CodeDxBuildAction resultAction = build.getAction(CodeDxBuildAction.class);

			int nbr_results = 0;

			while(resultAction != null){
				CodeDxResult result = resultAction.getResult();

				if(result != null){
					nbr_results++;

					if(nbr_results > 1){
						return true;
					}
				}

				resultAction = resultAction.getPreviousAction();
			}
		}

		return false;
	}

	/**
	 * Display the severity trend graph.
	 *
	 * @param request Stapler request
	 * @param response Stapler response
	 * @throws IOException in case of an error
	 */
	public void doSeverityTrend(final StaplerRequest request, final StaplerResponse response) throws IOException {
		Run<?,?> lastBuild = this.getLastFinishedBuild();
		final CodeDxBuildAction lastAction = lastBuild.getAction(CodeDxBuildAction.class);

		final Map<String,Color> colorMap = new HashMap<String,Color>();

		colorMap.put(Filter.SEVERITY_CRITICAL, new Color(0x86177E));
		colorMap.put(Filter.SEVERITY_HIGH, new Color(0xbd0026));
		colorMap.put(Filter.SEVERITY_MEDIUM, new Color(0xfd8d3c));
		colorMap.put(Filter.SEVERITY_LOW, new Color(0xfed976));
		colorMap.put(Filter.SEVERITY_INFO, new Color(0x888888));
		colorMap.put(Filter.SEVERITY_UNSPECIFIED, new Color(0xadadad));

		(new Graph(-1L, CHART_WIDTH, CHART_HEIGHT){
			@Override
			protected JFreeChart createGraph() {
				return CodeDxChartBuilder.buildChart(lastAction, analysisResultConfiguration.getNumBuildsInGraph(),"severity",colorMap);
			}
		}).doPng(request, response);
	}


	/**
	 * Display the status trend graph.
	 *
	 * @param request Stapler request
	 * @param response Stapler response
	 * @throws IOException in case of an error
	 */
	public void doStatusTrend(final StaplerRequest request, final StaplerResponse response) throws IOException {
		Run<?,?> lastBuild = this.getLastFinishedBuild();
		final CodeDxBuildAction lastAction = lastBuild.getAction(CodeDxBuildAction.class);

		final Map<String,Color> colorMap = new HashMap<String,Color>();

		colorMap.put(StatisticGroup.New, new Color(0x542788));
		colorMap.put(StatisticGroup.Unresolved, new Color(0x998ec3));
		colorMap.put(StatisticGroup.Reopened, new Color(0xAC5DA7));
		colorMap.put(StatisticGroup.Fixed, new Color(0x3288bd));
		colorMap.put(StatisticGroup.Mitigated, new Color(0x295ec6));
		colorMap.put(StatisticGroup.Assigned, new Color(0x01665e));
		colorMap.put(StatisticGroup.Escalated, new Color(0x5ab4ac));
		colorMap.put(StatisticGroup.Ignored, new Color(0xd8b365));
		colorMap.put(StatisticGroup.FalsePositive, new Color(0xd9d9d9));

		(new Graph(-1, CHART_WIDTH, CHART_HEIGHT){
			@Override
			protected JFreeChart createGraph() {
				return CodeDxChartBuilder.buildChart(lastAction, analysisResultConfiguration.getNumBuildsInGraph(),"status",colorMap);
			}
		}).doPng(request, response);
	}
}