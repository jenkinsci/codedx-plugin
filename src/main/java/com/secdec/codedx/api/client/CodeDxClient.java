/*
 *
 * Copyright 2014 Applied Visions
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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.Map;


/**
 * A RESTful client used to access the various API end-points exposed by CodeDx.
 *
 * @author anthonyd
 *
 */
public class CodeDxClient {

	private final String KEY_HEADER  = "API-Key";

	protected String key;
	protected String url;
	private String serverUrl;

	protected HttpClientBuilder httpClientBuilder;

	private Gson gson;

	/**
	 * Creates a new client, ready to be used for communications with CodeDx.
	 * @param url URL of the CodeDx web application.  The '/api' part of the URL is optional.
	 * @param key The API key.  Note that permissions must be set for this key on CodeDx admin page.
	 */
	public CodeDxClient(String url, String key){
		this(url, key, HttpClientBuilder.create());
	}

	/**
	 * Creates a new client, ready to be used for communications with CodeDx.
	 * @param url URL of the CodeDx web application.  The '/api' part of the URL is optional.
	 * @param key The API key.  Note that permissions must be set for this key on CodeDx admin page.
	 * @param clientBuilder an HttpClientBuilder that can handle the certificate used by the server
	 */
	public CodeDxClient(String url, String key, HttpClientBuilder clientBuilder){

		this.key = key;

		if(url == null)
			throw new NullPointerException("Argument url is null");

		if(key == null)
			throw new NullPointerException("Argument key is null");

		url = url.trim();

		if(!url.endsWith("/")){

			url = url + "/";
		}

		if(!url.endsWith("api/")){

			url = url + "api/";
		}

		this.url = url;
		this.serverUrl = url.replace("/api/", "/");

		httpClientBuilder = clientBuilder;

		gson = new Gson();
	}

	@Deprecated
	public String buildBrowsableAnalysisRunUrl(int projectId){

		return serverUrl + "projects/" + projectId;
	}

	public String buildLatestFindingsUrl(int projectId){

		return serverUrl + "projects/" + projectId;
	}

