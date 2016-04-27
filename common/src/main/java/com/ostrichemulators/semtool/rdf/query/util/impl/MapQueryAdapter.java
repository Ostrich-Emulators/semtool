/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.query.util.impl;

import com.ostrichemulators.semtool.rdf.query.util.QueryExecutorAdapter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author John Marquiss
 */
public abstract class MapQueryAdapter<T,V> extends QueryExecutorAdapter<Map<T, V>> {
  public MapQueryAdapter() {
    result = new HashMap<>();
  }

  public MapQueryAdapter( String sparq ) {
    super( sparq );
    result = new HashMap<>();
  }

  public void clear() {
    result.clear();
  }
  
  public void add( T k, V v ){
    result.put( k, v );
  }
}
