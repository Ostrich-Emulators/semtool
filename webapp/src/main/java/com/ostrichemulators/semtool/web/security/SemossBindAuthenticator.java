package com.ostrichemulators.semtool.web.security;

import org.apache.log4j.Logger;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.authentication.BindAuthenticator;

/**
 * A custom authentication manager that allows access if the user details exist
 * in the database and if the username and password are not the same. Otherwise,
 * throw a {@link BadCredentialsException}
 */
public class SemossBindAuthenticator extends BindAuthenticator {

	protected static Logger logger = Logger.getLogger( SemossBindAuthenticator.class );

	public SemossBindAuthenticator( BaseLdapPathContextSource contextSource ) {
		super( contextSource );
	}

	@Override
	public DirContextOperations authenticate( Authentication authentication ) {
		DirContextOperations ops = super.authenticate( authentication );
		return ops;
	}
}
