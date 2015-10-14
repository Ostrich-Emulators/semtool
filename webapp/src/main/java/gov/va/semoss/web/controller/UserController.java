package gov.va.semoss.web.controller;

import gov.va.semoss.user.User;
import gov.va.semoss.user.User.UserProperty;
import gov.va.semoss.web.datastore.UserMapper;
import gov.va.semoss.web.io.DbInfo;
import gov.va.semoss.web.security.DbAccess;
import gov.va.semoss.web.security.SemossUser;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;

import java.io.File;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
	
	@Autowired
	ServletContext servletContext;

	@Autowired
	private UserMapper datastore;
	
	@RequestMapping(  value="/", method = RequestMethod.GET )
	@ResponseBody
	public User[] getAllUsers() {
		log.debug( "Getting all Users." );
		Collection<User> users = datastore.getAll();
		User[] userArray = new User[users.size()];
		users.toArray(userArray);
		return userArray;
	}

	@RequestMapping( value = "/{id}", method = RequestMethod.GET  )
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
	
	@RequestMapping( value = "/{username}", method = RequestMethod.DELETE  )
	@ResponseBody
	public boolean deleteUser( @PathVariable( "username" ) String username,
			HttpServletResponse response ) {
		User user = datastore.getOne( username );
		if ( user == null ) {
			log.error("Unable to delete user: " + username + ", user not found.");
			return false;
		}
		log.debug( "Deleting user " + username + " (user: "
				+ user.getProperty( UserProperty.USER_FULLNAME ) + ")" );
		try {
			//datastore.remove(user);
		} catch (Exception e) {
			log.error("Unable to delete user: " + username + ", exception occurred.", e);
			return false;
		}
		return true;
	}

	@RequestMapping( value = "/{username}/accesses", method = RequestMethod.PUT )
	@ResponseBody
	public User updateUserAccess( @PathVariable( "username" ) String username,
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
		return user;
	}
	
	@RequestMapping( value = "/", method = RequestMethod.POST )
	@ResponseBody
	public boolean createUser(  @RequestBody String encodedJSON,
			@RequestBody String accessMap, HttpServletResponse response ) throws JSONException {
		try {
			User user = (User) WebCodec.instance().parse(encodedJSON);
			datastore.create(user);
			log.debug( "Created User: " + user.getUsername() + " (user: "
					+ user.getProperty( UserProperty.USER_FULLNAME ) + ")" );
			return true;
		}
		catch (Exception e){
			log.error("Error creating User.", e);
			return false;
		}
	}
	
	@RequestMapping( value="/", method = RequestMethod.PUT)
	@ResponseBody
	public boolean updateUser( @RequestBody String encodedJSON,
			HttpServletResponse response ) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		SemossUser user = SemossUser.class.cast( auth.getPrincipal() );
		try {
			User submittedUser = (User)WebCodec.instance().parse(encodedJSON);
			datastore.update(submittedUser);
			log.debug( "Updated database: " + user.getUsername() + " (user: "
					+ user.getProperty( UserProperty.USER_FULLNAME ) + ")" );
			return true;
		}
		catch (Exception e){
			log.error("Error creating database.", e);
			return false;
		}
	}
}
