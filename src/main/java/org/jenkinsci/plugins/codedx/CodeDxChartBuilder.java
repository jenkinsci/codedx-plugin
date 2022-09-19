/*
 *
 * © 2022 Synopsys, Inc. All rights reserved worldwide.
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
import hudson.model.Build;
import hudson.model.Run;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;
import hudson.util.DataSetBuilder;
import hudson.util.ShiftedCategoryAxis;

import java.awt.Color;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.jenkinsci.plugins.codedx.model.CodeDxGroupStatistics;
import org.jenkinsci.plugins.codedx.model.StatisticGroup;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;

import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleInsets;

/**
 *
 * @author ademartini This file is heavily derived from the sloccount-plugin (author: lordofthepigs)
 */
public class CodeDxChartBuilder implements Serializable {
	/** Serial version UID. */
	private static final long serialVersionUID = 0L;

	private static Set<String> hiddenGroups = new HashSet<String>() {{
		add(StatisticGroup.Gone);
	}};

	private CodeDxChartBuilder(){
	}

	public static JFreeChart buildChart(CodeDxBuildAction action,
			int numBuildsInGraph, String statisticsName, Map<String,Color> colors){

		CategoryDataset dataset = buildDataset(action, numBuildsInGraph, statisticsName);

		JFreeChart chart = ChartFactory.createStackedAreaChart(null, null,
				"Findings", dataset,
				PlotOrientation.VERTICAL, true, false, true);

		chart.setBackgroundPaint(Color.white);

		CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setOutlinePaint(null);
		plot.setForegroundAlpha(0.8f);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.black);

		CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
		plot.setDomainAxis(domainAxis);
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
		domainAxis.setLowerMargin(0.0);
		domainAxis.setUpperMargin(0.0);
		domainAxis.setCategoryMargin(0.0);

		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		// crop extra space around the graph
		plot.setInsets(new RectangleInsets(0, 0, 0, 5.0));


		List rows = dataset.getRowKeys();

		List<Color> colorList = new ArrayList<Color>();

		if(colors != null){
			for(Object row : rows){
				if(colors.containsKey(row.toString())){
					colorList.add(colors.get(row.toString()));
				} else {
					// Use color based on hash-code for a consistent
					// display color. Might not be pretty, but will still
					// allow us to show consistently-colored data.
					colorList.add(new Color(row.toString().hashCode()));
				}
			}
		}

		if(colorList.size() == rows.size()){

			plot.setRenderer(new CodeDxAreaRenderer(colorList));
		}
		else{

			plot.setRenderer(new CodeDxAreaRenderer(null));
		}


		ArrayList<LegendItem> legendItems = new ArrayList<LegendItem>();
		Iterator<LegendItem> itr = plot.getLegendItems().iterator();
		while (itr.hasNext()) {
			legendItems.add(itr.next());
		}
		//Reverse the order
		Collections.sort(legendItems, new Comparator<LegendItem>() {
			public int compare(LegendItem lhs, LegendItem rhs) {
				return rhs.getSeriesKey().compareTo(lhs.getSeriesKey());
			}
		});
		LegendItemCollection newItems = new LegendItemCollection();
		for (LegendItem item : legendItems) {
			newItems.add(item);
		}
		plot.setFixedLegendItems(newItems);

		return chart;
	}

	private static List<CodeDxBuildAction> collectSuccessfulBuilds(CodeDxBuildAction lastAction, int maxNumBuilds) {
		CodeDxBuildAction action = lastAction;
		List<CodeDxBuildAction> allBuildActions = new ArrayList<>();

		// maxNumBuilds <= 1 means unlimited
		while(action != null && (maxNumBuilds <= 1 || allBuildActions.size() < maxNumBuilds)){
			CodeDxResult result = action.getResult();
			if(result != null){
				allBuildActions.add(action);
			}

			action = action.getPreviousAction();
		}
		return allBuildActions;
	}

	/**
	 * Generates a NumberOnlyBuildLabel by using the provided Run, if available. Otherwise will
	 * fall back to using the provided AbstractBuild. Run is the current expected parameter, but
	 * accept AbstractBuild if available from previous plugin version.
	 */
	private static NumberOnlyBuildLabel MakeBuildLabel(Run<?, ?> run, AbstractBuild<?, ?> build)
	{
		if (run != null) {
			return new NumberOnlyBuildLabel(run);
		} else if (build != null) {
			// There's a constructor accepting an `AbstractBuild` but I can't use it without
			// getting ambiguous method errors. Grab the constructor and call it explicitly
			// instead.
			Class<?> cls = NumberOnlyBuildLabel.class;
			try {
				Constructor<?> ctor = cls.getConstructor(AbstractBuild.class);
				return (NumberOnlyBuildLabel)ctor.newInstance(build);
			} catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private static CategoryDataset buildDataset(CodeDxBuildAction lastAction,
			int numBuildsInGraph, String statisticsName){
		DataSetBuilder<String, NumberOnlyBuildLabel> builder = new DataSetBuilder<String, NumberOnlyBuildLabel>();

		List<CodeDxBuildAction> allBuildActions = collectSuccessfulBuilds(lastAction, numBuildsInGraph);

		// Collect all known groups so we can fill in blanks. Can't just rely on `valuesForStatistics` in
		// case there's a new/unknown group name, so we loop through all available builds first. Use
		// `valuesForStatistics` as a base so we can still show empty values for groups that don't
		// have any results.
		Set<String> knownGroups = StatisticGroup.valuesForStatistic(statisticsName);
		for (CodeDxBuildAction action : allBuildActions) {
			knownGroups.addAll(action.getResult().getStatistics(statisticsName).getAllGroups());
		}

		for (CodeDxBuildAction action : allBuildActions) {
			NumberOnlyBuildLabel buildLabel = MakeBuildLabel(action.getRun(), action.getBuild());

			CodeDxResult result = action.getResult();

			Set<String> remainingGroups = new HashSet<>(knownGroups);

			for(CodeDxGroupStatistics groupStats : result.getStatistics(statisticsName).getStatistics()){
				String statisticGroup = groupStats.getGroup();
				if (! hiddenGroups.contains(statisticGroup))
					builder.add(groupStats.getFindings(), statisticGroup, buildLabel);
				remainingGroups.remove(statisticGroup);
			}

			for(String group : remainingGroups) {
				builder.add(0, group, buildLabel);
			}
		}

		return builder.build();
	}


}