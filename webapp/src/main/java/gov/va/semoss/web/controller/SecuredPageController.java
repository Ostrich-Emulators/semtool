package gov.va.semoss.web.controller;

import java.util.HashMap;

import gov.va.semoss.web.ui.SEMOSSUIFactory;

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

	@RequestMapping(value = { "/admin**" }, method = RequestMethod.GET)
	public ModelAndView adminPage() {
		HashMap<String, Object> valueMap = new HashMap<String, Object>();
		// Note - this call is used in case you need to produce a UI through 
		// Velocity somewhere in the codebase, but do not have access to 
		// the ModelAndView managed by Spring
		//String ui = SEMOSSUIFactory.instance().getUI("admin.vm", valueMap);
		// Now for the Springified UI production version
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
