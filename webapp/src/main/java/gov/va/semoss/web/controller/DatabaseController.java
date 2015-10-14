package gov.va.semoss.web.controller;

import gov.va.semoss.user.User.UserProperty;
import gov.va.semoss.web.datastore.DbInfoMapper;
import gov.va.semoss.web.filters.RemoteDBReverseProxyFilter;

import javax.servlet.http.HttpServletResponse;

import gov.va.semoss.web.io.DbInfo;
import gov.va.semoss.web.security.SemossUser;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

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
	
	@Autowired
	ServletContext servletContext;

	@RequestMapping("/list" )
	@ResponseBody
	public DbInfo[] getAllDatabases() {
		log.debug( "Getting all database IDs." );
		Collection<DbInfo> testDbs = datastore.getAll();
		int i = 0;
		DbInfo[] testDbIDs = new DbInfo[testDbs.size()];
		for ( DbInfo dbi : testDbs ) {
			testDbIDs[i++] = dbi;
		}
		return testDbIDs;
	}

	@RequestMapping( value="/{name}")
	@ResponseBody
	public DbInfo getOneDatabaseWithID( @PathVariable( "name" ) String name,
			HttpServletResponse response ) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		SemossUser user = SemossUser.class.cast( auth.getPrincipal() );
		log.debug( "Getting database " + name + " (user: "
				+ user.getProperty( UserProperty.USER_FULLNAME ) + ")" );

		DbInfo dbi = datastore.getOne( name );
		if ( null == dbi ) {
			throw new UnauthorizedException();
		}
		return dbi;
	}

	/**
	 * Used for obtaining a set of "raw" DbInfo records, for use with 
	 * the admin web console, thus displaying the urls of the databases
	 * BEHIND the reverse proxy.
	 * @param req The incoming request
	 * @return An array of DbInfo objects
	 */
	@RequestMapping( value = "/raw", method = RequestMethod.GET )
	@ResponseBody
	public DbInfo[] getAllRawDatabases( HttpServletRequest req ) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		SemossUser user = SemossUser.class.cast( auth.getPrincipal() );
		log.debug( "Getting all databases (user: "
				+ user.getProperty( UserProperty.USER_FULLNAME ) + ")" );
		DbInfo[] testDbs = datastore.getAll().toArray( new DbInfo[0] );
		return testDbs;
	}
	
	/**
	 * Used to obtain RP-sanitized DBInfo items
	 * @param req
	 * @return
	 */
	@RequestMapping( value = "/", method = RequestMethod.GET )
	@ResponseBody
	public DbInfo[] getAllDatabases( HttpServletRequest req ) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		SemossUser user = SemossUser.class.cast( auth.getPrincipal() );
		log.debug( "Getting all reverse-proxied databases (user: "
				+ user.getProperty( UserProperty.USER_FULLNAME ) + ")" );

		DbInfo[] testDbs = datastore.getAll().toArray( new DbInfo[0] );

		for ( DbInfo dbi : testDbs ) {
			RemoteDBReverseProxyFilter.convertToRPStyle(dbi, req );
		}

		return testDbs;
	}
	
	
	@RequestMapping(value = "/import", method = RequestMethod.POST)
	@ResponseBody
	public String importDatabaseViaFile(HttpServletRequest req,
			@RequestParam(value = "jnlFile", required = true) MultipartFile jnlFile) {
		if (!jnlFile.isEmpty()) {
			try {
				validate(jnlFile);
				saveJNLFile(jnlFile.getName(), jnlFile);
				// TODO Save the file and do something with it
				
			} catch (RuntimeException | IOException re) {
				// TODO Log this
				return "Failed to save file";
			}
		}
		return "Success";
	}
	
	private boolean validate(MultipartFile file){
		return true;
	}
	
	private void saveJNLFile(String filename, MultipartFile jnlFile)
			throws RuntimeException, IOException {
				try {
					File file = new File(servletContext.getRealPath("/") + "/"
							+ filename);					 
					FileUtils.writeByteArrayToFile(file, jnlFile.getBytes());
				} 
				catch (IOException e) {
					throw e;
				}
			}


}

//	@RequestMapping( "/{id}/{type}" )
//	public void getRepo( @PathVariable( "id" ) String id,
//			@PathVariable( "type" ) String type, HttpServletRequest request,
//			HttpServletResponse response ) throws ServletException, IOException {
//		
//	}
//
//	@RequestMapping( "/{id}/{type}/statements" )
//	public void getStatements( @PathVariable( "id" ) String id,
//			@PathVariable( "type" ) String type, HttpServletRequest request,
//			HttpServletResponse response ) throws ServletException, IOException {
//		
//	}
//
//	@RequestMapping( "/{id}/{type}/contexts" )
//	public void getContexts( @PathVariable( "id" ) String id,
//			@PathVariable( "type" ) String type, HttpServletRequest request,
//			HttpServletResponse response ) throws IOException {
//		
//	}
//
//	@RequestMapping( "/{id}/{type}/size" )
//	public void getSize( @PathVariable( "id" ) String id,
//			@PathVariable( "type" ) String type, HttpServletRequest request,
//			HttpServletResponse response ) throws IOException {
//		
//	}
//
//	@RequestMapping( "/{id}/{type}/rdf-graphs" )
//	public void getGraphs( @PathVariable( "id" ) String id,
//			@PathVariable( "type" ) String type, HttpServletRequest request,
//			HttpServletResponse response ) throws IOException {
//		
//	}
//
//	@RequestMapping( "/{id}/{type}/rdf-graphs/service" )
//	public void getGraphsService( @PathVariable( "id" ) String id,
//			@PathVariable( "type" ) String type, HttpServletRequest request,
//			HttpServletResponse response ) throws IOException {
//		
//	}
//
//	@RequestMapping( "/{id}/{type}/rdf-graphs/{name}" )
//	public void getGraph( @PathVariable( "id" ) String id,
//			@PathVariable( "type" ) String type, @PathVariable String name,
//			HttpServletRequest request, HttpServletResponse response ) throws IOException {
//		
//	}
//
//	@RequestMapping( "/{id}/{type}/namespaces" )
//	public void getNamespaces( @PathVariable( "id" ) String id,
//			@PathVariable( "type" ) String type, HttpServletRequest request,
//			HttpServletResponse response ) throws IOException {
//		
//	}
//
//	@RequestMapping( "/{id}/{type}/namespaces/{prefix}" )
//	public void getNamespace( @PathVariable( "id" ) String id,
//			@PathVariable( "type" ) String type, @PathVariable String prefix,
//			HttpServletRequest request, HttpServletResponse response ) throws IOException {
//		
//	}

