package org.jenkinsci.plugins.codedx;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import hudson.model.Result;

import com.secdec.codedx.api.client.CodeDxClient;
import com.secdec.codedx.api.client.CodeDxClientException;
import com.secdec.codedx.api.client.Filter;

/**
 * Used to to determine if a build should be considered unstable or a failure, 
 * based on Code Dx analysis results.
 * 
 * @author anthonyd
 *
 */
public class AnalysisResultChecker {

	private CodeDxClient client;
	private String failureSeverity;
	private String unstableSeverity;
	private boolean failureOnlyNew;
	private boolean unstableOnlyNew;
	private int runId;
	private PrintStream logger;
	
	
	
	public AnalysisResultChecker(CodeDxClient client, String failureSeverity,
			String unstableSeverity, boolean failureOnlyNew,
			boolean unstableOnlyNew, int runId, PrintStream logger) {

		this.client = client;
		this.failureSeverity = failureSeverity;
		this.unstableSeverity = unstableSeverity;
		this.failureOnlyNew = failureOnlyNew;
		this.unstableOnlyNew = unstableOnlyNew;
		this.runId = runId;
		this.logger = logger;
	}

	public Result checkResult() throws ClientProtocolException, CodeDxClientException, IOException{
		
		
		logger.println("Checking for findings that indicate build failure...");
		if(!"None".equalsIgnoreCase(failureSeverity) && client.getFindingsCount(runId, createFilter(failureSeverity,failureOnlyNew)) > 0){
			
			logger.println("Failure!");
			return Result.FAILURE;
		}

		logger.println("Checking for findings that indicate unstable build.");
		if(!"None".equalsIgnoreCase(unstableSeverity) && client.getFindingsCount(runId, createFilter(unstableSeverity,unstableOnlyNew)) > 0){
			
			logger.println("Unstable!");
			return Result.UNSTABLE;
		}
		
		logger.println("CodeDx results indicate success!");
		
		return Result.SUCCESS;
	}
	
	private Filter createFilter(String minSeverity, boolean onlyNew){
		
		Filter filter = new Filter();
		
		filter.setSeverity(getSeverities(minSeverity));
		
		filter.setStatus(getStatuses(onlyNew));
		
		logger.println("Using filter: " + filter.toString());
		
		return filter;
	}
	
	private String[] getSeverities(String minSeverity){
		
		String[] possibleSeverities = {Filter.SEVERITY_INFO, Filter.SEVERITY_LOW, Filter.SEVERITY_MEDIUM, Filter.SEVERITY_HIGH};
		
		for(int i = 0; i < possibleSeverities.length; i++){
			
			if(possibleSeverities[i].equalsIgnoreCase(minSeverity)){
				
				return Arrays.copyOfRange(possibleSeverities, i, possibleSeverities.length);
			}
		}
		
		return new String[]{};
	}
	
	private String[] getStatuses(boolean onlyNew){
		
		if(onlyNew){
			
			return new String[]{Filter.STATUS_NEW};
		}
		else{
			
			return new String[]{Filter.STATUS_NEW, Filter.STATUS_UNRESOLVED, Filter.STATUS_ESCALATED};
		}
	}
}
