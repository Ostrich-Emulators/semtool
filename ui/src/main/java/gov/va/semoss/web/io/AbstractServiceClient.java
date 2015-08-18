package gov.va.semoss.web.io;


import gov.va.semoss.com.RestAuthenticator;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

public class AbstractServiceClient {
	
	protected final Map<String, UPPair> auths = new HashMap<>();
	/** The logger for this class */
	protected static final Logger log = Logger.getLogger( AbstractServiceClient.class );
	
	public static final String PROTOCOL = "http";
	
	public static final String HOST = "localhost";
	
	public static final int PORT = 8080;
	
	public static final String APPLICATION_CONTEXT = "semoss";
	
	@Autowired
	protected RestTemplate rest;
	
	
	public final void setAuthentication( String url, String username, char[] pass ){
		auths.put( url, new UPPair( username, pass ) );
	}
	
	protected HttpEntity<String> prepareHeaders(String serviceURL){
		UPPair authPair = auths.get(serviceURL);
		String username = null;
		String password = null;
		if (authPair == null){
			log.error("Call to 'Get Databases received, but auth values not set.'");
			return null;
		}
		else {
			username = authPair.u;
			password = new String(authPair.p);
			HttpEntity<String> request = RestAuthenticator.instance().getEntity(username, new String(password));
			return request;
		}
	}
	
	protected class UPPair {

		public final String u;
		public final char[] p;

		public UPPair( String u, char[] p ) {
			this.u = u;
			this.p = p;
		}
	}
}
