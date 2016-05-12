/**
 * *****************************************************************************
 * Copyright 2013 SEMOSS.ORG
 *
 * This file is part of SEMOSS.
 *
 * SEMOSS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SEMOSS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SEMOSS. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package com.ostrichemulators.semtool.ui.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 * This class allows the user to pick parameters by combining a button and a
 * drop-down list.
 */
public class ParamComboBox extends UriComboBox {

  private static final Logger log = Logger.getLogger( ParamComboBox.class );
  private String fieldName = null;
  private final List<String> dependency = new ArrayList<>();
  private String query;
  private URI type;

  /**
   * Constructor for ParamComboBox.
   *
   * @param array String[]
   */
  public ParamComboBox( URI[] array ) {
    super( array );
  }

  public ParamComboBox( Collection<URI> array ) {
    super( array );
  }

  public ParamComboBox() {
  }
  
  public void setType( String typ ) {
    type = new URIImpl( typ );
  }

  public String getType() {
    return type.stringValue();
  }

  public String getQuery() {
    return query;
  }

  /**
   * Sets the name of the parameter.
   *
   * @param fieldName Parameter name.
   */
  public void setParamName( String fieldName ) {
    this.fieldName = fieldName;
  }

  /**
   * Gets the name of the parameter.
   *
   * @return the Parameter name
   */
  public String getParamName() {
    return this.fieldName;
  }

  /**
   * Gets the URIs based on keys of the parameters hashtable.
   *
   * @param key Parameter whose URI the user wants.
   *
   * @return String	URI.
   */
  public String getURI( String key ) {
    return key;
  }

  /**
   * Sets dependencies.
   *
   * @param dep	List of dependencies.
   */
  public void setDependency( List<String> dep ) {
    this.dependency.clear();
    this.dependency.addAll( dep );
  }

  public Collection<String> getDependencies() {
    return new ArrayList<>( dependency );
  }

  /**
   * Sets the query for execution.
   *
   * @param query Query to be run.
   */
  public void setQuery( String query ) {
    this.query = query;
  }
  
}
