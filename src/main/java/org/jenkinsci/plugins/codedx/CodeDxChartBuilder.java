package org.jenkinsci.plugins.codedx;

import hudson.util.ChartUtil.NumberOnlyBuildLabel;
import hudson.util.DataSetBuilder;
import hudson.util.ShiftedCategoryAxis;

import java.awt.Color;
import java.io.Serializable;
import java.math.BigDecimal;
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

    private static CategoryDataset buildDataset(CodeDxBuildAction lastAction,
            int numBuildsInGraph, String statisticsName){
        DataSetBuilder<StatisticGroup, NumberOnlyBuildLabel> builder = new DataSetBuilder<StatisticGroup, NumberOnlyBuildLabel>();
        Set<StatisticGroup> allGroups = new HashSet<StatisticGroup>();

        CodeDxBuildAction action = lastAction;
        int numBuilds = 0;

        // numBuildsInGraph <= 1 means unlimited
        while(action != null && (numBuildsInGraph <= 1 || numBuilds < numBuildsInGraph)){
            CodeDxResult result = action.getResult();
            if(result != null){
                NumberOnlyBuildLabel buildLabel = new NumberOnlyBuildLabel(action.getBuild());

                for (String group : result.getStatistics(statisticsName).getAllGroups()) {
                    allGroups.add(StatisticGroup.forValue(group));
                }
                Set<StatisticGroup> remainingGroups = StatisticGroup.valuesForStatistic(statisticsName);

                for(CodeDxGroupStatistics groupStats : result.getStatistics(statisticsName).getStatistics()){
                    builder.add(groupStats.getFindings(), StatisticGroup.forValue(groupStats.getGroup()), buildLabel);
                    remainingGroups.remove(StatisticGroup.forValue(groupStats.getGroup()));
                }
                
                for(StatisticGroup group : remainingGroups) {
                    builder.add(0, group, buildLabel);
                }

                ++numBuilds;
            }

            action = action.getPreviousAction();
        }

        return builder.build();
    }


}