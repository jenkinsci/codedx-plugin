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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;



/**
 * A RESTful client used to access the various API end-points exposed by CodeDx.
 *  
 * @author anthonyd
 *
 */
public class CodeDxClient {

	private final String KEY_HEADER  = "API-Key";
	
	private String key;
	private String url;

	private DefaultHttpClient httpClient;
	
	private Gson gson;
	
	/**
	 * Creates a new client, ready to be used for communications with CodeDx.
	 * @param url URL of the CodeDx web application.  The '/api' part of the URL is optional. 
	 * @param key The API key.  Note that permissions must be set for this key on CodeDx admin page.
	 */
	public CodeDxClient(String url,String key){
		
		this.key = key;

		if(url == null)
			throw new NullPointerException("Argument url is null");

		if(key == null)
			throw new NullPointerException("Argument key is null");

		if(!url.endsWith("/")){
			
			url = url + "/";
		}
		
		if(!url.endsWith("api/")){
			
			url = url + "api/";
		}
		
		this.url = url;
		
		httpClient = new DefaultHttpClient();
		
		gson = new Gson();
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
		
		GetProjectsResponse response = doGet("projects",new TypeToken<GetProjectsResponse>(){}.getType());
		
		return response.getProjects(); 
	}
	
	/**
	 * Retreives a specific project from CodeDx
	 * 
	 * @param id The project ID
	 * @return A project
	 * @throws CodeDxClientException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public Project getProject(int id) throws CodeDxClientException, ClientProtocolException, IOException{

		return doGet("projects/id",new TypeToken<Project>(){}.getType());
	}
	
	/**
	 * A generic get that will marshal JSON data into some type.
	 * @param path Append this to the URL
	 * @param typeOfT
	 * @return Something of type T
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws CodeDxClientException
	 */
	private <T> T doGet(String path, Type typeOfT) throws ClientProtocolException, IOException, CodeDxClientException {
		
		HttpGet getRequest = new HttpGet(url + path);
		getRequest.addHeader(KEY_HEADER,key);

		HttpResponse response = httpClient.execute(getRequest);
		
		int responseCode = response.getStatusLine().getStatusCode();
		
		if(responseCode != 200){
			
			throw new CodeDxClientException("failed to get from: " + path,responseCode);
		}
		
		String data = IOUtils.toString(response.getEntity().getContent());
		
		return gson.<T>fromJson(data,typeOfT);
	}
	
	/**
	 * Kicks off a CodeDx analysis run on a specified project
	 * 
	 * @param projectId The project ID
	 * @param artifacts An array of streams to send over as analysis artifacts
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws CodeDxClientException
	 */
	public void startAnalysis(int projectId, InputStream[] artifacts) throws ClientProtocolException, IOException, CodeDxClientException {
		
		HttpPost postRequest = new HttpPost(url + "projects/" + projectId + "/analysis");
		postRequest.addHeader(KEY_HEADER,key);

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);


		for(InputStream artifact : artifacts){
			
			builder.addPart("file[]", new InputStreamBody(artifact,"file[]"));
		}
		
		HttpEntity entity = builder.build();
		
		postRequest.setEntity(entity);
		
		HttpResponse response = httpClient.execute(postRequest);
		
		int responseCode = response.getStatusLine().getStatusCode();
		
		if(responseCode != 202){
			
			System.out.println(IOUtils.toString(response.getEntity().getContent()));
			
			throw new CodeDxClientException("failed to start analysis.", responseCode);
		}
		
		//Eventually this method should return a Job ID but this is not yet supported by the API
		
	}
}

