/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.web.security;

import com.ostrichemulators.semtool.web.datastore.UserMapper;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;

/**
 *
 * @author ryan
 */
public class LoginListener implements ApplicationListener<InteractiveAuthenticationSuccessEvent> {

	private static final Logger log = Logger.getLogger( LoginListener.class );

	@Autowired
	private UserMapper usermapper;

	@Override
	public void onApplicationEvent( InteractiveAuthenticationSuccessEvent event ) {
		SemossUser user
				= SemossUser.class.cast( event.getAuthentication().getPrincipal() );
		String username = user.getUsername();

		if ( !usermapper.exists( username ) ) {
			try {
				usermapper.create( user );
			}
			catch ( Exception e ) {
				log.error( e, e );
			}
		}
	}
}
