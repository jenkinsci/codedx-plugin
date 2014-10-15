package com.secdec.codedx.api.client;

import java.util.List;

public class CountGroup {

	String id;
	String name;
	int count;
	
	List<CountGroup> children;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public List<CountGroup> getChildren() {
		return children;
	}

	public void setChildren(List<CountGroup> children) {
		this.children = children;
	}
	
	
}
