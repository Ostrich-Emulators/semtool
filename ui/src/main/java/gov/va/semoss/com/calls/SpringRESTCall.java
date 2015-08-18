package gov.va.semoss.com.calls;

import java.util.HashMap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.log4j.Logger;

import gov.va.semoss.web.io.ServiceClient;

public abstract class SpringRESTCall extends ServiceClient {

	protected static final Logger log = Logger.getLogger(SpringRESTCall.class);
	
	protected String name;

	protected String errorMessage;
	
	protected HashMap<String, Object> parameters = new HashMap<String, Object>();
	
	protected int statusCode;
	
	protected Object returnedData = null;
	
	protected static String CONTEXT = "semoss";
	
	protected SpringRESTCall(String name){
		this.name = name;
	}

	public abstract boolean execute(String host, int port, 
			String endpoint);
	
	public void setParameter(String name, Object value){
		this.parameters.put(name, value);
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
	
	public Object getData(){
		return returnedData;
	}

}