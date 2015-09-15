/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.web.io;

import gov.va.semoss.user.RemoteUserImpl;
import gov.va.semoss.user.User;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author ryan
 */
@Component
public class ServiceClientImpl extends AbstractServiceClient {

	private static final Logger log = Logger.getLogger( ServiceClient.class );

	public ServiceClientImpl(){
		
	}
	
	public ServiceClientImpl( RestTemplate rt ){
		setRestTemplate( rt );
	}
	
	@Override
	public DbInfo[] getDbs( SemossService svc ) throws RestClientException {
		DbInfo[] dbs = rest.getForObject( svc.databases(), DbInfo[].class );
		return dbs;
	}

	@Override
	public User getUser( SemossService svc ) throws RestClientException {
		User user = rest.getForObject( svc.user(), RemoteUserImpl.class );
		return user;
	}
}
