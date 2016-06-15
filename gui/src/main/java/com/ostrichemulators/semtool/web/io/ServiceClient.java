/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.web.io;

import com.ostrichemulators.semtool.user.User;
import org.springframework.web.client.RestClientException;

/**
 *
 * @author ryan
 */
public interface ServiceClient {

	/**
	 * Gets the available databases from the given url
	 *
	 * @return a (possibly empty) array of database objects
	 * @throws RestClientException if there is a network/password problem
	 */
	public DbInfo[] getDbs() throws RestClientException;

	/**
	 * Gets details about the currently-logged-in user from the given url
	 *
	 * @return
	 * @throws RestClientException
	 */
	public User getUser() throws RestClientException;

	/**
	 * Sets the username/password pair for authenticating against the given
	 * service
	 *
	 * @param username
	 * @param pass
	 */
	public void setAuthentication( String username, char[] pass );
}
