/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.query.util.impl;

import gov.va.semoss.rdf.query.util.QueryExecutorAdapter;
import org.apache.log4j.Logger;
import org.openrdf.model.Model;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.query.BindingSet;

/**
 *
 * @author ryan
 */
public class ModelQueryAdapter extends QueryExecutorAdapter<Model> {

	public ModelQueryAdapter() {
		result = new LinkedHashModel();
	}

	public ModelQueryAdapter( String sparq ) {
		this( sparq, new LinkedHashModel() );
	}

	public ModelQueryAdapter( String sparql, Model model ) {
		super( sparql );
		result = model;
	}

	@Override
	public void handleTuple( BindingSet set, ValueFactory fac ) {
		Logger.getLogger( getClass() ).error( "use getResults().add() instead" );
	}
}
