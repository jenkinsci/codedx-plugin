package org.jenkinsci.plugins.codedx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jenkinsci.plugins.codedx.model.CodeDxReportStatistics;
import org.jenkinsci.plugins.codedx.model.CodeDxSeverityStatistics;

/**
 * 
 * @author anthonyd
 *
 */
public class CodeDxDiffSummary extends CodeDxDiff{

	private final List<CodeDxDiffSeverity> severityDiffs;
	
	public CodeDxDiffSummary(List<CodeDxDiffSeverity> severityDiffs, int findings, int findingsDelta) {
		super(findings, findingsDelta);
		this.severityDiffs = severityDiffs;
		// TODO Auto-generated constructor stub
	}


	public List<CodeDxDiffSeverity> getSeverityDiffs() {
		return severityDiffs;
	}


    public static CodeDxDiffSummary getDiffSummary(
            CodeDxReportStatistics previous,
            CodeDxReportStatistics current) {
        if(previous == null) {
            return getDiffSummary(current);
        }

        Set<String> severities = new HashSet<String>();
        severities.addAll(previous.getAllSeverities());
        severities.addAll(current.getAllSeverities());

        List<CodeDxDiffSeverity> result = new ArrayList<CodeDxDiffSeverity>();
        int findings = 0;
        int findingsDelta = 0;

        for(String severity: severities) {
            // Quadratic complexity can be optimized, but severities count is small
            CodeDxSeverityStatistics curStats = current.getSeverity(severity);
            CodeDxSeverityStatistics prevStats = previous.getSeverity(severity);

            result.add(new CodeDxDiffSeverity(curStats.getSeverity(),
                    curStats.getFindings(),
                    curStats.getFindings() - prevStats.getFindings()));

            findings += curStats.getFindings();
            findingsDelta += curStats.getFindings() - prevStats.getFindings();
        }

        return new CodeDxDiffSummary(result, findings,findingsDelta);
    }

    private static CodeDxDiffSummary getDiffSummary(CodeDxReportStatistics current) {
        if(current == null) {
            return getDiffSummary();
        }

        List<CodeDxDiffSeverity> result = new ArrayList<CodeDxDiffSeverity>();
        int findings = 0;

        for(CodeDxSeverityStatistics language: current.getStatistics()) {
            result.add(new CodeDxDiffSeverity(language.getSeverity(),
                    language.getFindings(), 0));

            findings += language.getFindings();
        }

        return new CodeDxDiffSummary(result, findings, 0);
    }


    private static CodeDxDiffSummary getDiffSummary() {
        return new CodeDxDiffSummary(
                Collections.<CodeDxDiffSeverity>emptyList(), 0, 0);
    }
}
