/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.query.util.impl;

import org.openrdf.model.Value;

import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;

public class ListOfValueArraysQueryAdapter extends ListQueryAdapter<Value[]> {

	public ListOfValueArraysQueryAdapter() {
	}

	public ListOfValueArraysQueryAdapter( String sparq ) {
		super( sparq );
	}

	@Override
	public void handleTuple( BindingSet set, ValueFactory fac ) {
		Value[] vals = new Value[getNumBindings()];
		int idx = 0;
		for ( String name : getBindingNames() ) {
			vals[idx] = set.getValue( name );
			idx++;
		}
		add( vals );
	}
}
