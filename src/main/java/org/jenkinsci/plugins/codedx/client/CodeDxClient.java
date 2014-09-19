package org.jenkinsci.plugins.codedx.client;

import java.io.IOException;
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



public class CodeDxClient {

	private final String KEY_HEADER  = "API-Key";
	
	private String key;
	private String url;

	private DefaultHttpClient httpClient;
	
	private Gson gson;
	
	public CodeDxClient(String url,String key){
		
		this.key = key;

		if(url == null)
			throw new NullPointerException("Argument url is null");

		if(key == null)
			throw new NullPointerException("Argument key is null");

		if(!url.endsWith("/")){
			
			url = url + "/";
		}
		
		this.url = url;
		
		httpClient = new DefaultHttpClient();
		
		gson = new Gson();
	}
	
	public List<Project> getProjects() throws CodeDxClientException, ClientProtocolException, IOException{
		
		GetProjectsResponse response = doGet("projects",new TypeToken<GetProjectsResponse>(){}.getType());
		
		return response.getProjects(); 
	}
	
	public Project getProject(int id) throws CodeDxClientException, ClientProtocolException, IOException{

		return doGet("projects/id",new TypeToken<Project>(){}.getType());
	}
	
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
	
	public int startAnalysis(int projectId, AnalysisArtifact[] artifacts) throws ClientProtocolException, IOException, CodeDxClientException {
		
		HttpPost postRequest = new HttpPost(url + "projects/" + projectId + "/analysis");
		postRequest.addHeader(KEY_HEADER,key);

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);


		for(AnalysisArtifact artifact : artifacts){
			
			builder.addPart("file[]", new InputStreamBody(artifact.createInputStream(),"file[]"));
		}
		
		HttpEntity entity = builder.build();
		
		postRequest.setEntity(entity);
		
		HttpResponse response = httpClient.execute(postRequest);
		
		int responseCode = response.getStatusLine().getStatusCode();
		
		if(responseCode != 202){
			
			System.out.println(IOUtils.toString(response.getEntity().getContent()));
			
			throw new CodeDxClientException("failed to start analysis.", responseCode);
		}
		
		return 0;
	}
}

