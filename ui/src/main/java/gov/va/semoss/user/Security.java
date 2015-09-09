/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.user;

import gov.va.semoss.rdf.engine.api.IEngine;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ryan
 */
public class Security {

	private static Security instance;
	private final Map<IEngine, User> usermap = new HashMap<>();

	public static Security getSecurity() {
		if ( null == instance ) {
			instance = new Security();
		}
		return instance;
	}

	public void associateUser( IEngine eng, User u ) {
		usermap.put( eng, u );
	}

	public User getAssociatedUser( IEngine e ) {
		return ( usermap.containsKey( e ) ? usermap.get( e ) : new LocalUserImpl() );
	}
}
