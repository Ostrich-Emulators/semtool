/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.user;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ryan
 */
public abstract class AbstractUser implements User {

	private String username = null;
	private final Map<String, String> props = new HashMap<>();

	public AbstractUser() {
	}

	public AbstractUser( String username ) {
		this.username = username;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public String getProperty( String prop ) {
		return props.get( prop );
	}

	@Override
	public void setProperty( String prop, String value ) {
		props.put( prop, value );
	}
}
