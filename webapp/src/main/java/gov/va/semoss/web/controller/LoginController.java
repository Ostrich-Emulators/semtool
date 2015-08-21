package gov.va.semoss.web.controller;

import gov.va.semoss.security.User;
import gov.va.semoss.security.UserImpl;
import org.apache.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * This controller serves pages secured by Spring Security
 *
 * @author Wayne Warren
 *
 */
@Controller
public class LoginController extends SemossControllerBase {

	private static final Logger log = Logger.getLogger( LoginController.class );

	@RequestMapping( value = "/login2", method = RequestMethod.GET )
	@ResponseBody
	public gov.va.semoss.security.User login( @AuthenticationPrincipal UserImpl user ) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		org.springframework.security.core.userdetails.User ldap
				= org.springframework.security.core.userdetails.User.class.cast(
						authentication == null ? null : authentication.getPrincipal() );

		user.setUsername( ldap.getUsername() );
		for ( GrantedAuthority ga : ldap.getAuthorities() ) {
			log.debug( ga );
		}

		return user;
	}

	@RequestMapping( "/logout" )
	@ResponseBody
	public User logout() {
		return null;
	}
}
