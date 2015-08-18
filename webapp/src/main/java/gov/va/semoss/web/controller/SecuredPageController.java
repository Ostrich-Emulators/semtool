package gov.va.semoss.web.controller;

import gov.va.semoss.web.datastore.DbInfoMapper;
import javax.servlet.http.HttpServletResponse;

import gov.va.semoss.web.io.DbInfo;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * This controller serves pages secured by Spring Security
 * @author Wayne Warren
 *
 */
@Controller
public class SecuredPageController  extends SemossControllerBase  {
	private static final Logger log = Logger.getLogger( SecuredPageController.class );

	@Autowired
	private DbInfoMapper datastore;
	
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
	
	@RequestMapping( value = "/semoss/allDatabaseIDs", method = RequestMethod.GET )
	public @ResponseBody String[] getAllDatabaseIDs() {
		log.debug("Getting all database IDs.");
		DbInfo[] testDbs = DbInfo.getAllDBs();

		String[] testDbIDs = new String[testDbs.length];
		for ( int i = 0; i < testDbs.length; i++ ) {
			testDbIDs[i] = testDbs[i].getName();
		}

		return testDbIDs;
	}

	@RequestMapping( value = "/semoss/oneDatabase/{id}", method = RequestMethod.GET )
	public @ResponseBody DbInfo getOneDatabaseWithID( @PathVariable( "id" ) String id,
			HttpServletResponse response ) {
		log.debug( "Getting database with ID " + id + "." );
		DbInfo[] testDbs = DbInfo.getAllDBs();
		for ( DbInfo testDb : testDbs ) {
			if ( testDb.getName().equals( id ) ) {
				return testDb;
			}
		}
		response.setStatus( HttpServletResponse.SC_NOT_FOUND );
		return null;
	}

	@RequestMapping( value = "/semoss/allDatabases", method = RequestMethod.GET )
	public @ResponseBody DbInfo[] getAllDatabases() {
		log.debug("Getting all databases.");
		DbInfo[] testDbs = DbInfo.getAllDBs();
		
		return testDbs;
	}
}
