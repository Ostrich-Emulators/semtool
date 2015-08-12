/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.query.util.impl;

import gov.va.semoss.rdf.query.util.impl.ListQueryAdapter;
import java.util.List;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;

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

	public static OneVarListQueryAdapter<URI> getUriList( String sparql, String var ) {
		return new OneVarListQueryAdapter<URI>( sparql, var ) {

			@Override
			protected URI getValue( Value value, ValueFactory fac ) {
				return URI.class.cast( value );
			}
		};
	}

	public static OneVarListQueryAdapter<URI> getUriList( String sparql ) {
		return getUriList( sparql, null );
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

	public static OneVarListQueryAdapter<String> getLabels( URI subject ) {
		OneVarListQueryAdapter<String> query
				= getStringList( "SELECT ?label WHERE { ?s ?labelpred ?label }", "label" );
		query.bind( "s", subject );
		query.bind( "labelpred", RDFS.LABEL );
		return query;
	}
}
