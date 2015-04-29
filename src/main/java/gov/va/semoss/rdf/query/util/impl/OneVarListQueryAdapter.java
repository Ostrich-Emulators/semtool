/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.query.util.impl;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;

/**
 *
 * @author ryan
 * @param <T> the type of class in the returned list
 */
public abstract class OneVarListQueryAdapter<T> extends ListQueryAdapter<T> {

  String varname;

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
    this( sparq );
    setVariableName( vname );
  }

  public OneVarListQueryAdapter( String sparq, boolean cos, String vname ) {
    super( sparq, cos );
    setVariableName( vname );
  }

  public final void setVariableName( String name ) {
    varname = name;
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

  public static OneVarListQueryAdapter<String> getStringList( String sparql, String var ) {
    return new OneVarListQueryAdapter<String>( sparql, var ) {

      @Override
      protected String getValue( Value value, ValueFactory fac ) {
        return value.stringValue();
      }
    };
  }

  public static OneVarListQueryAdapter<String> getLabels( URI subject ) {
    OneVarListQueryAdapter<String> query
        = getStringList( "SELECT ?label WHERE { ?s ?labelpred ?label }", "label" );
    query.bind( "s", subject );
    query.bind( "labelpred", RDFS.LABEL );
    return query;
  }
}
