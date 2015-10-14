package gov.va.semoss.web.controller;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * This controller serves pages secured by Spring Security
 *
 * @author Wayne Warren
 *
 */
@Controller
public class SecuredPageController extends SemossControllerBase {

	private static final Logger log = Logger.getLogger( SecuredPageController.class );

	@RequestMapping( value = { "/admin**" }, method = RequestMethod.GET )
	public String adminPage( Model model ) {
		// Note - these calls are used in case you need to produce a UI through 
		// Velocity somewhere in the codebase, but do not have access to 

		model.addAttribute( "message", "Admin Page" );
		return "admin";
	}

	@RequestMapping( value = "/user**", method = RequestMethod.GET )
	public String userPage( Model model ) {
		model.addAttribute( "message", "User Page" );
		return "user.vm";
	}
}
