/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.web.controller;

import gov.va.semoss.web.io.DbInfo;

import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

/**
 *
 * @author ryan
 */
@Controller
public class HomeController extends SemossControllerBase {

	private static final Logger log = Logger.getLogger( HomeController.class );

	@RequestMapping( value = "/", method = RequestMethod.GET )
	public String getWelcome() {
		return "index";
	}

	@RequestMapping( value = "/testDriver", method = RequestMethod.GET )
	public String getTestDriver() {
		return "testDriver";
	}
	
	@RequestMapping( value = "/semoss/allDatabaseIDs", method = RequestMethod.GET )
	public @ResponseBody String getAllDatabaseIDs() {
		ArrayList<Map<String, Object>> knowledgeBases = DbInfo.getTestDatabases();
		
		ArrayList<String> theDatabaseIDs = new ArrayList<String>();
		for (Map<String, Object> knowledgeBase: knowledgeBases) {
			theDatabaseIDs.add( knowledgeBase.get("name")+"" );
		}
		
		String json = new Gson().toJson( theDatabaseIDs );
		return json;
	}
	
	@RequestMapping( value = "/semoss/allDatabases", method = RequestMethod.GET )
	public @ResponseBody String getAllDatabases() {
		log.debug("Getting all databases..");
		
		ArrayList<Map<String, Object>> knowledgeBases = DbInfo.getTestDatabases();
		
		String json = new Gson().toJson( knowledgeBases );
		return json;
	}
	
	@RequestMapping( value = "/semoss/oneDatabase/{id}", method = RequestMethod.GET )
	public @ResponseBody String getOneDatabaseWithID(@PathVariable("id") String id) {
		ArrayList<Map<String, Object>> knowledgeBases = DbInfo.getTestDatabases();

		for (Map<String, Object> knowledgeBase: knowledgeBases) {
			if ( knowledgeBase.get("name").equals(id) ) {
				return new Gson().toJson( knowledgeBase );
			}
		}
		
		return new Gson().toJson( DbInfo.getEmptyDatabase().getAsMap() );
	}

}
