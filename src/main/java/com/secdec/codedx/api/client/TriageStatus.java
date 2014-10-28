package com.secdec.codedx.api.client;

public class TriageStatus {

	private String type;
	private String display;
	private String settable;
	
	public static final String TYPE_STATUS = "status";
	public static final String TYPE_USER = "user";
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDisplay() {
		return display;
	}
	public void setDisplay(String display) {
		this.display = display;
	}
	public String getSettable() {
		return settable;
	}
	public void setSettable(String settable) {
		this.settable = settable;
	}
	
	@Override
	public String toString() {
		return "FindingStatus [type=" + type + ", display=" + display
				+ ", settable=" + settable + "]";
	}
	
	
}
