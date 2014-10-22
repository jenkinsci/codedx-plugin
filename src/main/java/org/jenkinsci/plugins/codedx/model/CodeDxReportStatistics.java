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
    
    private List<CodeDxGroupStatistics> statistics;

	public CodeDxReportStatistics(List<CodeDxGroupStatistics> statistics) {

		System.out.println("Statistics is: " + statistics);
		this.statistics = statistics;
	}
    
	@Exported(name="groups")
	public List<CodeDxGroupStatistics> getStatistics() {
		return statistics;
	}
	
    @Exported(name="totalFindings")
    public int getFindings() {
        int findings = 0;

        for(CodeDxGroupStatistics it : statistics) {
            findings += it.getFindings();
        }

        return findings;
    }
    
    public List<String> getAllGroups() {
        List<String> groups = new LinkedList<String>();

        for(CodeDxGroupStatistics it : statistics) {
            groups.add(it.getGroup());
        }

        return groups;
    }
    
    public CodeDxGroupStatistics getGroup(String group) {
        for(CodeDxGroupStatistics it : statistics) {
            if(it.getGroup().equals(group)) {
                return it;
            }
        }

        return new CodeDxGroupStatistics(group, 0);
    }
}
