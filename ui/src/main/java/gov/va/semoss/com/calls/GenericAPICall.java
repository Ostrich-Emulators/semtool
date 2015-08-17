package gov.va.semoss.com.calls;

import gov.va.semoss.com.WebServerAPI;

import java.util.HashMap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

public abstract class GenericAPICall {
	
	protected static final Logger log = Logger.getLogger(GenericAPICall.class);
	
	protected String name;
	
	protected HttpMethodParams parameters = new HttpMethodParams();

	protected String errorMessage;
	
	protected int statusCode;
	
	protected HashMap<String, Object> returnedData = new HashMap<String, Object>();
	
	protected GenericAPICall(String name){
		this.name = name;
	}
	
	public abstract boolean execute(HttpClient client, String host, int port, 
			String endpoint);
	
	public void setParameters(HttpMethodParams parameters){
		this.parameters = parameters;
	}

	public String getName(){
		return this.name;
	}
	
	public String getErrorMessage(){
		return this.errorMessage;
	}
	
	public int getStatusCode(){
		return this.statusCode;
	}
	
	public Object getData(String key){
		return returnedData.get(key);
	}
	
	protected void setData(String key, Object value){
		this.returnedData.put(key, value);
	}
	
}
