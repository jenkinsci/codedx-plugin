package org.jenkinsci.plugins.codedx.model;

public enum StatisticGroup {
    Unspecified("Unspecified"),
    Info("Info"),
    Low("Low"),
    Medium("Medium"),
    High("High"),

    FalsePositive("False Positive"),
    Ignored("Ignored"),
    Escalated("Escalated"),
    Assigned("Assigned"),
    Fixed("Fixed"),
    Unresolved("Unresolved"),
    New("New");

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

}
