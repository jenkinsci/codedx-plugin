package com.secdec.codedx.api.client;

public class CountRequest {

	private Filter filter;

	public CountRequest(){
			
	}
	
	public CountRequest(Filter filter){
		
		this.filter = filter;
	}
	
	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}	
}
