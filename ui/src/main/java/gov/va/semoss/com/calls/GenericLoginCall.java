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

public class GenericLoginCall extends GenericAPICall {

	public static final String USERNAME = "USERNAME";
	
	public static final String PASSWORD = "PASSWORD";
	
	protected GenericLoginCall(String username, String password) {
		super("login");
		this.parameters.setParameter(USERNAME, username);
		this.parameters.setParameter(PASSWORD, password);
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
				new UsernamePasswordCredentials((String)this.parameters.getParameter(USERNAME), 
						(String)this.parameters.getParameter(PASSWORD)));
		PostMethod post = new PostMethod();
		GetMethod get = new GetMethod();
		try {
			HttpURL getURL = new HttpURL(host, port, "/semoss-webserver/login");
			get.setURI(getURL);
			int loginStatus = client.executeMethod(get);
			String content = get.getResponseBodyAsString();
			int crsfIndex = content.indexOf("name=\"_csrf\" type=\"hidden\" value=\"");
			String csrf = content.substring(crsfIndex + 34, crsfIndex + 34 + 36);
			this.parameters.setParameter("_csrf", csrf);
			this.parameters.setParameter("submit", "Login");
			String paramsString = getParamString();
			HttpURL url = new HttpURL(host, port, "/semoss-webserver/" + endpoint + paramsString);
			post.setURI(url);
			//post.setParams(this.parameters);
			post.setDoAuthentication(true);
			post.addRequestHeader("X-CSRF-TOKEN", csrf);
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
	
	
	private String getParamString(){
		StringBuilder content = new StringBuilder();
		content.append("?");
		content.append("username=" + this.parameters.getParameter(USERNAME));
		content.append("&password=" + this.parameters.getParameter(PASSWORD));
		content.append("&_csrf=" + this.parameters.getParameter("_csrf"));
		content.append("&submit=Login");
		return content.toString();
	}
	
	

}
