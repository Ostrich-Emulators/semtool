/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.query.util.impl;

import gov.va.semoss.rdf.query.util.QueryExecutorAdapter;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author John Marquiss
 */
public abstract class MapQueryAdapter<T> extends QueryExecutorAdapter<Map<String,String>> {
  public MapQueryAdapter() {
    result = new TreeMap<String,String>();
  }

  public MapQueryAdapter( String sparq ) {
    super( sparq );
    result = new TreeMap<String,String>();
  }

  public void clear() {
    result.clear();
  }
  
  public void add( String k, String v ){
    result.put( k, v );
  }
}
