package org.jenkinsci.plugins.codedx;

import hudson.util.ChartUtil.NumberOnlyBuildLabel;
import hudson.util.DataSetBuilder;
import hudson.util.ShiftedCategoryAxis;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jenkinsci.plugins.codedx.model.CodeDxGroupStatistics;
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
            	
            	if(colors.containsKey(row)){
            		
            		colorList.add(colors.get(row));
            	}
            }
        }

        if(colorList.size() == rows.size()){
        
            plot.setRenderer(new CodeDxAreaRenderer(colorList));
        }
        else{
        	
            plot.setRenderer(new CodeDxAreaRenderer(null));
        }
       
        return chart;
    }

    private static CategoryDataset buildDataset(CodeDxBuildAction lastAction,
            int numBuildsInGraph, String statisticsName){
        DataSetBuilder<String, NumberOnlyBuildLabel> builder = new DataSetBuilder<String, NumberOnlyBuildLabel>();
        Set<String> allGroups = new HashSet<String>();

        CodeDxBuildAction action = lastAction;
        int numBuilds = 0;

        // numBuildsInGraph <= 1 means unlimited
        while(action != null && (numBuildsInGraph <= 1 || numBuilds < numBuildsInGraph)){
            CodeDxResult result = action.getResult();
            if(result != null){
                NumberOnlyBuildLabel buildLabel = new NumberOnlyBuildLabel(action.getBuild());

                allGroups.addAll(result.getStatistics(statisticsName).getAllGroups());
                Set<String> remainingGroups = new HashSet<String>(allGroups);

                for(CodeDxGroupStatistics groupStats : result.getStatistics(statisticsName).getStatistics()){
                    builder.add(groupStats.getFindings(), groupStats.getGroup(), buildLabel);
                    remainingGroups.remove(groupStats.getGroup());
                }
                
                for(String group : remainingGroups) {
                    // Language disappeared
                    builder.add(0, group, buildLabel);
                }

                ++numBuilds;
            }

            action = action.getPreviousAction();
        }

        return builder.build();
    }
}