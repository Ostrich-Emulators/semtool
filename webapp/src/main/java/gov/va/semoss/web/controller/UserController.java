package gov.va.semoss.web.controller;

import gov.va.semoss.user.User;
import gov.va.semoss.user.User.UserProperty;
import gov.va.semoss.web.datastore.UserMapper;

import gov.va.semoss.web.security.DbAccess;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;

import java.util.Collection;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * This controller handles API requests related to user management
 *
 * @author Wayne Warren
 *
 */
@Controller
@RequestMapping( "/users" )
public class UserController extends SemossControllerBase {

	private static final Logger log = Logger.getLogger( UserController.class );
	
	private static final ObjectMapper mapper = new ObjectMapper();
	
	@Autowired
	ServletContext servletContext;

	@Autowired
	private UserMapper datastore;
	
	@RequestMapping( "/" )
	@ResponseBody
	public User[] getAllUsers() {
		log.debug( "Getting all Users." );
		Collection<User> users = datastore.getAll();
		User[] userArray = new User[users.size()];
		users.toArray(userArray);
		return userArray;
	}

	@RequestMapping( value = "/{id}" )
	@ResponseBody
	public User getOneUserWithUsername( @PathVariable( "id" ) String id,
			HttpServletResponse response ) {
		User user = datastore.getOne( id );
		if ( user == null ) {
			throw new NotFoundException();
		}
		log.debug( "Retrieving user " + id + " (user: "
				+ user.getProperty( UserProperty.USER_FULLNAME ) + ")" );
		return user;
	}

	@RequestMapping( value = "/{username}/accesses", method = RequestMethod.PUT )
	@ResponseBody
	public User updateUser( @PathVariable( "username" ) String username,
			@RequestBody String accessMap, HttpServletResponse response ) throws JSONException {
		User user = datastore.getOne( username );

		if ( user == null ) {
			throw new NotFoundException();
		}

		// ugh: why doens't Jackson automatically handle this for us?
		JSONObject json = new JSONObject( accessMap );
		Map<URI, DbAccess> accesses = new HashMap<>();
		Iterator<String> keyit = json.keys();
		while( keyit.hasNext() ){
			String key = keyit.next();
			accesses.put( new URIImpl( key ),
					DbAccess.valueOf( json.getString( key ) ) );
		}

		datastore.setAccesses( user, accesses );
		log.debug( "Updating accesses for user " + username + " (user: "
				+ user.getProperty( UserProperty.USER_FULLNAME ) + ")" );
		if ( user == null ) {
			throw new NotFoundException();
		}
		return user;
	}
}
