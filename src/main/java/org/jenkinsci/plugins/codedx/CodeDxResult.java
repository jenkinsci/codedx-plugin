package org.jenkinsci.plugins.codedx;

import java.io.Serializable;
import java.util.Map;

import hudson.model.AbstractBuild;

import org.jenkinsci.plugins.codedx.model.CodeDxReportStatistics;

/**
 * 
 * @author ademartini This file is heavily derived from the sloccount-plugin (author: lordofthepigs)
 *
 */
public class CodeDxResult implements Serializable{

    /** Serial version UID. */
    private static final long serialVersionUID = 0L;

    private final AbstractBuild<?,?> owner;
    
	private Map<String, CodeDxReportStatistics> statisticsMap;

	public CodeDxResult(Map<String,CodeDxReportStatistics> statisticsMap, AbstractBuild<?,?> owner){
		
		this.owner = owner;
		this.statisticsMap = statisticsMap;
	}

    public AbstractBuild<?,?> getOwner() {
        return owner;
    }
    
	public CodeDxReportStatistics getStatistics(String name) {

		return statisticsMap.get(name);
	}

	public Map<String, CodeDxReportStatistics> getStatisticsMap() {

		return statisticsMap;
	}

	public boolean isEmpty(){
		
		for(CodeDxReportStatistics stats: statisticsMap.values()){
			
			if(stats.getFindings() > 0){
				
				return false;
			}
		}
		
		return true;
	}
}
