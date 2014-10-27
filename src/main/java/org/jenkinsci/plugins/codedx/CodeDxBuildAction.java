package org.jenkinsci.plugins.codedx;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;

import java.io.Serializable;

import org.jenkinsci.plugins.codedx.model.CodeDxReportStatistics;
import org.kohsuke.stapler.StaplerProxy;

/**
 *
 * @author ademartini This file is heavily derived from the sloccount-plugin (author: lordofthepigs)
 */
public class CodeDxBuildAction implements Action, Serializable, StaplerProxy {
    /** Serial version UID. */
    private static final long serialVersionUID = 0L;

    public static final String URL_NAME = "codedxResult";

    private AbstractBuild<?,?> build;
    private CodeDxResult result;

    public CodeDxBuildAction(AbstractBuild<?,?> build, CodeDxResult result){
        this.build = build;
        this.result = result;
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
     * Get differences between two severity statistics.
     * 
     * @return the differences
     */
    public CodeDxDiffSummary getSeverityDiffSummary() {
        return CodeDxDiffSummary.getDiffSummary(getPreviousSeverityStats(),
                result.getStatistics("severity"), "Severity");
    }

    /**
     * Get differences between two status statistics.
     * 
     * @return the differences
     */
    public CodeDxDiffSummary getStatusDiffSummary() {
        return CodeDxDiffSummary.getDiffSummary(getPreviousStatusStats(),
                result.getStatistics("status"), "Status");
    }

    public CodeDxResult getResult(){
        return this.result;
    }

    private CodeDxReportStatistics getPreviousSeverityStats(){
        CodeDxResult previous = this.getPreviousResult();
        if(previous == null){
            return null;
        }else{
           return previous.getStatistics("severity");
        }
    }
    
    private CodeDxReportStatistics getPreviousStatusStats(){
        CodeDxResult previous = this.getPreviousResult();
        if(previous == null){
            return null;
        }else{
           return previous.getStatistics("status");
        }
    }

    CodeDxResult getPreviousResult(){
        CodeDxBuildAction previousAction = this.getPreviousAction();
        CodeDxResult previousResult = null;
        if(previousAction != null){
            previousResult = previousAction.getResult();
        }
        
        return previousResult;
    }

    /**
     * Get the previous valid and non-empty action.
     * 
     * @return the action or null
     */
    CodeDxBuildAction getPreviousAction(){
        if(this.build == null){
            return null;
        }

        AbstractBuild<?,?> previousBuild = this.build.getPreviousBuild();

        while(previousBuild != null){
            CodeDxBuildAction action = previousBuild
                    .getAction(CodeDxBuildAction.class);

            if (action != null) {
                CodeDxResult result = action.getResult();
                
                if(result != null && !result.isEmpty()) {
                    return action;
                }
            }

            previousBuild = previousBuild.getPreviousBuild();
        }

        return null;
    }

    public AbstractBuild<?,?> getBuild(){
        return this.build;
    }

    public Object getTarget() {
        return this.result;
    }
}