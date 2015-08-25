/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.web.io;

import org.springframework.web.client.RestClientException;

/**
 *
 * @author ryan
 */
public interface ServiceClient {

	/**
	 * Gets the available databases from the given url
	 *
	 * @param serviceurl the url to hit
	 * @return a (possibly empty) array of database objects
	 * @throws RestClientException if there is a network/password problem
	 */
	public DbInfo[] getDbs( String serviceurl ) throws RestClientException;

	/**
	 * Sets the username/password pair for authenticating against the given url
	 *
	 * @param url
	 * @param username
	 * @param pass
	 */
	public void setAuthentication( String url, String username, char[] pass );
}
