/*
 *
 * Copyright 2022 Synopsys, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 *
 */

package com.secdec.codedx.api.client;

/**
 * An exception thrown by various CodeDxClient calls.
 *
 * @author anthonyd
 *
 */
public class CodeDxClientException extends Exception{

	private int httpCode;
	private String requestPath;
	private String requestMethod;
	private String responseContent;
	private String responseMessage;

	public CodeDxClientException(String requestMethod, String requestPath, String responseMessage, int httpCode, String responseContent) {
		super(String.format("Received non-success response from the server [%d: %s] while executing %s %s%n{{%s}}", httpCode, responseMessage, requestMethod, requestPath, responseContent));
		this.httpCode = httpCode;
		this.requestPath = requestPath;
		this.requestMethod = requestMethod;
		this.responseContent = responseContent;
		this.responseMessage = responseMessage;
	}


	public int getHttpCode() {
		return httpCode;
	}

	public String getRequestPath() {
		return requestPath;
	}

	public String getRequestMethod() {
		return requestMethod;
	}

	public String getResponseContent() {
		return responseContent;
	}

	public String getResponseMessage() {
		return responseMessage;
	}
}
