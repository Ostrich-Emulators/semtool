/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.api;

import com.ostrichemulators.semtool.util.Constants;
import org.eclipse.rdf4j.model.IRI;

/**
 *
 * @author ryan
 */
public enum ReificationStyle {

	LEGACY( Constants.NONODE ),
	SEMTOOL( com.ostrichemulators.semtool.model.vocabulary.SEMTOOL.SEMTOOL_Reification ),
	W3C( com.ostrichemulators.semtool.model.vocabulary.SEMTOOL.W3C_Reification ),
	RDR( com.ostrichemulators.semtool.model.vocabulary.SEMTOOL.RDR_Reification );
	public final IRI uri;

	ReificationStyle( IRI u ) {
		uri = u;
	}

	public static ReificationStyle fromUri( IRI u ) {
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
