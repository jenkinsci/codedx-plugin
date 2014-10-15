package com.secdec.codedx.api.client;

public class GroupedCountRequest extends CountRequest{

	public GroupedCountRequest() {

	}

	public GroupedCountRequest(Filter filter, String countBy) {
		super(filter);
		this.countBy = countBy;
	}

	private String countBy;

	public String getCountBy() {
		return countBy;
	}

	public void setCountBy(String countBy) {
		this.countBy = countBy;
	}
}
