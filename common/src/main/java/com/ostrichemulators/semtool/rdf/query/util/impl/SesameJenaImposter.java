/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.query.util.impl;


import com.ostrichemulators.semtool.util.RDFDatatypeTools;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.BindingSet;

/**
 * A class to mimic the legacy SesameJenaSelectWrapper class to ease the
 * transition
 *
 * @author ryan
 */
public class SesameJenaImposter extends ListQueryAdapter<Object[]> {

	public SesameJenaImposter() {
	}

	public SesameJenaImposter( String sparq ) {
		super( sparq );
	}

	@Override
	public void handleTuple( BindingSet set, ValueFactory fac ) {
		Object[] vals = new Object[getNumBindings()];
		int idx = 0;
		for ( String name : getBindingNames() ) {
			Value v = set.getValue( name );
			vals[idx] = convertToBaseObject( v );

			idx++;
		}
		add( vals );
	}

	private static Object convertToBaseObject( Value v ) {
		if ( null == v || v instanceof IRI ) {
			return v;
		}

		if ( v instanceof Literal ) {
			Literal l = Literal.class.cast( v );
			return RDFDatatypeTools.getObjectFromValue( l );
		}
		return v;
	}

	public String[] getVariables() {
		return this.getBindingNames().toArray( new String[0] );
	}
}
