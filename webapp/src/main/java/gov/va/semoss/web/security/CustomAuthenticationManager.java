package gov.va.semoss.web.security;


import gov.va.semoss.web.init.SpringContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * A custom authentication manager that allows access if the user details exist
 * in the database and if the username and password are not the same. Otherwise,
 * throw a {@link BadCredentialsException}
 */
public class CustomAuthenticationManager implements AuthenticationProvider {

	protected static Logger logger = Logger.getLogger("service");

	private static final String[] attributeIDs = { "role" };

	public static final String USER_SESSION_ATTRIBUTE = "USER";


	public Authentication authenticate(Authentication auth)
			throws AuthenticationException {
		LDAPUser user = null;
		// Retrieve user details from LDAP
		String saltedPassword = ADSLdapManager.instance().saltPassword((String)auth.getCredentials());
		String hashedSaltedPassword = ADSLdapManager.instance().hashPassword(saltedPassword);
		try {
			user = ADSLdapManager.instance().getUser(auth.getName(),
				hashedSaltedPassword, attributeIDs);
		}
		catch (Exception e){
			throw new BadCredentialsException("LDAP server connection issue");
		}
		// If the user came back null, then "That dog just don't hunt :)"
		if (user == null) {
			throw new BadCredentialsException("Unknown user name, or bad password");
		}
		// Otherwise, we've got what we need
		else {
			// Let the rest of our Nebraska-sized web framework that we have authenticated a user, and 
			// assign the role, and stick the user object in the SecurityContext, so that it can be 
		    // retrieved, as in session, like so (LDAPUser)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user,
				auth.getCredentials(), user.getAuthorities());
			return token;
		}
	}


	@Override
	public boolean supports(Class<? extends Object> authentication) {
		if (authentication.getName().equals("org.springframework.security.authentication.UsernamePasswordAuthenticationToken")){
			return true;
		}
		else {
			return false;
		}
		// return
		// (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
	}


}
