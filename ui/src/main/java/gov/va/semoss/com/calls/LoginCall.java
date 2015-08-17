package gov.va.semoss.com.calls;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpHost;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class LoginCall extends APICall {

	public static final String USERNAME = "USERNAME";
	
	public static final String PASSWORD = "PASSWORD";
	
	protected LoginCall(String username, String password) {
		super("login");
		this.parameters.put(USERNAME, username);
		this.parameters.put(PASSWORD, password);
	}

	@Override
	public boolean execute(HttpClient client, String host, int port,
			String endpoint) {
		List<String> authPrefs = new ArrayList<String>(2);
		authPrefs.add(AuthPolicy.DIGEST);
		authPrefs.add(AuthPolicy.BASIC);
		client.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);
		client.getParams().setAuthenticationPreemptive(true);
		client.getState().setCredentials(new AuthScope(host, port, AuthScope.ANY_REALM),
				new UsernamePasswordCredentials((String)this.parameters.get(USERNAME), 
						(String)this.parameters.get(PASSWORD)));
		
		
		// Tell the GET method to automatically handle authentication. The
		// method will use any appropriate credentials to handle basic
		// authentication requests. Setting this value to false will cause
		// any request for authentication to return with a status of 401.
		// It will then be up to the client to handle the authentication.
		PostMethod post = new PostMethod();
		GetMethod get = new GetMethod();
		try {
			HttpURL getURL = new HttpURL(host, port, "/semoss-webserver/login");
			get.setURI(getURL);
			int loginStatus = client.executeMethod(get);
			String content = get.getResponseBodyAsString();
			int crsfIndex = content.indexOf("name=\"_csrf\" type=\"hidden\" value=\"") + 38;
			
			String csrf = "";
			HttpURL url = new HttpURL(host, port, "/semoss-webserver/" + endpoint);
			post.setURI(url);
			HttpMethodParams params = new HttpMethodParams(this.parameters);
			params.set
			post.setParams(params);
			post.setParameter("_csrf", );
			
			
			username=ryan&password=123456&submit=Login&_csrf=d3073e79-b02b-4f87-9a00-9e4393558182
			post.setDoAuthentication(true);
			// execute the GET
			int status = client.executeMethod(post);
			String result = status + client.getState().toString() + "\n" + post.getStatusLine()
					+ post.getResponseBodyAsString();
			log.info(result);
			return true;
		} catch (HttpException e) {
			log.error("Error attempting to login", e);
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			// release any connection resources used by the method
			post.releaseConnection();
		}
		return true;
	}
	
	

}
