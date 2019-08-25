/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.util;

import com.ostrichemulators.semtool.model.vocabulary.SEMTOOL;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.query.util.MetadataQuery;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;

/**
 *
 * @author ryan
 */
public class StructureManagerFactory {

	private static final Logger log = Logger.getLogger( StructureManagerFactory.class );

	private StructureManagerFactory() {

	}

	public static StructureManager getStructureManager( IEngine engine ) {
		MetadataQuery mq = new MetadataQuery( SEMTOOL.ReificationModel );
		engine.queryNoEx( mq );
		IRI reif = IRI.class.cast( mq.getOne() );

		if ( SEMTOOL.Custom_Reification.equals( reif ) ) {
			throw new IllegalArgumentException( "Custom reification is not (yet) supported" );
		}

		return new SemtoolStructureManagerImpl( engine );
	}
}
