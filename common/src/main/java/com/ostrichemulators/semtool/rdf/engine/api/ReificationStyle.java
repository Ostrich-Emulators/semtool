/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.api;

import com.ostrichemulators.semtool.model.vocabulary.VAS;
import com.ostrichemulators.semtool.util.Constants;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public enum ReificationStyle {

	LEGACY( Constants.NONODE ),
	SEMOSS( VAS.VASEMOSS_Reification ),
	W3C( VAS.W3C_Reification ),
	RDR( VAS.RDR_Reification );
	public final URI uri;

	ReificationStyle( URI u ) {
		uri = u;
	}

	public static ReificationStyle fromUri( URI u ) {
		if ( null == u ) {
			return LEGACY;
		}

		for ( ReificationStyle r : values() ) {
			if ( r.uri.equals( u ) ) {
				return r;
			}
		}
		throw new IllegalArgumentException( "Unknown reification URI: " + u );
	}
}
