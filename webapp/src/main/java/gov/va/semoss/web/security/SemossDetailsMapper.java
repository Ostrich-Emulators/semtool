package gov.va.semoss.web.security;

import gov.va.semoss.user.User.UserProperty;
import gov.va.semoss.web.security.SemossUser.SemossEssence;
import java.util.Collection;
import org.apache.log4j.Logger;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;

/**
 * A custom authentication manager that allows access if the user details exist
 * in the database and if the username and password are not the same. Otherwise,
 * throw a {@link BadCredentialsException}
 */
public class SemossDetailsMapper extends LdapUserDetailsMapper {

	protected static Logger logger = Logger.getLogger( SemossDetailsMapper.class );

	@Override
	public UserDetails mapUserFromContext( DirContextOperations ctx, String username,
			Collection<? extends GrantedAuthority> authorities ) {
		LdapUserDetails ldap
				= LdapUserDetails.class.cast( super.mapUserFromContext( ctx, username,
								authorities ) );

		SemossEssence essence = new SemossUser.SemossEssence( ldap );
		SemossUser user = essence.createUserDetails();

		user.setProperty( UserProperty.USER_EMAIL, ctx.getStringAttribute( "mail" ) );
		user.setProperty( UserProperty.USER_FULLNAME, ctx.getStringAttribute( "cn" ) );

		return user;
	}
}
