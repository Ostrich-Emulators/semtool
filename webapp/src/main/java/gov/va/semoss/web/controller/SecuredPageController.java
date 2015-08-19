package gov.va.semoss.web.controller;


import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * This controller serves pages secured by Spring Security
 * @author Wayne Warren
 *
 */
@Controller
public class SecuredPageController  extends SemossControllerBase  {
	private static final Logger log = Logger.getLogger( SecuredPageController.class );

	
	@RequestMapping(value = { "/admin**" }, method = RequestMethod.GET)
	public ModelAndView adminPage() {
		// Note - these calls are used in case you need to produce a UI through 
		// Velocity somewhere in the codebase, but do not have access to 
		ModelAndView model = new ModelAndView();
		model.addObject("message", "Admin Page");
		model.setViewName("admin");
		return model;
	}
 
	@RequestMapping(value = "/data**", method = RequestMethod.GET)
	public ModelAndView dataPage() {
		// Create the model and view 
		ModelAndView model = new ModelAndView();
		model.addObject("message", "Data Page");
		model.setViewName("data");
 		return model;
 
	}
 
	@RequestMapping(value = "/insight**", method = RequestMethod.GET)
	public ModelAndView insightPage() {
		ModelAndView model = new ModelAndView();
		model.addObject("message", "Insight Page");
		model.setViewName("insight");
		return model;
	}
	
	@RequestMapping(value = "/user**", method = RequestMethod.GET)
	public ModelAndView userPage() {
		ModelAndView model = new ModelAndView();
		model.addObject("message", "User Page");
		model.setViewName("user.vm");
		return model;
	}	
}
