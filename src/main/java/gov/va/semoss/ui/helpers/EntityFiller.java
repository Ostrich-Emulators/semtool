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
package gov.va.semoss.ui.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.ui.components.ParamComboBox;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.Utility;

/**
 * This gets access to the engine and runs a query with parameters, and helps
 * appropriately process the results.
 */
public class EntityFiller{
  private static final Logger logger = Logger.getLogger( EntityFiller.class );

  private String extQuery;

  public List<URI> getData( IEngine eng, String type ) {
    logger.debug( "EntityFiller using engine: " + eng );
    List<URI> names = new ArrayList<>();

    if ( null != type ) {
      // if options for the parameter have been explicitly defined on the question sheet
      // parse and use just those
      String typeprop = type + "_" + Constants.OPTION;
      if ( DIHelper.getInstance().getProperty( typeprop ) != null ) {
        // try to pick this from DBCM Properties table
        // this will typically be of the format
        String options = DIHelper.getInstance().getProperty( typeprop );

        // this is a string with ; delimited values
        for ( String s : Arrays.asList( options.split( ";" ) ) ) {
          names.add( new URIImpl( s ) );
        }
        //DIHelper.getInstance().setLocalProperty(type, names);
      }
      else if ( DIHelper.getInstance().getLocalProp( type ) == null ) {
      // the the param options have not been explicitly defined and the combo box has not been cached
        // time for the main processing

        //check if URI is used in param filler
        if ( type.startsWith( "http://" ) ) {
          // use the type query defined on RDF Map unless external query has been defined
          String sparqlQuery
              = DIHelper.getInstance().getProperty( "TYPE_" + Constants.QUERY );

          Map<String, String> paramTable = new HashMap<>();
          paramTable.put( Constants.ENTITY, type );
          sparqlQuery = ( extQuery == null
              ? Utility.fillParam( sparqlQuery, paramTable ) : extQuery );

          // get back all of the URIs that are of that type
          Collection<URI> enturis = eng.getEntityOfType( sparqlQuery );
          logger.debug( "URIs: " + enturis );

          if ( enturis.isEmpty() ) {
            names.add( ParamComboBox.MISSING_CONCEPT );
          }
          else {
            names.addAll( enturis );
          }
        }
        else {
          names.add( ParamComboBox.BAD_FILL );
        }
      }
    }
    return names;
  }

  /**
   * Gets access to engine, gets the type query based on the type of engine,
   * fills query parameters, and runs the query.
   * @param box
   * @param engine
   * @param type
   */
  public void fill( ParamComboBox box, IEngine engine, String type) {
    List<URI> names = getData( engine, type );
    Map<URI, String> labellkp = Utility.getInstanceLabels( names, engine );
    box.setData( labellkp );
    box.setEditable( false );
  }

  /**
   * Sets the external query to the given SPARQL query.
   *
   * @param query String - The SPARQL query in string form that this external
   *              query is set to.
   */
  public void setExternalQuery( String query ) {
    this.extQuery = query;
  }
}
