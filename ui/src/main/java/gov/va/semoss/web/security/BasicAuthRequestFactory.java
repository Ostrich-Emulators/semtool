/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.web.security;

import java.net.URI;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.protocol.HttpContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

/**
 *
 * @author ryan
 */
public class BasicAuthRequestFactory extends HttpComponentsClientHttpRequestFactory {

	private final AuthCache authCache = new BasicAuthCache();
	private final BasicCredentialsProvider credentials = new BasicCredentialsProvider();

	public void cache( HttpHost host, String username, char[] password ) {
		// Generate BASIC scheme object and add it to the local auth cache
		BasicScheme basicAuth = new BasicScheme();
		authCache.put( host, basicAuth );

		credentials.setCredentials( new AuthScope( host ),
				new UsernamePasswordCredentials( username, new String( password ) ) );
	}

	@Override
	protected HttpContext createHttpContext( HttpMethod httpMethod, URI uri ) {
		HttpClientContext localcontext = HttpClientContext.create();
		localcontext.setAuthCache( authCache );
		localcontext.setCredentialsProvider( credentials );
		return localcontext;
	}
}
