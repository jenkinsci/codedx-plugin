package com.secdec.codedx.api.client;

import java.time.Instant;
import java.util.*;

public class AnalysisInfo {
	private int id;
	private String creationTime;
	private Map<String, List<AnalysisInputInfo>> toolInputs, toolOutputs;

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }

	public String getCreationTime() { return creationTime; }
	public Instant getCreationTimeInstant() { return Instant.parse(creationTime); }
	public void setCreationTime(String creationTime) { this.creationTime = creationTime; }

	public Map<String, List<AnalysisInputInfo>> getToolInputs() { return this.toolInputs; }
	public void setToolInputs(Map<String, List<AnalysisInputInfo>> toolInputs) { this.toolInputs = toolInputs; }

	public Map<String, List<AnalysisInputInfo>> getToolOutputs() { return this.toolOutputs; }
	public void setToolOutputs(Map<String, List<AnalysisInputInfo>> toolOutputs) { this.toolOutputs = toolOutputs; }

	private static Boolean containsInput(Map<String, List<AnalysisInputInfo>> inputs, String name) {
		for (Map.Entry<String, List<AnalysisInputInfo>> entry : inputs.entrySet()) {
			for (AnalysisInputInfo input : entry.getValue()) {
				if (input.getName().equals(name)) return true;
			}
		}
		return false;
	}

	private static void collectFiles(HashSet<String> target, Collection<List<AnalysisInputInfo>> inputs) {
		for (List<AnalysisInputInfo> infos : inputs) {
			for (AnalysisInputInfo info : infos) {
				if (!info.isFromGitSource()) {
					target.add(info.getName());
				}
			}
		}
	}

	public Set<String> getInputFiles() {
		HashSet<String> result = new HashSet<>();
		collectFiles(result, toolInputs.values());
		collectFiles(result, toolOutputs.values());
		return result;
	}
}
