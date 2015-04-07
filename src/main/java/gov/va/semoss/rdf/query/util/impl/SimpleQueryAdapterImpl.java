package gov.va.semoss.rdf.query.util.impl;

import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;

/*
 * This is a simple query adapter which returns a List of Strings for the 
 * SPARQL return variable named "returnVariable"
 */
public class SimpleQueryAdapterImpl extends ListQueryAdapter<String>{

	public SimpleQueryAdapterImpl() {}
	public SimpleQueryAdapterImpl(String query) {
		this.setSparql(query);
	}

	@Override
	public void handleTuple( BindingSet set, ValueFactory fac ) {
		add( set.getValue( "returnVariable" ).stringValue() );
	}
}
