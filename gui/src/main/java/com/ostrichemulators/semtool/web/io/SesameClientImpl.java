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
public class SesameClientImpl extends AbstractServiceClient {

	private static final Logger log = Logger.getLogger( ServiceClient.class );

	public SesameClientImpl() {

	}

	public SesameClientImpl( RestTemplate rt, String url ) {
		setRestTemplate( rt );
		setRoot( url );
	}

	@Override
	public DbInfo[] getDbs() throws RestClientException {
		return new DbInfo[0];
//		String url = getRoot();
//		RemoteRepositoryManager rrm = null;
//		try {
//			rrm = RemoteRepositoryManager.getInstance( url, getUsername(), getPassword() );
//			rrm.initialize();
//			Collection<RepositoryInfo> infos = rrm.getAllRepositoryInfos( true );
//			DbInfo[] dbs = new DbInfo[infos.size()];
//			int i = 0;
//			for ( RepositoryInfo ri : infos ) {
//				DbInfo info = new DbInfo( ri.getDescription(), url, ri.getLocation().toExternalForm(), null );
//				dbs[i++] = info;
//			}
//			return dbs;
//		}
//		catch ( RepositoryException re ) {
//			log.error( re, re );
//			throw new RestClientException( "could not connect to server: " + url );
//		}
//		finally {
//			if ( null != rrm ) {
//				try {
//					rrm.shutDown();
//				}
//				catch ( Exception e ) {
//					log.warn( e, e );
//				}
//			}
//		}
	}

	@Override
	public User getUser() throws RestClientException {
		return new RemoteUserImpl( getUsername() );
	}
}
