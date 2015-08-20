package gov.va.semoss.web.controller;

import gov.va.semoss.web.datastore.DbInfoMapper;
import javax.servlet.http.HttpServletResponse;

import gov.va.semoss.web.io.DbInfo;

import java.util.Collection;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * This controller serves pages secured by Spring Security
 *
 * @author Wayne Warren
 *
 */
@Controller
@RequestMapping( "/databases" )
public class DatabaseController extends SemossControllerBase {

	private static final Logger log = Logger.getLogger( DatabaseController.class );

	@Autowired
	private DbInfoMapper datastore;

	@RequestMapping( params = "names" )
	@ResponseBody
	public String[] getAllDatabaseIDs() {
		log.debug( "Getting all database IDs." );
		Collection<DbInfo> testDbs = datastore.getAll();

		int i = 0;
		String[] testDbIDs = new String[testDbs.size()];
		for ( DbInfo dbi : testDbs ) {
			testDbIDs[i++] = dbi.getName();
		}

		return testDbIDs;
	}

	@RequestMapping( "/{id}" )
	@ResponseBody
	public DbInfo getOneDatabaseWithID( @PathVariable( "id" ) String id,
			HttpServletResponse response ) {
		log.debug( "Getting database with ID " + id + "." );
		DbInfo test = datastore.getOne( id );
		if ( null == test ) {
			throw new UnauthorizedException();
		}
		return test;
	}

	@RequestMapping
	@ResponseBody
	public DbInfo[] getAllDatabases() {
		log.debug( "Getting all databases." );
		DbInfo[] testDbs = datastore.getAll().toArray( new DbInfo[0] );
		return testDbs;
	}
}
