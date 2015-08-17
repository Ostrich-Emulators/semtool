/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.web.controller;

import org.apache.log4j.Logger;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import java.net.URI;
import java.net.URISyntaxException;

import gov.va.semoss.web.io.DbInfo;

/**
 *
 * @author ryan
 */
@Controller
public class HomeController extends SemossControllerBase {

	private static final Logger log = Logger.getLogger( HomeController.class );
	private static ArrayList<Map<String, Object>> knowledgeBases;

	@RequestMapping( value = "/", method = RequestMethod.GET )
	public String getWelcome() {
		return "index.vm";
	}

	@RequestMapping( value = "/testDriver", method = RequestMethod.GET )
	public String getTestDriver() {
		return "testDriver.vm";
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
