/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.web.io;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author ryan
 */
@Component
public class ServiceClient extends AbstractServiceClient {
	
	public DbInfo[] getDbs( String serviceurl) {
			HttpEntity<String> request = prepareHeaders(serviceurl);
			ResponseEntity<DbInfo[]> response = (new RestTemplate()).exchange(serviceurl, HttpMethod.GET, request, DbInfo[].class);
			DbInfo[] dbs = response.getBody();
			return dbs;
		
	}
}
