/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.util;

import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
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
		if ( value instanceof Value ) {
			Value val = Value.class.cast( value );
			if ( !super.containsKey( val ) ) {
				String label = cache( val );
				if ( !caching ) {
					return label;
				}
			}
		}

		return super.get( value );
	}

	/**
	 * Caches a label for the given value. If the value is already cached, null is
	 * returned (NOT the cached value).
	 *
	 * @param value
	 * @return
	 */
	public String cache( Value value ) {
		Value val = Value.class.cast( value );
		String label = null;

		if ( !super.containsKey( val ) ) {
			if ( value instanceof URI ) {
				URI uri = URI.class.cast( value );
				label = ( null == engine
						? uri.getLocalName()
						: Utility.getInstanceLabel( uri, engine ) );
			}
			else if ( value instanceof Literal ) {
				Literal lit = Literal.class.cast( value );
				label = lit.getLabel();
			}
			else {
				label = value.toString();
			}

			if ( caching ) {
				put( Value.class.cast( value ), label );
			}
		}

		return label;
	}

	public void cache( Collection<? extends Value> values ) {
		for ( Value val : values ) {
			if ( !super.containsKey( val ) ) {
				cache( val );
			}
		}
	}

	public void reset( Map<Value, String> newdata ) {
		clear();
		putAll( newdata );
	}
}
