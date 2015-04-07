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
 *****************************************************************************
 */
package gov.va.semoss.ui.components;


import org.apache.log4j.Logger;
import org.openrdf.repository.RepositoryException;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.impl.SesameJenaUpdateWrapper;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.Utility;

/**
 * The update processor class is used to process a query on a specific engine.
 */
public class UpdateProcessor {

  Logger logger = Logger.getLogger( getClass() );
  String query;
  IEngine engine;

  /**
   * Constructor for UpdateProcessor.
   */
  public UpdateProcessor() {

  }

  /**
   * Runs the query on a specific engine.
   */
  public void processQuery() {
		//if the engine has been set, it will run the query only on that engine
    //if the engine has not been set, it will run the query on all selected engines
    // RPB: ...but we can only select one engine at a time
    
    IEngine eng = ( null == engine ? DIHelper.getInstance().getRdfEngine() : engine );
//    try{
//      engine.calculateInferences();
//    }
//    catch( RepositoryException re ){
//      logger.warn( re, re );
//    }

    SesameJenaUpdateWrapper wrapper = new SesameJenaUpdateWrapper();
    wrapper.setEngine( eng );
    wrapper.setQuery( query );
    wrapper.execute();
    eng.commit();
  }

  /**
   * Sets the query.
   *
   * @param q Query, in string form.
   */
  public void setQuery( String q ) {
    query = q;
  }

  /**
   * Sets the engine.
   *
   * @param e Engine to be set.
   */
  public void setEngine( IEngine e ) {
    engine = e;
  }

}
