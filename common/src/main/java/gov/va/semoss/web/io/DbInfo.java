/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.web.io;

import java.util.ArrayList;

/**
 *
 * @author ryan
 */
public class DbInfo {
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
	
	public DbInfo(){
		
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
	
	public static DbInfo[] getTestDatabases() {
		ArrayList<DbInfo> testDbs = new ArrayList<DbInfo>();
		
		testDbs.add(new DbInfo(
				"thisKB", 
				"http://www.storagePlace.gov/choiceOne/serverURI", 
				"http://www.storagePlace.gov/choiceOne/databaseURI", 
				"http://www.storagePlace.gov/choiceOne/insightURI"
			));

		testDbs.add(new DbInfo(
				"thatKB", 
				"http://www.storagePlace.gov/choiceTwo/serverURI", 
				"http://www.storagePlace.gov/choiceTwo/databaseURI", 
				"http://www.storagePlace.gov/choiceTwo/insightURI"
				));
	
		testDbs.add(new DbInfo(
				"theOtherKB", 
				"http://www.storagePlace.gov/choiceThree/serverURI", 
				"http://www.storagePlace.gov/choiceThree/databaseURI", 
				"http://www.storagePlace.gov/choiceThree/insightURI"
				));
		
		return testDbs.toArray( new DbInfo[testDbs.size()] );
	}
	

	/*
	 * Returns all current DB info we have access to. Currently it returns test data
	 * but should be updated to hit a configuration file or to hit a data store. JPM 08/18/2015
	 */
	public static DbInfo[] getAllDBs() {
		return DbInfo.getTestDatabases();
	}
}