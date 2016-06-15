/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.web.io;

import com.ostrichemulators.semtool.user.RemoteUserImpl;
import com.ostrichemulators.semtool.user.User;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author ryan
 */
@Component
public class SemtoolClientImpl extends AbstractServiceClient {

	private static final Logger log = Logger.getLogger( ServiceClient.class );

	public SemtoolClientImpl() {

	}

	public SemtoolClientImpl( RestTemplate rt, String url ) {
		setRestTemplate( rt );
		setRoot( url );
	}

	@Override
	public DbInfo[] getDbs() throws RestClientException {
		String url = getRoot() + "/databases/";

		DbInfo[] dbs = rest.getForObject( url, DbInfo[].class );
		return dbs;
	}

	@Override
	public User getUser() throws RestClientException {
		String url = getRoot() + "/login?whoami";
		User user = rest.getForObject( url, RemoteUserImpl.class );
		return user;
	}
}
