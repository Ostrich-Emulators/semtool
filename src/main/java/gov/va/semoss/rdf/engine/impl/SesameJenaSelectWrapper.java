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
package gov.va.semoss.rdf.engine.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.util.Utility;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import gov.va.semoss.rdf.query.util.QueryExecutorAdapter;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.QueryEvaluationException;

/**
 * The wrapper helps takes care of selection of the type of engine you are using
 * (Jena/Sesame). This wrapper processes SELECT statements.
 */
public class SesameJenaSelectWrapper {

  TupleQueryResult tqr = null;
  ResultSet rs = null;
  Enum engineType = IEngine.ENGINE_TYPE.SESAME;
  QuerySolution curSt = null;
  public IEngine engine = null;
  String query = null;
  private static final Logger logger
      = Logger.getLogger( SesameJenaSelectWrapper.class );
  String[] var = null;
	
  /**
   * Method setEngine. Sets the engine type.
   *
   * @param engine IEngine - The engine type being set.
   */
  public void setEngine( IEngine engine ) {
    logger.debug( "Set the engine " );
    this.engine = engine;
    if ( engine == null ) {
      engineType = IEngine.ENGINE_TYPE.JENA;
    }
    else {
      engineType = engine.getEngineType();
    }
  }

  /**
   * Method setQuery. - Sets the SPARQL query statement.
   *
   * @param query String - The string version of the SPARQL query.
   */
  public void setQuery( String query ) {
    logger.debug( "Setting the query " + query );
    this.query = query;
  }

  /**
   * Method executeQuery. Executes the SPARQL query based on the type of engine
   * selected.
   */
  public void executeQuery() {
    if ( engineType == IEngine.ENGINE_TYPE.SESAME ) {
      tqr = (TupleQueryResult) engine.execSelectQuery( query );
    }
    else if ( engineType == IEngine.ENGINE_TYPE.JENA ) {
      rs = (ResultSet) engine.execSelectQuery( query );
    }
  }

  /**
   * Method getVariables. Based on the type of engine, this returns the
   * variables from the query result.
   *
   * @return String[] - An array containing the names of the variables from the
   *         result.
	 *
   */
  public String[] getVariables() {
    if ( var != null ) {
      return var;
    }

    if ( engineType == IEngine.ENGINE_TYPE.SESAME ) {
      return getVariablesFromSesame();
    }

    if ( engineType == IEngine.ENGINE_TYPE.JENA ) {
      return getVariablesFromJena();
    }

    return null;
  }

  /**
   * Method getVariablesFromSesame. This returns the sesame variables from the
   * query result.
   *
   * @return String[] - An array containing the names of the variables from the
   *         result.
	 *
   */
  private String[] getVariablesFromSesame() {
    if ( tqr == null ) {
      logger.warn( "Sesame Query is malformed." );
      return null;
    }

    try {
      var = new String[tqr.getBindingNames().size()];
      List<String> names = tqr.getBindingNames();
      for ( int colIndex = 0; colIndex < names.size(); colIndex++ ) {
        var[colIndex] = names.get( colIndex );
      }
    }
    catch ( Exception e ) {
      logger.error( e );
    }
    return var;
  }

  /**
   * Method getVariablesFromJena. This returns the jena variables from the query
   * result.
   *
   * @return String[] - An array containing the names of the variables from the
   *         result.
	 *
   */
  private String[] getVariablesFromJena() {
    if ( rs == null ) {
      logger.warn( "Jena Query is malformed." );
      return null;
    }

    var = new String[rs.getResultVars().size()];
    List<String> names = rs.getResultVars();
    for ( int colIndex = 0; colIndex < names.size(); colIndex++ ) {
      var[colIndex] = names.get( colIndex );
    }

    return var;
  }

