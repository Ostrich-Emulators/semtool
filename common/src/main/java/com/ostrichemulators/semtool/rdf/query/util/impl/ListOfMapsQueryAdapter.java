/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.query.util.impl;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.rdf4j.model.Value;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.BindingSet;

/**
 *
 * @author John Marquiss
 * @param <T>
 */
public abstract class ListOfMapsQueryAdapter<T> extends ListQueryAdapter<Map<String, T>> {

  public ListOfMapsQueryAdapter() {
  }

  public ListOfMapsQueryAdapter( String sparq ) {
    super( sparq );
  }

  @Override
  public void handleTuple( BindingSet set, ValueFactory fac ) {
    Map<String, T> map = new HashMap<>();
    for ( String variable : set.getBindingNames() ) {
      map.put( variable, convertValue( variable, set.getValue( variable ), fac ) );
    }
    add( map );
  }

  protected abstract T convertValue( String variable, Value v, ValueFactory fac );

  public static ListOfMapsQueryAdapter<String> forStrings( String sparql ) {
    ListOfMapsQueryAdapter<String> q = new ListOfMapsQueryAdapter<String>( sparql ) {

      @Override
      protected String convertValue( String variable, Value v, ValueFactory fac ) {
        return v.stringValue();
      }
    };

    return q;
  }
}
