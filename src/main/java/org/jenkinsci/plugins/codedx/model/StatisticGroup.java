package org.jenkinsci.plugins.codedx.model;

import java.util.HashSet;
import java.util.Set;

public enum StatisticGroup {
    Unspecified("Unspecified"),
    Info("Info"),
    Low("Low"),
    Medium("Medium"),
    High("High"),
    Critical("Critical"),

	Gone("Gone"),
	Assigned("Assigned"),
	Escalated("Escalated"),
	Unresolved("Unresolved"),
	FalsePositive("False Positive"),
	Ignored("Ignored"),
	Mitigated("Mitigated"),
	Fixed("Fixed");

    private String value;

    private StatisticGroup(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }

    public static StatisticGroup forValue(String value) {
        for (StatisticGroup group : StatisticGroup.values()) {
            if (group.value.equals(value)) {
                return group;
            }
        }
        return null;
    }

    public static Set<StatisticGroup> valuesForStatistic(String statisticName) {
        Set<StatisticGroup> values = new HashSet<StatisticGroup>();
        if ("severity".equals(statisticName)) {
            values.add(Unspecified);
            values.add(Info);
            values.add(Low);
            values.add(Medium);
            values.add(High);
            values.add(Critical);
        } else if ("status".equals(statisticName)) {
	        values.add(Fixed);
	        values.add(Mitigated);
	        values.add(Ignored);
	        values.add(FalsePositive);
	        values.add(Unresolved);
	        values.add(Escalated);
            values.add(Assigned);
        }

        return values;
    }
}
