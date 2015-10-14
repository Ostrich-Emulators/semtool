package gov.va.semoss.web.controller;

import gov.va.semoss.user.User;
import gov.va.semoss.user.User.UserProperty;
import gov.va.semoss.web.datastore.UserMapper;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;

import gov.va.semoss.web.security.DbAccess;

import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.openrdf.model.URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
	
	@RequestMapping("/" )
	@ResponseBody
	public User[] getAllUsers() {
		log.debug( "Getting all Users." );
		Collection<User> users = datastore.getAll();
		User[] userArray = new User[users.size()];
		users.toArray(userArray);
		return userArray;
	}

	@RequestMapping( value="/{id}")
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

	@RequestMapping( value="/{username}/accesses", method=RequestMethod.PUT)
	@ResponseBody
	public User updateUser( @PathVariable( "username" ) String username,
			@RequestParam Map<URI, DbAccess> accessMap, HttpServletResponse response ) {
		User user = datastore.getOne(username);
		if ( user == null ) {
			throw new NotFoundException();
		}

		datastore.setAccesses(user, accessMap);
		log.debug( "Updating user " + username + " (user: "
				+ user.getProperty( UserProperty.USER_FULLNAME ) + ")" );
		if ( user == null ) {
			throw new NotFoundException();
		}
		return user;
	}

	

}