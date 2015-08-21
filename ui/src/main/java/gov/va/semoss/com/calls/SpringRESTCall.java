package gov.va.semoss.com.calls;

import java.util.HashMap;

import org.apache.log4j.Logger;

import gov.va.semoss.web.io.ServiceClientImpl;
import java.util.Map;

public abstract class SpringRESTCall extends ServiceClientImpl {

	protected static final Logger log = Logger.getLogger(SpringRESTCall.class);
	
	protected String name;

	protected String errorMessage;
	
	protected Map<String, Object> parameters = new HashMap<>();
	
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