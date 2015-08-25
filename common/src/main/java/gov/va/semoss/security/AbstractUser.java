/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.security;

import java.security.Permission;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author ryan
 */
public abstract class AbstractUser implements User {

	private final Set<Permission> permissions = new HashSet<>();
	private String username = null;

	public AbstractUser() {
	}

	public AbstractUser( String username ) {
		this.username = username;
	}

	public AbstractUser( String username, Collection<Permission> pp ) {
		this.username = username;
		permissions.addAll( pp );
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public void setUsername( String username ) {
		this.username = username;
	}

	@Override
	public boolean hasPermission( Permission theirs ) {
		for ( Permission mine : permissions ) {
			if ( mine.implies( theirs ) ) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void resetPermissions( Collection<Permission> perms ) {
		permissions.clear();
		permissions.addAll( perms );
	}
}
