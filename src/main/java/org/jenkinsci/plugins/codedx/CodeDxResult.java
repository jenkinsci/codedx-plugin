package org.jenkinsci.plugins.codedx;

import java.io.Serializable;

import hudson.model.AbstractBuild;

import org.jenkinsci.plugins.codedx.model.CodeDxReportStatistics;

/**
 * 
 * @author ademartini This file is heavily derived from the sloccount-plugin (author: lordofthepigs)
 *
 */
public class CodeDxResult implements Serializable{

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    private final AbstractBuild<?,?> owner;
    
	private CodeDxReportStatistics statistics;

	public CodeDxResult(CodeDxReportStatistics statistics,AbstractBuild<?,?> owner){
		
		this.owner = owner;
		this.statistics = statistics;
	}

    public AbstractBuild<?,?> getOwner() {
        return owner;
    }
    
	public CodeDxReportStatistics getStatistics() {

		return statistics;
	}

	public boolean isEmpty() {

        if(statistics != null) {
            return statistics.getFindings() <= 0;
        }
        
        return true;
	}

}
