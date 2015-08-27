package gov.va.semoss.web.security;

import java.util.Collection;
import java.util.Set;
import org.apache.log4j.Logger;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;

/**
 * A custom authentication manager that allows access if the user details exist
 * in the database and if the username and password are not the same. Otherwise,
 * throw a {@link BadCredentialsException}
 */
public class SemossAuthoritiesPopulator extends DefaultLdapAuthoritiesPopulator {

	protected static Logger logger = Logger.getLogger( SemossAuthoritiesPopulator.class );

	public SemossAuthoritiesPopulator( ContextSource contextSource, String groupSearchBase ) {
		super( contextSource, groupSearchBase );
	}

	@Override
	protected Set<GrantedAuthority> getAdditionalRoles( DirContextOperations user,
			String username ) {
		// we can look elsewhere to populate groups if we want
		// our superclass handles the LDAP groups
		return super.getAdditionalRoles( user, username );
	}
}
