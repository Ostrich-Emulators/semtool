/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.query.util.impl;

import org.openrdf.model.Value;

import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;

public class ListOfValueArraysQueryAdapterImpl extends ListQueryAdapter<Value[]> {

	public ListOfValueArraysQueryAdapterImpl() {
	}

	public ListOfValueArraysQueryAdapterImpl( String sparq ) {
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
