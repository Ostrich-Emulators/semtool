/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.util;

import gov.va.semoss.model.vocabulary.VAS;
import java.util.Map;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.api.ReificationStyle;
import gov.va.semoss.rdf.query.util.MetadataQuery;
import gov.va.semoss.rdf.query.util.ModificationExecutorAdapter;
import gov.va.semoss.util.Constants;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;

/**
 * A class to centralize Engine operations. This class is thread-safe, and if
 * started as a thread and used to mount repositories, those repositories can be
 * successfully unmounted later.
 *
 * @author ryan
 */
public class EngineUtil2 {

	private static final Logger log = Logger.getLogger( EngineUtil2.class );

	private EngineUtil2() {
	}

	public static void clear( IEngine engine ) throws RepositoryException {
		try {
			final Map<URI, Value> metas = engine.query( new MetadataQuery() );
			metas.remove( VAS.Database );

			engine.execute( new ModificationExecutorAdapter( true ) {

				@Override
				public void exec( RepositoryConnection conn ) throws RepositoryException {
					conn.remove( (Resource) null, null, null );
					ValueFactory vf = conn.getValueFactory();

					// re-add the metadata
					for ( Map.Entry<URI, Value> en : metas.entrySet() ) {
						conn.add( engine.getBaseUri(),
								URI.class.cast( EngineLoader.cleanValue( en.getKey(), vf ) ),
								EngineLoader.cleanValue( en.getValue(), vf ) );
					}
				}
			} );
		}
		catch ( MalformedQueryException | QueryEvaluationException e ) {
			log.error( e, e );
		}
	}

	public static String getEngineLabel( IEngine engine ) {
		String label = engine.getEngineName();
		MetadataQuery mq = new MetadataQuery( RDFS.LABEL );
		engine.queryNoEx( mq );
		String str = mq.getString();
		if ( null != str ) {
			label = str;
		}
		return label;
	}

	/**
	 * Gets the reification model URI from the given engine
	 *
	 * @param engine
	 * @return return the reification model, or {@link Constants#NONODE} if none
	 * is defined
	 */
	public static ReificationStyle getReificationStyle( IEngine engine ) {
		URI reif = Constants.NONODE;
		if ( null != engine ) {
			MetadataQuery mq = new MetadataQuery( VAS.ReificationModel );
			try {
				engine.query( mq );
				Value str = mq.getOne();
				reif = ( null == str ? Constants.NONODE : URI.class.cast( str ) );
			}
			catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
				// don't care
			}
		}

		return ReificationStyle.fromUri( reif );
	}
}
