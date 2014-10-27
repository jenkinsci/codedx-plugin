package org.jenkinsci.plugins.codedx;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.util.ChartUtil;
import java.io.IOException;
import java.io.Serializable;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

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

    /** 
     * Maximal number of last successful builds displayed in the trend graphs.
     * One or less means unlimited.
     */
    private final int numBuildsInGraph;

    public CodeDxProjectAction(final AbstractProject<?, ?> project,
            int numBuildsInGraph) {
        this.project = project;
        this.numBuildsInGraph = numBuildsInGraph;
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

        ChartUtil.generateGraph(
                request,
                response,
                CodeDxChartBuilder.buildChart(lastAction, numBuildsInGraph,"severity"),
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

        ChartUtil.generateGraph(
                request,
                response,
                CodeDxChartBuilder.buildChart(lastAction, numBuildsInGraph,"status"),
                CHART_WIDTH,
                CHART_HEIGHT);
    }
}