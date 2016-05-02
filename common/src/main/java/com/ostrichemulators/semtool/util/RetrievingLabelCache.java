/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.util;

import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * A class that will cache labels for URIs. Cache misses will result in a label
 * retrieval. This class is really just a convenience around
 * {@link Utility#getInstanceLabel(org.openrdf.model.Resource, com.ostrichemulators.semtool.rdf.engine.api.IEngine) }
 *
 * @author ryan
 */
public final class RetrievingLabelCache extends HashMap<Value, String> {

	private static final Logger log = Logger.getLogger( RetrievingLabelCache.class );
	private IEngine engine;
	private boolean caching = true;

	public RetrievingLabelCache( IEngine eng, boolean docache ) {
		engine = eng;
		caching = docache;
	}

	public RetrievingLabelCache( IEngine eng, Map<? extends Value, String> map ) {
		super( map );
		engine = eng;
	}

	public RetrievingLabelCache( IEngine engine ) {
		this( engine, true );
	}

	public RetrievingLabelCache() {
	}

	public void setEngine( IEngine e ) {
		engine = e;
	}

	@Override
	public boolean containsKey( Object o ) {
		return ( o instanceof Value );
	}

	public boolean isCaching() {
		return caching;
	}

	/**
	 * Should this cache actually cache results?
	 *
	 * @param b
	 */
	public void setCaching( boolean b ) {
		caching = b;
	}

	@Override
	public String get( Object value ) {
		if ( value instanceof URI ) {
			URI uri = URI.class.cast( value );
			if ( !super.containsKey( uri ) ) {
				String label = ( null == engine
						? uri.getLocalName()
						: Utility.getInstanceLabel( uri, engine ) );

				if ( caching ) {
					put( uri, label );
				}
				else {
					return label;
				}
			}
		}
		else if ( value instanceof Value ) {
			if ( caching ) {
				put( Value.class.cast( value ), value.toString() );
			}
			else {
				return value.toString();
			}
		}

		return super.get( value );
	}

	public void reset( Map<URI, String> newdata ) {
		clear();
		putAll( newdata );
	}
}
