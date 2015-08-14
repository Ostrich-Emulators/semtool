/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.web.io;

/**
 *
 * @author ryan
 */
public class DbInfo {
	private String name;
	private String dataUrl;
	private String insightsUrl;

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
	}
	
	public String getDataUrl() {
		return dataUrl;
	}

	public void setDataUrl( String dataUrl ) {
		this.dataUrl = dataUrl;
	}

	public String getInsightsUrl() {
		return insightsUrl;
	}

	public void setInsightsUrl( String insightsUrl ) {
		this.insightsUrl = insightsUrl;
	}
}
