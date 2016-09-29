package org.jenkinsci.plugins.codedx;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.util.ChartUtil;

import java.awt.Color;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.jenkinsci.plugins.codedx.model.StatisticGroup;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.secdec.codedx.api.client.Filter;

/**
 *
 * @author ademartini This file is heavily derived from the sloccount-plugin (author: lordofthepigs)
 */
public class CodeDxProjectAction implements Action, Serializable {
    /** Serial version UID. */
    private static final long serialVersionUID = 0L;

    public static final String URL_NAME = "codedxResult";

    public static final int CHART_WIDTH = 500;
    public static final int CHART_HEIGHT = 200;

    public AbstractProject<?,?> project;

    private final String latestAnalysisUrl;
  
    private AnalysisResultConfiguration analysisResultConfiguration;

    public CodeDxProjectAction(final AbstractProject<?, ?> project,
            AnalysisResultConfiguration analysisResultConfiguration, String latestAnalysisUrl) {
        this.project = project;
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
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error
     */
    public void doIndex(final StaplerRequest request, final StaplerResponse response) throws IOException {
        AbstractBuild<?, ?> build = getLastFinishedBuild();
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
    public AbstractBuild<?, ?> getLastFinishedBuild() {
        AbstractBuild<?, ?> lastBuild = project.getLastBuild();
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
        AbstractBuild<?, ?> lastBuild = getLastFinishedBuild();
        return (lastBuild != null) ? lastBuild.getAction(CodeDxBuildAction.class) : null;
    }

    public final boolean hasValidResults() {
        AbstractBuild<?, ?> build = getLastFinishedBuild();

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
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error
     */
    public void doSeverityTrend(final StaplerRequest request, final StaplerResponse response) throws IOException {
        AbstractBuild<?,?> lastBuild = this.getLastFinishedBuild();
        CodeDxBuildAction lastAction = lastBuild.getAction(CodeDxBuildAction.class);

        Map<String,Color> colorMap = new HashMap<String,Color>();

        colorMap.put(Filter.SEVERITY_CRITICAL, new Color(0x610a14));
        colorMap.put(Filter.SEVERITY_HIGH, new Color(0xbd0026));
        colorMap.put(Filter.SEVERITY_MEDIUM, new Color(0xfd8d3c));
        colorMap.put(Filter.SEVERITY_LOW, new Color(0xfed976));
        colorMap.put(Filter.SEVERITY_INFO, new Color(0x888888));
        colorMap.put(Filter.SEVERITY_UNSPECIFIED, new Color(0xadadad));
        
        ChartUtil.generateGraph(
                request,
                response,
                CodeDxChartBuilder.buildChart(lastAction, analysisResultConfiguration.getNumBuildsInGraph(),"severity",colorMap),
                CHART_WIDTH,
                CHART_HEIGHT);
    }
    

    /**
     * Display the status trend graph.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error
     */
    public void doStatusTrend(final StaplerRequest request, final StaplerResponse response) throws IOException {
        AbstractBuild<?,?> lastBuild = this.getLastFinishedBuild();
        CodeDxBuildAction lastAction = lastBuild.getAction(CodeDxBuildAction.class);

        Map<String,Color> colorMap = new HashMap<String,Color>();

        colorMap.put(StatisticGroup.New.toString(), new Color(0x542788));
        colorMap.put(StatisticGroup.Unresolved.toString(), new Color(0x998ec3));
        colorMap.put(StatisticGroup.Fixed.toString(), new Color(0x3288bd));
	    colorMap.put(StatisticGroup.Mitigated.toString(), new Color(0x295ec6));
        colorMap.put(StatisticGroup.Assigned.toString(), new Color(0x01665e));
        colorMap.put(StatisticGroup.Escalated.toString(), new Color(0x5ab4ac));
        colorMap.put(StatisticGroup.Ignored.toString(), new Color(0xd8b365));
        colorMap.put(StatisticGroup.FalsePositive.toString(), new Color(0xd9d9d9));

        ChartUtil.generateGraph(
                request,
                response,
                CodeDxChartBuilder.buildChart(lastAction, analysisResultConfiguration.getNumBuildsInGraph(),"status",colorMap),
                CHART_WIDTH,
                CHART_HEIGHT);
    }
}