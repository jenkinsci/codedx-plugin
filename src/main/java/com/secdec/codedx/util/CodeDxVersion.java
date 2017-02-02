package com.secdec.codedx.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a Code Dx version number, disregarding tail matter like "RC" and "SNAPSHOT".
 * The constructor is private; use the static <code>fromString</code> method instead.
 * Instances are available for reference to point out min/max versions supporting certain
 * features that might necessitate the Jenkins plugin to behave differently.
 */
public final class CodeDxVersion implements Comparable<CodeDxVersion> {

	/** First version which does not support the "New" triage status. */
	public final static CodeDxVersion MAX_FOR_NEW_STATUS = fromString("2.4.0");
	/** First version that supports the "analysis names" feature. */
	public final static CodeDxVersion MIN_FOR_ANALYSIS_NAMES = fromString("2.4.0");

	public static CodeDxVersion fromString(String version){
		// format is expected to be "x(.y)*-abc", and we want the x.y.z part
		Pattern versionRegex = Pattern.compile("^(\\d+(?:\\.\\d+)*).*");
		Matcher versionMatcher = versionRegex.matcher(version);
		if(versionMatcher.matches()){
			String matchedNumbers = versionMatcher.group(1);
			String[] rawNumbers = matchedNumbers.split("\\.");
			int[] numbers = new int[rawNumbers.length];
			for(int i=0; i<rawNumbers.length; ++i){
				numbers[i] = Integer.valueOf(rawNumbers[i]);
			}
			return new CodeDxVersion(numbers);
		} else {
			throw new IllegalArgumentException("for input string \"" + version + "\"");
		}
	}

	private final int[] numbers;

	private CodeDxVersion(int[] numbers){
		this.numbers = numbers;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<numbers.length; ++i){
			if(i > 0) sb.append('.');
			sb.append(numbers[i]);
		}
		return sb.toString();
	}

	@Override
	public boolean equals(Object thatObj) {
		if(thatObj instanceof CodeDxVersion){
			CodeDxVersion that = (CodeDxVersion) thatObj;
			return compareTo(that) == 0;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public int compareTo(CodeDxVersion that) {
		int index = 0;
		int limit = Math.max(this.numbers.length, that.numbers.length);
		do {
			int nThis = index < this.numbers.length ? this.numbers[index] : 0;
			int nThat = index < that.numbers.length ? that.numbers[index] : 0;
			if(nThis != nThat) return nThis - nThat;
			++index;
		} while(index < limit);
		return 0;
	}


}
