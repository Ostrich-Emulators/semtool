package gov.va.semoss.web.controller;

import org.apache.log4j.Logger;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * This controller serves pages secured by Spring Security
 *
 * @author Wayne Warren
 *
 */
@Controller
public class LoginController extends SemossControllerBase {

	private static final Logger log = Logger.getLogger( LoginController.class );
	
	@RequestMapping( value = "/login", method = RequestMethod.GET )
	@ResponseBody
	public ModelAndView basicLogin(HttpServletRequest request, @RequestParam(value = "error", required = false) String error,
		    @RequestParam(value = "logout", required = false) String logout ) {
		ModelAndView model = new ModelAndView();
		CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
	    if (csrfToken != null) {
	        model.addObject("_csrf",csrfToken);
	    }
	    if (error != null) {
	        model.addObject("error", "Invalid username and password!");
	      }

	      if (logout != null) {
	        model.addObject("msg", "You've been logged out successfully.");
	      }
		model.setViewName("/pages/login");
		return model;
	}
	
	@RequestMapping( value = "/login", method = RequestMethod.POST )
	@ResponseBody
	public ModelAndView basicAuth(HttpServletRequest request ) {
		ModelAndView model = new ModelAndView();
		CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
	    if (csrfToken != null) {
	        model.addObject("_csrf",csrfToken);
	    }
		model.setViewName("/pages/login");
		return model;
	}

	
}
