/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.query.util.impl;

import java.util.List;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.BindingSet;

/**
 * A query adapter that returns one variable. If no variable is explicitly
 * defined, the first binding variable will be returned
 *
 * @author ryan
 * @param <T> the type of class in the returned list
 */
public abstract class OneVarListQueryAdapter<T> extends ListQueryAdapter<T> {

	private String varname = null;

	public OneVarListQueryAdapter() {
	}

	public OneVarListQueryAdapter( boolean cos ) {
		super( cos );
	}

	public OneVarListQueryAdapter( String sparq ) {
		super( sparq );
	}

	public OneVarListQueryAdapter( String sparq, boolean cos ) {
		super( sparq, cos );
	}

	public OneVarListQueryAdapter( String sparq, String vname ) {
		super( sparq );
	}

	public OneVarListQueryAdapter( String sparq, boolean cos, String vname ) {
		super( sparq, cos );
		setVariableName( vname );
	}

	public final void setVariableName( String name ) {
		varname = name;
	}

	@Override
	public void start( List<String> bnames ) {
		super.start( bnames );
		if ( null == varname && !bnames.isEmpty() ) {
			varname = bnames.get( 0 );
		}
	}

	@Override
	public void handleTuple( BindingSet set, ValueFactory fac ) {
		add( getValue( set.getValue( varname ), fac ) );
	}

	protected abstract T getValue( Value val, ValueFactory fac );

	public static OneVarListQueryAdapter<IRI> getIriList( String sparql, String var ) {
		return new OneVarListQueryAdapter<IRI>( sparql, var ) {

			@Override
			protected IRI getValue( Value value, ValueFactory fac ) {
				return IRI.class.cast( value );
			}
		};
	}

	public static OneVarListQueryAdapter<IRI> getIriList( String sparql ) {
		return getIriList( sparql, null );
	}

	public static OneVarListQueryAdapter<String> getStringList( String sparql, String var ) {
		return new OneVarListQueryAdapter<String>( sparql, var ) {

			@Override
			protected String getValue( Value value, ValueFactory fac ) {
				return value.stringValue();
			}
		};
	}

	public static OneVarListQueryAdapter<String> getStringList( String sparql ) {
		return getStringList( sparql, null );
	}

	public static OneVarListQueryAdapter<String> getLabels( IRI subject ) {
		OneVarListQueryAdapter<String> query
				= getStringList( "SELECT ?label WHERE { ?s rdfs:label ?label }" );
		query.bind( "s", subject );
		return query;
	}
}
