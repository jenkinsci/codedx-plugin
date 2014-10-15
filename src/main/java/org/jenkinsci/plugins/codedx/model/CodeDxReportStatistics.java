package org.jenkinsci.plugins.codedx.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.kohsuke.stapler.export.Exported;

/**
 * 
 * @author anthonyd
 *
 */
public class CodeDxReportStatistics implements Serializable{

	/** Serial version UID. */
    private static final long serialVersionUID = 0L;
    
    private List<CodeDxSeverityStatistics> statistics;

	public CodeDxReportStatistics(List<CodeDxSeverityStatistics> statistics) {

		System.out.println("Statistics is: " + statistics);
		this.statistics = statistics;
	}
    
	@Exported(name="severities")
	public List<CodeDxSeverityStatistics> getStatistics() {
		return statistics;
	}
	
    @Exported(name="totalFindings")
    public int getFindings() {
        int findings = 0;

        System.out.println("getFindings Statistics is: " + statistics);
        for(CodeDxSeverityStatistics it : statistics) {
            findings += it.getFindings();
        }

        return findings;
    }
    
    public List<String> getAllSeverities() {
        List<String> severities = new LinkedList<String>();

        for(CodeDxSeverityStatistics it : statistics) {
            severities.add(it.getSeverity());
        }

        return severities;
    }
    
    public CodeDxSeverityStatistics getSeverity(String severity) {
        for(CodeDxSeverityStatistics it : statistics) {
            if(it.getSeverity().equals(severity)) {
                return it;
            }
        }

        return new CodeDxSeverityStatistics(severity, 0);
    }
}
