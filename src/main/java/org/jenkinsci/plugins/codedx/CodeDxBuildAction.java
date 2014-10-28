package org.jenkinsci.plugins.codedx;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
    private String[] projectUsers;
    
    public CodeDxBuildAction(AbstractBuild<?,?> build, CodeDxResult result, String[] projectUsers){
        this.build = build;
        this.result = result;
        this.projectUsers = projectUsers;
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

    public String[] getProjectUsers(){
    	
    	return projectUsers;
    }
    
    private class DiffGroupComparator implements Comparator<CodeDxDiffGroup>{

		List<String> groupOrdering = new ArrayList<String>();
		
    	public DiffGroupComparator(List<String> groupOrdering){
    		
    		this.groupOrdering = groupOrdering;
    	}
    	
		public int compare(CodeDxDiffGroup o1, CodeDxDiffGroup o2) {

			int index1 = groupOrdering.indexOf(o1.getName().toLowerCase());
			int index2 = groupOrdering.indexOf(o2.getName().toLowerCase());
			
			return Integer.compare(index1, index2);
		}
    	
    	
    }
    /**
     * Get differences between two severity statistics.
     * 
     * @return the differences
     */
    public CodeDxDiffSummary getSeverityDiffSummary() {
    
    	List<String> order = new ArrayList<String>();
    	order.add("high");
    	order.add("medium");
    	order.add("low");
    	order.add("info");
    	
        return CodeDxDiffSummary.getDiffSummary(getPreviousSeverityStats(),
                result.getStatistics("severity"), "Severity",new DiffGroupComparator(order));
    }

    /**
     * Get differences between two status statistics.
     * 
     * @return the differences
     */
    public CodeDxDiffSummary getStatusDiffSummary() {
    	
    	List<String> order = new ArrayList<String>();
    	order.add("fixed");
    	
    	for(String user: projectUsers){
    		
    		order.add("assigned to " + user.toLowerCase());
    	}

    	order.add("escalated");
    	order.add("new");
    	order.add("unresolved");
    	order.add("false positive");
    	order.add("gone");
    	
        return CodeDxDiffSummary.getDiffSummary(getPreviousStatusStats(),
                result.getStatistics("status"), "Status",new DiffGroupComparator(order));
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