/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.web.controller;

import gov.va.semoss.web.io.DbInfo;

import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author ryan
 */
@Controller
public class HomeController extends SemossControllerBase {

	private static final Logger log = Logger.getLogger( HomeController.class );

	@RequestMapping( value = "/", method = RequestMethod.GET )
	public String getWelcome() {
		return "index.vm";
	}

	@RequestMapping( value = "/testDriver", method = RequestMethod.GET )
	public String getTestDriver() {
		return "testDriver.vm";
	}

	@RequestMapping( value = "/semoss/allDatabaseIDs", method = RequestMethod.GET )
	public @ResponseBody String[] getAllDatabaseIDs() {
		log.debug("Getting all database IDs.");
		DbInfo[] testDbs = getAllDBs();

		String[] testDbIDs = new String[testDbs.length];
		for ( int i = 0; i < testDbs.length; i++ ) {
			testDbIDs[i] = testDbs[i].getName();
		}

		return testDbIDs;
	}

	@RequestMapping( value = "/semoss/allDatabases", method = RequestMethod.GET )
	public @ResponseBody DbInfo[] getAllDatabases() {
		log.debug( "Getting all databases." );
		DbInfo[] testDbs = getAllDBs();

		return testDbs;
	}

	@RequestMapping( value = "/semoss/oneDatabase/{id}", method = RequestMethod.GET )
	public @ResponseBody DbInfo getOneDatabaseWithID( @PathVariable( "id" ) String id,
			HttpServletResponse response ) {
		log.debug( "Getting database with ID " + id + "." );
		DbInfo[] testDbs = getAllDBs();

		for ( DbInfo testDb : testDbs ) {
			if ( testDb.getName().equals( id ) ) {
				return testDb;
			}
		}

		response.setStatus( HttpServletResponse.SC_NOT_FOUND );
		return null;
	}

	/*
	 * Returns all current DB info we have access to. Currently it returns test data
	 * but should be updated to hit a configuration file or to hit a data store. JPM 08/18/2015
	 */
	private DbInfo[] getAllDBs() {
		return DbInfo.getTestDatabases();
	}
}
