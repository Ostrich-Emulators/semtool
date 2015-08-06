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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.ui.components.UriComboBox;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.Utility;

/**
 * This gets access to the engine and runs a query with parameters for a
 * subclass, and helps appropriately process the results.
 */
public class EntityFillerForSubClass implements Runnable {

  private static final Logger log
      = Logger.getLogger( EntityFillerForSubClass.class );
  private final List<JComboBox> boxes;
  private final String parent;
  private final IEngine engine;
  String sparqlQuery = "SELECT ?entity WHERE {?entity <http://www.w3.org/2000/01/rdf-schema#subClassOf> <";

  public EntityFillerForSubClass( IEngine engine, List<JComboBox> boxes,
      String parent ) {
    this.engine = engine;
    this.boxes = boxes;
    this.parent = parent;
  }

  /**
   * Gets access to engine, gets the type query based on the type of engine,
   * fills query parameters, and runs the query.
   */
  @Override
  public void run() {
    if ( null == engine ) {
      log.warn( "skipping run for null engine" );
      return;
    }

    List<URI> names = new ArrayList<>();

    String entityNS = DIHelper.getInstance().getProperty( parent );
    if ( entityNS != null ) {
      sparqlQuery = sparqlQuery + entityNS + "/" + parent + "> ;}";
      names.addAll( engine.getEntityOfType( sparqlQuery ) );
// FIXME: RPB: not sure this is the right thing to do here
//      if ( engine instanceof AbstractEngine ) {
//        RDFFileSesameEngine baseeng = ( (AbstractEngine) engine ).getBaseDataEngine();
//        if ( null != baseeng ) {
//          Collection<String> baseNames = baseeng.getEntityOfType( sparqlQuery );
//          for ( String name : baseNames ) {
//            if ( !allnames.contains( name ) ) {
//              allnames.add( name );
//            }
//          }
//        }
//      }
      if( log.isDebugEnabled() ){
       for( URI n :names ){
          log.debug( "engine.getEntityOfType: "+n );
        }
      }
    }

    Map<URI, String> labels = Utility.getInstanceLabels( names, engine );
    for ( JComboBox box : boxes ) {
      if ( box != null ) {
        // if it is a paramcombobox, set the whole hashtable--will need to look
        // up the URI for selected label later
        if ( box instanceof UriComboBox ) {
          UriComboBox.class.cast( box ).setData( labels );
        }
        else {
          // else just set the model on the box with the list
  
          Map<String, String> paramHash = new HashMap<>();
          List<String> nameVector = new ArrayList<>();
          for ( Map.Entry<URI, String> en : labels.entrySet() ) {
            nameVector.add( en.getKey().stringValue() );
            paramHash.put( en.getValue(), en.getKey().stringValue() );
          }

          DefaultComboBoxModel model = new DefaultComboBoxModel( nameVector.toArray() );
          box.setModel( model );
        }
      }
    }
  }
}
