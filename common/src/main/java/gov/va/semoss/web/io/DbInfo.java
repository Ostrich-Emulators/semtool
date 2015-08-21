/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.web.io;

import java.util.ArrayList;
import java.util.List;

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
}