/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.web.io;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author ryan
 */
@Component
public class ServiceClient {

	private final Map<String, UPPair> auths = new HashMap<>();
	@Autowired
	protected RestTemplate rest;
	
	public final void setAuthentication( String url, String username, char[] pass ){
		auths.put( url, new UPPair( username, pass ) );
	}
	
	public DbInfo[] getDbs( String serviceurl ) {
		DbInfo dbs[] = rest.getForObject( serviceurl, DbInfo[].class );
		return dbs;
	}

	private class UPPair {

		public final String u;
		public final char[] p;

		public UPPair( String u, char[] p ) {
			this.u = u;
			this.p = p;
		}
	}
}
