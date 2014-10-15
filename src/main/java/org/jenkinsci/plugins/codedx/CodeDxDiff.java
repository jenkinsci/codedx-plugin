package org.jenkinsci.plugins.codedx;

/**
 * 
 * @author anthonyd
 *
 */
public class CodeDxDiff implements Comparable<CodeDxDiff>{

	private final int findings;
	private final int findingsDelta;
	
	public CodeDxDiff(int findings, int findingsDelta) {

		this.findings = findings;
		this.findingsDelta = findingsDelta;
	}

	public int getFindings() {
		return findings;
	}
	public int getFindingsDelta() {
		return findingsDelta;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + findings;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CodeDxDiff other = (CodeDxDiff) obj;
		if (findings != other.findings)
			return false;
		return true;
	}
	
	public int compareTo(CodeDxDiff o){
		
		return o.findings - findings;
	}
	
	
    public String getFindingsString() {
        return String.format("%,d", findings);
    }

    public String getFindingsDeltaString() {
        if(findingsDelta == 0) {
            return "";
        }

        // Negative prefix '-' is added automatically
        String result = String.format("%,d", findingsDelta);
        return (findingsDelta > 0) ? "+" + result : result;
    }
}