	/**
	 * Retrieves a list of projects from CodeDx.
	 *
	 * @return Project list
	 *
	 * @throws CodeDxClientException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public List<Project> getProjects() throws CodeDxClientException, IOException{
		GetProjectsResponse response = doHttpRequest(
			new HttpGet(),
			"projects",
			false,
			new TypeToken<GetProjectsResponse>(){}.getType(),
			null
		);
		return response.getProjects();
	}

	/**
	 * Retrieves a specific project from CodeDx
	 *
	 * @param id The project ID
	 * @return A project
	 * @throws CodeDxClientException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public Project getProject(int id) throws CodeDxClientException, IOException{
		return doHttpRequest(
			new HttpGet(),
			"projects/" + id,
			true,
			new TypeToken<Project>(){}.getType(),
			null
		);
	}


	/**
	 * Retrieves all Triage statuses for a given project.
	 *
	 * @param id The project ID
	 * @return A map from status code String to TriageStatus
	 * @throws CodeDxClientException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public Map<String,TriageStatus> getTriageStatuses(int id) throws CodeDxClientException, IOException{
		return doHttpRequest(
			new HttpGet(),
			"projects/" + id + "/statuses",
			true,
			new TypeToken<Map<String,TriageStatus>>(){}.getType(),
			null
		);
	}


	/**
	 * Retrieves a specific job from CodeDx
	 *
	 * @param id The job ID
	 * @return A Job
	 * @throws CodeDxClientException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public Job getJob(String id) throws CodeDxClientException, IOException{
		return doHttpRequest(
			new HttpGet(),
			"jobs/" + id,
			false,
			new TypeToken<Job>(){}.getType(),
			null
		);
	}

	/**
	 * Retrieves a job status from CodeDx.  This is a convenience method for polling that
	 * relies on getJob.
	 *
	 * @param id The job ID
	 * @return The Job Status
	 * @throws CodeDxClientException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public String getJobStatus(String id) throws CodeDxClientException, IOException{
		return getJob(id).getStatus();
	}

	/**
	 * Retrieves the total findings count for a given run.
	 *
	 * @param id The run ID
	 * @return The count
	 * @throws CodeDxClientException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public int getFindingsCount(String id) throws CodeDxClientException, IOException{
		CountResponse resp = doHttpRequest(
			new HttpGet(),
			"runs/" + id + "/findings/count",
			true,
			new TypeToken<CountResponse>(){}.getType(),
			null
		);
		return resp.getCount();
	}

	/**
	 * Retrieves the total findings count for a given project using the provided Filter
	 *
	 * @param projectId The project ID
	 * @param filter A Filter object (set to null to not filter)
	 * @return The count
	 * @throws CodeDxClientException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public int getFindingsCount(int projectId, Filter filter) throws CodeDxClientException, IOException{
		CountResponse resp = doHttpRequest(
			new HttpPost(),
			"projects/" + projectId + "/findings/count",
			true,
			new TypeToken<CountResponse>(){}.getType(),
			new CountRequest(filter)
		);
		return resp.getCount();
	}

	/**
	 * Retrieves an array of CountGroups using the provided Filter and countBy field name.
	 *
	 * @param projectId The project ID
	 * @param filter A Filter object
	 * @param countBy The field to group the counts by
	 * @return A list of CountGroups
	 * @throws CodeDxClientException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public List<CountGroup> getFindingsGroupedCounts(int projectId, Filter filter, String countBy) throws CodeDxClientException, IOException{
		return doHttpRequest(
			new HttpPost(),
			"projects/" + projectId + "/findings/grouped-counts",
			true,
			new TypeToken<List<CountGroup>>(){}.getType(),
			new GroupedCountRequest(filter,countBy)
		);
	}

	public void setAnalysisName(int projectId, int analysisId, String name) throws IOException, CodeDxClientException {
		JsonObject reqBody = new JsonObject();
		reqBody.addProperty("name", name);
		doHttpRequest(
			new HttpPut(),
			"projects/" + projectId + "/analyses/" + analysisId,
			true,
			null,
			reqBody
		);
	}

	/**
	 * Perform an HttpRequest to the given api path, with an optional request body, and parse the response
	 * @param request Generally a new `HttpGet`, `HttpPost`, or `HttpPut`
	 * @param path The relative API path (not including /x/ or /api/)
	 * @param isXApi Flag that determines whether the request will prepend /x/ or /api/ to the path (true = /x/)
	 * @param responseType A type instance that helps `gson` parse the response body
	 * @param requestBody An optional payload that will be converted to json and sent with the request
	 * @param <T> Type parameter that determines the parsed response type
	 * @return The parsed response
	 * @throws IOException If the underlying IO goes wrong
	 * @throws CodeDxClientException For non 2xx response codes
	 */
	protected <T> T doHttpRequest(HttpRequestBase request, String path, boolean isXApi, Type responseType, Object requestBody) throws IOException, CodeDxClientException {
		// set the request path (as a URI), with a different relative path depending on the `isXApi` flag
		String apiPath = (isXApi ? url.replace("/api/", "/x/") : url) + path;
		request.setURI(URI.create(apiPath));

		// set authentication headers
		request.addHeader(KEY_HEADER, key);

		// if a request body is provided, and the request is able to add entities, JSONify it and use it as the request "entity"
		if(requestBody != null && request instanceof HttpEntityEnclosingRequest){
			HttpEntityEnclosingRequest entityRequest = (HttpEntityEnclosingRequest) request;
			HttpEntity requestBodyEntity = new StringEntity(gson.toJson(requestBody));
			entityRequest.setEntity(requestBodyEntity);
		}

		// run the request (this requires a client; both the client and the response must be closed afterwards)
		try(
			CloseableHttpClient client = httpClientBuilder.build();
			CloseableHttpResponse response = client.execute(request);
		){
			// get the response code and body
			int responseCode = response.getStatusLine().getStatusCode();
			boolean isSuccess = (responseCode / 100) == 2;
			String responseBody = response.getEntity() == null ? null : IOUtils.toString(response.getEntity().getContent());

			// for 2xx responses, parse the body; for others, throw an exception
			if(isSuccess){
				// 200 = OK, so parse the response as JSON, then convert to a model using `gson`
				if(responseType == null || responseBody == null){
					return null;
				} else {
					return gson.<T>fromJson(responseBody, responseType);
				}
			} else {
				// non-200 = error, so collect some helpful information as an exception
				throw new CodeDxClientException(
					request.getMethod(),
					apiPath,
					response.getStatusLine().getReasonPhrase(),
					responseCode,
					responseBody
				);
			}
		}
	}

	/**
	 * Kicks off a CodeDx analysis run on a specified project
	 *
	 * @return A StartAnalysisResponse object
	 * @param projectId The project ID
	 * @param artifacts An array of streams to send over as analysis artifacts
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws CodeDxClientException
	 *
	 */
	public StartAnalysisResponse startAnalysis(int projectId, Map<String, InputStream> artifacts) throws IOException, CodeDxClientException {
		String path = "projects/" + projectId + "/analysis";
		HttpPost postRequest = new HttpPost(url + path);
		postRequest.addHeader(KEY_HEADER, key);

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();

		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

		for(String artifactName : artifacts.keySet()){
			builder.addPart("file[]", new InputStreamBody(artifacts.get(artifactName), artifactName));
		}

		HttpEntity entity = builder.build();

		postRequest.setEntity(entity);

		HttpResponse response = httpClientBuilder.build().execute(postRequest);

		int responseCode = response.getStatusLine().getStatusCode();

		if(responseCode != 202){
			throw new CodeDxClientException("POST", path, response.getStatusLine().getReasonPhrase(), responseCode, IOUtils.toString(response.getEntity().getContent()));
		}

		String data = IOUtils.toString(response.getEntity().getContent());

		return gson.<StartAnalysisResponse>fromJson(data,new TypeToken<StartAnalysisResponse>(){}.getType());
	}
}

