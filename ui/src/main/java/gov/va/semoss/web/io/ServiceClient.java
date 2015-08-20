/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.web.io;

import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/**
 *
 * @author ryan
 */
@Component
public class ServiceClient extends AbstractServiceClient {

	private static final Logger log = Logger.getLogger( ServiceClient.class );

	/**
	 *
	 * @param serviceurl
	 * @return
	 * @throws RestClientException if there is a network/password problem
	 */
	public DbInfo[] getDbs( String serviceurl ) throws RestClientException {
		HttpEntity<String> request = prepareHeaders( serviceurl );

		ResponseEntity<DbInfo[]> response = rest.exchange( serviceurl,
				HttpMethod.GET, request, DbInfo[].class );
		DbInfo[] dbs = response.getBody();
		return dbs;
	}
}
