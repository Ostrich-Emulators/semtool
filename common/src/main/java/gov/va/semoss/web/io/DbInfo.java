/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.web.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ryan
 */
public class DbInfo {
	public final static String NAME_KEY = "name";
	public final static String SERVERURL_KEY = "serverUrl";
	public final static String DATAURL_KEY = "dataUrl";
	public final static String INSIGHTSURL_KEY = "insightsUrl";
	
	private String name;
	private String serverUrl;
	private String dataUrl;
	private String insightsUrl;

	public DbInfo(String name, String serverUrl, String dataUrl, String insightsUrl) {
		this.name = name;
		this.serverUrl = serverUrl;
		this.dataUrl = dataUrl;
		this.insightsUrl = insightsUrl;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public String getDataUrl() {
		return dataUrl;
	}

	public void setDataUrl(String dataUrl) {
		this.dataUrl = dataUrl;
	}

	public String getInsightsUrl() {
		return insightsUrl;
	}

	public void setInsightsUrl(String insightsUrl) {
		this.insightsUrl = insightsUrl;
	}
	
	public Map<String, Object> getAsMap() {
		HashMap<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put(NAME_KEY, name);
		returnMap.put(SERVERURL_KEY, serverUrl);
		returnMap.put(DATAURL_KEY, dataUrl);
		returnMap.put(INSIGHTSURL_KEY, insightsUrl);
		
		return returnMap;
	}
	
	public static DbInfo getEmptyDatabase() {
		return new DbInfo(
				"invalidID", 
				"http://www.invalidID.gov/", 
				"http://www.invalidID.gov/", 
				"http://www.invalidID.gov/"
				);
	}
	
	public static ArrayList<Map<String, Object>> getTestDatabases() {
		ArrayList<Map<String, Object>> knowledgeBases = new ArrayList<Map<String, Object>>();
		
		knowledgeBases.add(new DbInfo(
				"thisKB", 
				"http://www.storagePlace.gov/choiceOne/serverURI", 
				"http://www.storagePlace.gov/choiceOne/databaseURI", 
				"http://www.storagePlace.gov/choiceOne/insightURI"
			).getAsMap());

		knowledgeBases.add(new DbInfo(
				"thatKB", 
				"http://www.storagePlace.gov/choiceTwo/serverURI", 
				"http://www.storagePlace.gov/choiceTwo/databaseURI", 
				"http://www.storagePlace.gov/choiceTwo/insightURI"
			).getAsMap());
	
		knowledgeBases.add(new DbInfo(
				"theOtherKB", 
				"http://www.storagePlace.gov/choiceThree/serverURI", 
				"http://www.storagePlace.gov/choiceThree/databaseURI", 
				"http://www.storagePlace.gov/choiceThree/insightURI"
			).getAsMap());
		
		return knowledgeBases;
	}
}