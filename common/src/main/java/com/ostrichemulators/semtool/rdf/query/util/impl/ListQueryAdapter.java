/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.query.util.impl;

import com.ostrichemulators.semtool.rdf.query.util.QueryExecutorAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ryan
 * @param <T> the type of class in the returned list
 */
public abstract class ListQueryAdapter<T> extends QueryExecutorAdapter<List<T>> {

  private boolean clearOnStart;

  public ListQueryAdapter() {
    this( true );
  }

  /**
   * Ctor
   * @param cos should this adapter clear its results list on start?
   */
  public ListQueryAdapter( boolean cos ) {
    result = new ArrayList<>();
    clearOnStart = cos;
  }

  /**
   * Convenience {@link #ListQueryAdapter(java.lang.String, boolean) 
   * ListQueryAdapter( sparql, true )}
   * @param sparq the sparql to execute
   */
  public ListQueryAdapter( String sparq ) {
    this( sparq, true );
  }

  /**
   * Ctor
   * @param sparq the sparql to execute
   * @param cos should this adapter clear its results list on start?
   */
  public ListQueryAdapter( String sparq, boolean cos ) {
    super( sparq );
    result = new ArrayList<>();
    clearOnStart = cos;
  }

  public void clear() {
    result.clear();
  }

  public void add( T t ) {
    result.add( t );
  }
  
  @Override
  public void start( List<String> bnames ){
    if( clearOnStart ){
      result.clear();
    }
    super.start( bnames );
  }
}
