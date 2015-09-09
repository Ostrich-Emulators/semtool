package gov.va.semoss.web.controller;

import gov.va.semoss.user.RemoteUserImpl;
import gov.va.semoss.user.User;
import gov.va.semoss.web.security.SemossUser;
import org.apache.log4j.Logger;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Spring security actually handles the authentication and authorization...this
 * class just lets us use our own login form, which we can style nicely.
 *
 * @author Wayne Warren
 *
 */
@Controller
public class LoginController extends SemossControllerBase {

	private static final Logger log = Logger.getLogger( LoginController.class );

	@RequestMapping( value = "/login", method = RequestMethod.GET )
	public String login( HttpServletRequest request, Model model ) {
		tokenify( model, request );
		return "/pages/login";
	}

	@RequestMapping( value = "/login", method = RequestMethod.GET, params = "error" )
	public String login( HttpServletRequest request, Model model,
			@RequestParam String error ) {
		tokenify( model, request );
		model.addAttribute( "error", true );
		return "/pages/login";
	}

	@RequestMapping( value = "/login", method = RequestMethod.POST )
	public String doLogin( HttpServletRequest request, Model model ) {
		tokenify( model, request );
		return "/pages/login";
	}

	@RequestMapping( value = "/login", method = RequestMethod.GET, params = "logout" )
	public String loginout( HttpServletRequest request, Model model ) {
		return logout( request, model );
	}

	@RequestMapping( "/logout" )
	public String logout( HttpServletRequest request, Model model ) {
		tokenify( model, request );
		SecurityContextHolder.getContext().setAuthentication( null );
		model.addAttribute( "msg", "You've been logged out successfully." );
		return "/pages/login";
	}

	private static void tokenify( Model model, HttpServletRequest request ) {
		CsrfToken csrfToken = CsrfToken.class.cast( request.getAttribute( "_csrf" ) );
		if ( csrfToken != null ) {
			model.addAttribute( "_csrf", csrfToken );
		}
	}

	@RequestMapping( value = "/login", params = "whoami" )
	@ResponseBody
	public User getUser( HttpServletRequest req ) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if ( hasRole( auth, "ROLE_ANONYMOUS" ) ) {
			return null;
		}
		else {
			Object principal = auth.getPrincipal();
			SemossUser ldap = SemossUser.class.cast( principal );
			RemoteUserImpl user = new RemoteUserImpl( ldap );
			return user;
		}
	}
}
