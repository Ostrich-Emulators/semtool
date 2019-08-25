/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.query.util.impl;

import com.ostrichemulators.semtool.rdf.query.util.QueryExecutorAdapter;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.query.BindingSet;

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

	/**
	 * Sets the model to fill as the query gets executed
	 *
	 * @param m
	 */
	public void setModel( Model m ) {
		result = m;
	}

	@Override
	public void handleTuple( BindingSet set, ValueFactory fac ) {
		// we can *sometimes* use a binding set instead of getResult().add(...)
		// *) if it has only three elements of the type Resource URI Value,

		boolean ok = false;
		if ( 3 == set.size() ) {
			String[] names = new String[3];
			getBindingNames().toArray( names );

			Resource subject = null;
			IRI predicate = null;
			Value object = set.getValue( names[2] );

			if ( set.hasBinding( names[0] )
					&& set.getValue( names[0] ) instanceof Resource ) {
				subject = Resource.class.cast( set.getValue( names[0] ) );
			}
			if ( set.hasBinding( names[1] )
					&& set.getValue( names[1] ) instanceof IRI ) {
				predicate = IRI.class.cast( set.getValue( names[1] ) );
			}

			if ( !( null == subject || null == predicate || null == object ) ) {
				ok = true;
				result.add( subject, predicate, object );
			}
		}

		if ( !ok ) {
			Logger.getLogger( getClass() ).error( "use getResults().add() instead" );
		}
	}

	public static ModelQueryAdapter describe( Resource rsr ) {
		String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }";
		ModelQueryAdapter mqa = new ModelQueryAdapter( query );
		mqa.bind( "s", rsr );
		return mqa;
	}
}
