/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.user;

/**
 *
 * @author ryan
 */
public abstract class AbstractUser implements User {

	private String username = null;

	public AbstractUser() {
	}

	public AbstractUser( String username ) {
		this.username = username;
	}

	@Override
	public String getUsername() {
		return username;
	}	
}
