package org.jenkinsci.plugins.codedx.client;

public class CodeDxClientException extends Exception{

	private int httpCode;
	
	public CodeDxClientException(String responseMessage, int httpCode) {
        super(responseMessage);
        this.httpCode = httpCode;
    }
	

    public int getHttpCode() {
		return httpCode;
	}
}
