package org.jenkinsci.plugins.codedx;

import hudson.util.ChartUtil.NumberOnlyBuildLabel;
import hudson.util.DataSetBuilder;
import hudson.util.ShiftedCategoryAxis;

import java.awt.Color;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.jenkinsci.plugins.codedx.model.CodeDxReportStatistics;
import org.jenkinsci.plugins.codedx.model.CodeDxSeverityStatistics;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
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

    private CodeDxChartBuilder(){
    }

    public static JFreeChart buildChart(CodeDxBuildAction action,
            int numBuildsInGraph){

        JFreeChart chart = ChartFactory.createStackedAreaChart(null, null,
                "Findings", buildDataset(action, numBuildsInGraph),
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

        CodeDxAreaRenderer renderer = new CodeDxAreaRenderer();
        plot.setRenderer(renderer);

        return chart;
    }

    private static CategoryDataset buildDataset(CodeDxBuildAction lastAction,
            int numBuildsInGraph){
        DataSetBuilder<String, NumberOnlyBuildLabel> builder = new DataSetBuilder<String, NumberOnlyBuildLabel>();
        Set<String> allSeverities = new HashSet<String>();

        CodeDxBuildAction action = lastAction;
        int numBuilds = 0;

        // numBuildsInGraph <= 1 means unlimited
        while(action != null && (numBuildsInGraph <= 1 || numBuilds < numBuildsInGraph)){
            CodeDxResult result = action.getResult();
            if(result != null){
                NumberOnlyBuildLabel buildLabel = new NumberOnlyBuildLabel(action.getBuild());

                allSeverities.addAll(result.getStatistics().getAllSeverities());
                Set<String> remainingSeverities = new HashSet<String>(allSeverities);

                for(CodeDxSeverityStatistics severityStats : result.getStatistics().getStatistics()){
                    builder.add(severityStats.getFindings(), severityStats.getSeverity(), buildLabel);
                    remainingSeverities.remove(severityStats.getSeverity());
                }
                
                for(String language : remainingSeverities) {
                    // Language disappeared
                    builder.add(0, language, buildLabel);
                }

                ++numBuilds;
            }

            action = action.getPreviousAction();
        }

        return builder.build();
    }
    
    public static JFreeChart buildChartDelta(CodeDxBuildAction action,
            int numBuildsInGraph){

        JFreeChart chart = ChartFactory.createStackedAreaChart(null, null,
                "Findings Delta", buildDatasetDelta(action, numBuildsInGraph),
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

        CodeDxAreaRenderer renderer = new CodeDxAreaRenderer();
        plot.setRenderer(renderer);

        return chart;
    }

    private static CategoryDataset buildDatasetDelta(CodeDxBuildAction lastAction,
            int numBuildsInGraph){
        DataSetBuilder<String, NumberOnlyBuildLabel> builder = new DataSetBuilder<String, NumberOnlyBuildLabel>();
        Set<String> allSeverities = new HashSet<String>();
        CodeDxBuildAction action = lastAction;

        // Initial languages from the first action
        if(action != null && action.getResult() != null) {
            allSeverities.addAll(action.getResult().getStatistics().getAllSeverities());
        }

        int numBuilds = 0;

        // numBuildsInGraph <= 1 means unlimited
        while(action != null && (numBuildsInGraph <= 1 || numBuilds < numBuildsInGraph)){
            CodeDxBuildAction previousAction = action.getPreviousAction();
            CodeDxResult result = action.getResult();
            CodeDxReportStatistics previousStatistics = null;

            if(result != null){
                NumberOnlyBuildLabel buildLabel = new NumberOnlyBuildLabel(action.getBuild());

                if(previousAction != null && previousAction.getResult() != null){
                    previousStatistics = previousAction.getResult().getStatistics();
                } else {
                    // This will produce zero delta for the first build
                    previousStatistics = result.getStatistics();
                }

                allSeverities.addAll(previousStatistics.getAllSeverities());
                Set<String> remainingSeverities = new HashSet<String>(allSeverities);

                for(CodeDxSeverityStatistics current : result.getStatistics().getStatistics()){
                    CodeDxSeverityStatistics previous = previousStatistics.getSeverity(current.getSeverity());

                    builder.add(current.getFindings() - previous.getFindings(),
                            current.getSeverity(), buildLabel);

                    remainingSeverities.remove(current.getSeverity());
                }

                for(String severity : remainingSeverities) {
                    CodeDxSeverityStatistics previous
                            = previousStatistics.getSeverity(severity);

                    // Language disappeared (current - previous = 0 - previous)
                    builder.add(-previous.getFindings(), severity, buildLabel);
                }

                ++numBuilds;
            }

            action = previousAction;
        }

        return builder.build();
    }
}