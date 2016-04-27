package com.ostrichemulators.semtool.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

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
	public String adminPage( Model model, HttpServletRequest req ) {
		// Note - these calls are used in case you need to produce a UI through 
		// Velocity somewhere in the codebase, but do not have access to 
		CsrfToken csrfToken = (CsrfToken) req.getAttribute(CsrfToken.class.getName());
		if (csrfToken != null) {
			model.addAttribute("_csrf", csrfToken);
	    }
		model.addAttribute( "message", "Admin Page" );
		return "admin";
	}

	@RequestMapping( value = "/user**", method = RequestMethod.GET )
	public String userPage( Model model ) {
		model.addAttribute( "message", "User Page" );
		return "user.vm";
	}
}
