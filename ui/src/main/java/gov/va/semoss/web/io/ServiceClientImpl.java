/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.web.io;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/**
 *
 * @author ryan
 */
@Component
public class ServiceClientImpl extends AbstractServiceClient {

	private static final Logger log = Logger.getLogger( ServiceClient.class );

	@Override
	public DbInfo[] getDbs( String serviceurl ) throws RestClientException {
		DbInfo[] dbs = rest.getForObject( serviceurl, DbInfo[].class );
		return dbs;
	}
}