  /**
   * Checks to see if the tuple query result has additional results.
   *
   * @return boolean - True if the Tuple Query result has additional results.
	 *
   */
	public boolean hasNext() {
		boolean retBool = false;
		if ( IEngine.ENGINE_TYPE.SESAME == engineType ) {
			try {
				retBool = tqr.hasNext();
				if ( !retBool ) {
					tqr.close();
				}
			}
			catch ( QueryEvaluationException ex ) {
				logger.error( ex );
			}
		}
		else {
			retBool = rs.hasNext();
		}

		return retBool;
	}

  /**
   * Method next. Processes the select statement for either Sesame or Jena.
   *
   * @return SesameJenaSelectStatement - returns the select statement.
	 *
   */
  public SesameJenaSelectStatement next() {
    SesameJenaSelectStatement retSt = new SesameJenaSelectStatement();
    try {
      if ( IEngine.ENGINE_TYPE.SESAME == engineType ) {
        logger.debug( "Adding a sesame statement " );
        BindingSet bs = tqr.next();
        for ( String var1 : var ) {
          Value val = bs.getValue( var1 );

					if( null == val ){
						retSt.setVar( var1, "" );
					}
					else if( val instanceof Literal ){
						Literal lval = Literal.class.cast( val );
						URI datatype = lval.getDatatype();
						if( XMLSchema.DATETIME.equals( datatype ) ){
							retSt.setVar(  var1 , QueryExecutorAdapter.getDate( lval.calendarValue() ) );
						}
						else if( XMLSchema.DOUBLE.equals( datatype ) ||
								XMLSchema.DECIMAL.equals( datatype ) ){
							retSt.setVar( var1, lval.doubleValue() );
						}
						else{
							if( XMLSchema.STRING.equals( datatype ) ){
								logger.warn( "treating datatype as string: " + datatype );
							}
							
							retSt.setVar( var1, lval.stringValue() );
						}
					}
					else if( val instanceof URI ) {
						URI u = URI.class.cast( val );
						// FIXME: Utility.getInstanceLabel() is much slower than 
						// Utility.getInstanceLabels(), but we'll use a cache to help a bit
//						if( !labelcache.containsKey( u ) ){
//							labelcache.put( u, Utility.getInstanceLabel( u, engine ) );
//						}
//						retSt.setVar( var1, labelcache.get( u ) );
						retSt.setVar( var1, u );
					}

          retSt.setRawVar( var1, val );
        }
      }
      else {
        QuerySolution row = rs.nextSolution();
        curSt = row;
        for ( String var1 : var ) {
          String value = row.get( var1 ) + "";
          RDFNode node = row.get( var1 );
          if ( node.isAnon() ) {
            logger.debug( "Ok.. an anon node" );
            String id = Utility.getNextID();
            retSt.setVar( var1, id );
          }
          else {
            logger.debug( "Raw data JENA For Column " + var1 + " >>  " + value );
						if( node.isURIResource() ){
							URI newu = new URIImpl( node.asResource().toString() );
							retSt.setVar( var1, newu );
						}
						else{
							retSt.setVar( var1, value );
						}						
          }
          retSt.setRawVar( var1, value );
          logger.debug( "Binding Name: " + var1 + " Value: " + value );
        }
        logger.debug( "Adding a JENA statement " );
      }
    }
    catch ( Exception ex ) {
      logger.error( ex, ex );
    }
    return retSt;
  }

  /**
   * Method getJenaStatement. Gets the query solution for a JENA model.
   *
   * @return QuerySolution
   */
  public QuerySolution getJenaStatement() {
    return curSt;
  }

  /**
   * Method setEngineType. Sets the engine type.
   *
   * @param engineType Enum - The type engine that this is being set to.
   */
  public void setEngineType( Enum engineType ) {
    this.engineType = engineType;
  }

  /**
   * Method setResultSet. Sets the result set.
   *
   * @param rs ResultSet - The result set.
   */
  public void setResultSet( ResultSet rs ) {
    this.rs = rs;
  }
}
