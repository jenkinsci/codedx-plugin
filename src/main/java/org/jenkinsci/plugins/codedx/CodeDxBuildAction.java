package org.jenkinsci.plugins.codedx;

import hudson.model.AbstractBuild;
import hudson.model.Action;

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
     * Get differences between two report statistics.
     * 
     * @return the differences
     */
    public CodeDxDiffSummary getDiffSummary() {
        return CodeDxDiffSummary.getDiffSummary(getPreviousStatistics(),
                result.getStatistics());
    }

    public CodeDxResult getResult(){
        return this.result;
    }

    private CodeDxReportStatistics getPreviousStatistics(){
        CodeDxResult previous = this.getPreviousResult();
        if(previous == null){
            return null;
        }else{
           return previous.getStatistics();
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