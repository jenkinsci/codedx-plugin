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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.tools.ant.taskdefs.condition.Http;


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
	public CodeDxClient(String url,String key){
		this(url, key,HttpClientBuilder.create());
	}

	/**
	 * Creates a new client, ready to be used for communications with CodeDx.
	 * @param url URL of the CodeDx web application.  The '/api' part of the URL is optional.
	 * @param key The API key.  Note that permissions must be set for this key on CodeDx admin page.
	 * @param clientBuilder an HttpClientBuilder that can handle the certificate used by the server
	 */
	public CodeDxClient(String url,String key, HttpClientBuilder clientBuilder){

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
	public List<Project> getProjects() throws CodeDxClientException, ClientProtocolException, IOException{
		
		GetProjectsResponse response = doGet("projects",new TypeToken<GetProjectsResponse>(){}.getType(),false);
		
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
	public Project getProject(int id) throws CodeDxClientException, ClientProtocolException, IOException{

		return doGet("projects/" + id,new TypeToken<Project>(){}.getType(),true);
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
	public Map<String,TriageStatus> getTriageStatuses(int id) throws CodeDxClientException, ClientProtocolException, IOException{

		return doGet("projects/" + id + "/statuses",new TypeToken<Map<String,TriageStatus>>(){}.getType(),true);
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
	public Job getJob(String id) throws CodeDxClientException, ClientProtocolException, IOException{

		return doGet("jobs/" + id,new TypeToken<Job>(){}.getType(),false);
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
	public String getJobStatus(String id) throws CodeDxClientException, ClientProtocolException, IOException{

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
	public int getFindingsCount(String id) throws CodeDxClientException, ClientProtocolException, IOException{

		CountResponse resp = doGet("runs/" + id + "/findings/count",new TypeToken<CountResponse>(){}.getType(),true);
		
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
	public int getFindingsCount(int projectId, Filter filter) throws CodeDxClientException, ClientProtocolException, IOException{

		CountResponse resp = doPost("projects/" + projectId + "/findings/count",new TypeToken<CountResponse>(){}.getType(),new CountRequest(filter),true);
		
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
	public List<CountGroup> getFindingsGroupedCounts(int projectId, Filter filter, String countBy) throws CodeDxClientException, ClientProtocolException, IOException{

		return doPost("projects/" + projectId + "/findings/grouped-counts",new TypeToken<List<CountGroup>>(){}.getType(),new GroupedCountRequest(filter,countBy),true);
	}
	
	/**
	 * A generic get that will marshal JSON data into some type.
	 * @param path Append this to the URL
	 * @param typeOfT
	 * @param experimental If this request is part of the experimental API
	 * @return Something of type T
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws CodeDxClientException
	 */
	protected <T> T doGet(String path, Type typeOfT, boolean experimental) throws ClientProtocolException, IOException, CodeDxClientException {
		
		HttpGet getRequest;
		
		if(experimental){
		
			getRequest = new HttpGet(url.replace("/api/", "/x/") + path);
		}
		else{
			
			getRequest = new HttpGet(url + path);
		}

		getRequest.addHeader(KEY_HEADER, key);

		HttpResponse response = httpClientBuilder.build().execute(getRequest);
		
		int responseCode = response.getStatusLine().getStatusCode();
		
		if(responseCode != 200){
			
			throw new CodeDxClientException("GET", path, response.getStatusLine().getReasonPhrase(), responseCode, IOUtils.toString(response.getEntity().getContent()));
		}
		
		String data = IOUtils.toString(response.getEntity().getContent());
		
		return gson.<T>fromJson(data,typeOfT);
	}
	
	
	/**
	 * A generic post that will send a payload object and then
	 * marshal a JSON data response into some type.
	 * @param path Append this to the URL
	 * @param typeOfT
	 * @param payload Data to send
	 * @param experimental If this request is part of the experimental API
	 * @return Something of type T
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws CodeDxClientException
	 */
	private <T> T doPost(String path, Type typeOfT, Object payload, boolean experimental) throws ClientProtocolException, IOException, CodeDxClientException {
		
		HttpPost postRequest;
		
		if(experimental){
		
			postRequest = new HttpPost(url.replace("/api/", "/x/") + path);
		}
		else{
			
			postRequest = new HttpPost(url + path);
		}

		postRequest.addHeader(KEY_HEADER, key);
		
		postRequest.setEntity(new StringEntity(gson.toJson(payload)));
		
		HttpResponse response = httpClientBuilder.build().execute(postRequest);
		
		int responseCode = response.getStatusLine().getStatusCode();
		
		if(responseCode != 200){
			
			throw new CodeDxClientException("POST", path, response.getStatusLine().getReasonPhrase(), responseCode, IOUtils.toString(response.getEntity().getContent()));
		}
		
		String data = IOUtils.toString(response.getEntity().getContent());
		
		return gson.<T>fromJson(data,typeOfT);
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
	public StartAnalysisResponse startAnalysis(int projectId, Map<String, InputStream> artifacts) throws ClientProtocolException, IOException, CodeDxClientException {
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

