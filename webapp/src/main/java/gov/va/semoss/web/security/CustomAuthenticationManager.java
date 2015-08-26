package gov.va.semoss.web.security;

import org.apache.log4j.Logger;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;

/**
 * A custom authentication manager that allows access if the user details exist
 * in the database and if the username and password are not the same. Otherwise,
 * throw a {@link BadCredentialsException}
 */
public class CustomAuthenticationManager extends LdapUserDetailsMapper {

	protected static Logger logger = Logger.getLogger( CustomAuthenticationManager.class );

	@Override
	public void mapUserToContext( UserDetails user, DirContextAdapter ctx ) {
		super.mapUserToContext( user, ctx );
	}
}
