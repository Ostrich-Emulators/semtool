/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.query.util.impl;

import com.ostrichemulators.semtool.rdf.query.util.QueryExecutorAdapter;
import java.util.List;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.BindingSet;

/**
 * A query adapter that returns a single value for the variable. If no variable
 * is explicitly defined, the first binding variable will be returned. If the
 * query returns many tuples, only the first one returned.
 *
 * @author ryan
 * @param <T> the type of class in the returned list
 */
public abstract class OneValueQueryAdapter<T> extends QueryExecutorAdapter<T> {

	private String varname = null;

	public OneValueQueryAdapter() {
	}

	public OneValueQueryAdapter( String sparq ) {
		super( sparq );
	}

	public OneValueQueryAdapter( String sparq, String vname ) {
		super( sparq );
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
		if ( null == result ) {
			result = getValue( set.getValue( varname ), fac );
		}
	}

	protected abstract T getValue( Value val, ValueFactory fac );

	public static OneValueQueryAdapter<IRI> getUri( String sparql, String var ) {
		return new OneValueQueryAdapter<IRI>( sparql, var ) {

			@Override
			protected IRI getValue( Value value, ValueFactory fac ) {
				return IRI.class.cast( value );
			}
		};
	}

	public static OneValueQueryAdapter<IRI> getUri( String sparql ) {
		return getUri( sparql, null );
	}

	public static OneValueQueryAdapter<String> getString( String sparql, String var ) {
		return new OneValueQueryAdapter<String>( sparql, var ) {

			@Override
			protected String getValue( Value value, ValueFactory fac ) {
				return value.stringValue();
			}
		};
	}

	public static OneValueQueryAdapter<String> getString( String sparql ) {
		return getString( sparql, null );
	}

	public static OneValueQueryAdapter<Double> getDouble( String sparql, String var ) {
		return new OneValueQueryAdapter<Double>( sparql, var ) {

			@Override
			protected Double getValue( Value value, ValueFactory fac ) {
				return Literal.class.cast( value ).doubleValue();
			}
		};
	}

	public static OneValueQueryAdapter<Double> getDouble( String sparql ) {
		return getDouble( sparql, null );
	}

	public static OneValueQueryAdapter<Boolean> getBoolean( String sparql, String var ) {
		return new OneValueQueryAdapter<Boolean>( sparql, var ) {

			@Override
			protected Boolean getValue( Value value, ValueFactory fac ) {
				return ( null == value ? false : Literal.class.cast( value ).booleanValue() );
			}
		};
	}

	public static OneValueQueryAdapter<Boolean> getBoolean( String sparql ) {
		return getBoolean( sparql, null );
	}
}
