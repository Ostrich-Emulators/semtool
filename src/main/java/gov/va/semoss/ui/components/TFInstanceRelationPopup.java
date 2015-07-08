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
package gov.va.semoss.ui.components;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JMenu;

import org.apache.log4j.Logger;

import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.impl.SesameJenaSelectStatement;
import gov.va.semoss.rdf.engine.impl.SesameJenaSelectWrapper;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.ui.transformer.LabelTransformer;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.Utility;
import java.util.Collection;
import org.openrdf.model.vocabulary.RDF;

/**
 * This class is used to create a popup menu for the TF instance relation.
 */
public class TFInstanceRelationPopup extends JMenu implements MouseListener {

	private static final Logger logger
			= Logger.getLogger( TFInstanceRelationPopup.class );
	private static final long serialVersionUID = -3788376340326236013L;
	private final GraphPlaySheet gps;
	private final Collection<SEMOSSVertex> pickedVertex;

	private String mainQuery, mainQueryJENA, neighborQuery, neighborQueryJENA;

	private boolean populated = false;

	private final IEngine engine;

	public TFInstanceRelationPopup( SEMOSSVertex vertex, IEngine e,
			GraphPlaySheet ps, Collection<SEMOSSVertex> pickedVertex ) {
		super( "Traverse Freely: " + LabelTransformer.chop(
				Utility.getInstanceLabel( vertex.getURI(), e ), 30 ) );

		this.gps = ps;
		this.engine = e;
		this.pickedVertex = pickedVertex;
		this.mainQuery = Constants.NEIGHBORHOOD_TYPE_QUERY;
		this.mainQueryJENA = Constants.NEIGHBORHOOD_TYPE_QUERY_JENA;
		this.neighborQuery = Constants.TRAVERSE_FREELY_QUERY;
		this.neighborQueryJENA = Constants.TRAVERSE_FREELY_QUERY_JENA;

		addMouseListener( this );
	}

	/**
	 * Executes query and adds appropriate relations.
	 *
	 * @param prefix	Prefix used to create the type query.
	 */
	public void addRelations( String prefix ) {
		// execute the query
		// add all the relationships
		// the relationship needs to have the subject - selected vertex
		// need to add the relationship to the relationship URI
		// and the predicate selected
		// the listener should then trigger the graph play sheet possibly
		// and for each relationship add the listener
		String typeQuery;
		if ( engine.getEngineType() == IEngine.ENGINE_TYPE.JENA ) {
			typeQuery = DIHelper.getInstance().getProperty( this.neighborQueryJENA + prefix );
		}
		else {
			typeQuery = DIHelper.getInstance().getProperty( this.neighborQuery + prefix );
		}
		Map<String, String> hash = new HashMap<>();
		String ignoreURI = DIHelper.getInstance().getProperty( Constants.IGNORE_URI );
		int count = 0;
		List<String> typeV = new ArrayList<>();
		for ( SEMOSSVertex thisVert : pickedVertex ) {

			String query2;
			if ( engine.getEngineType() == IEngine.ENGINE_TYPE.JENA ) {
				query2 = DIHelper.getInstance().getProperty( this.mainQueryJENA + prefix );
			}
			else {
				query2 = DIHelper.getInstance().getProperty( this.mainQuery + prefix );
			}
			String typeName = Utility.getConceptType( engine, thisVert.getURI().stringValue() );
			if ( typeV.contains( typeName ) ) {
				continue;
			}
			else {
				typeV.add( typeName );
			}

			if ( prefix.equals( "" ) ) {
				hash.put( "SUBJECT_TYPE", typeName );
			}
			else {
				hash.put( "OBJECT_TYPE", typeName );
			}

			// get the filter values
			String fileName = "";
			if ( engine.getEngineType() == IEngine.ENGINE_TYPE.JENA ) {
				for ( SEMOSSVertex pickedVertex1 : pickedVertex ) {
					if ( pickedVertex1.getProperty( RDF.TYPE ).equals( thisVert.getProperty( RDF.TYPE ) ) ) {
						fileName = fileName + "<" + pickedVertex1.getURI() + ">";
					}
				}
			}
			else {
				for ( SEMOSSVertex pickedVertex1 : pickedVertex ) {
					if ( pickedVertex1.getProperty( RDF.TYPE ).equals( thisVert.getProperty( RDF.TYPE ) ) ) {
						fileName = fileName + "(<" + pickedVertex1.getURI() + ">)";
					}
				}
			}

			//put in param hash and fill
			hash.put( "FILTER_VALUES", fileName );
			String filledQuery = Utility.fillParam( query2, hash );
			logger.debug( "Found the engine for repository   " + engine.getEngineName() );

			// run the query
			SesameJenaSelectWrapper sjw = new SesameJenaSelectWrapper();
			sjw.setEngine( engine );
			sjw.setEngineType( engine.getEngineType() );
			sjw.setQuery( filledQuery );
			sjw.executeQuery();

			logger.debug( "Executed Query" );

			String[] vars = sjw.getVariables();
			while ( sjw.hasNext() ) {
				SesameJenaSelectStatement stmt = sjw.next();
				// only one variable
				String objClassName = stmt.getRawVar( vars[0] ) + "";
				String pred = "";
				if ( engine.getEngineType() == IEngine.ENGINE_TYPE.JENA ) {
					pred = stmt.getRawVar( vars[1] ) + "";
				}
        //logger.debug("Predicate is " + predName + "<<>> "+ predClassName);

				//logger.debug("Filler Query is " + nFillQuery);
				// compose the query based on this class name
				// should we get type or not ?
				// that is the question
				logger.debug( "Trying predicate class name for " + objClassName );
				if ( objClassName.length() > 0 && !Utility.checkPatternInString( ignoreURI, objClassName )
						&& !objClassName.equals( "http://semoss.org/ontologies/Concept" )
						&& !objClassName.equals( "http://www.w3.org/2000/01/rdf-schema#Resource" )
						&& !objClassName.equals( "http://www.w3.org/2000/01/rdf-schema#Class" )
						&& !pred.equals( "http://semoss.org/ontologies/Relation" )
						&& ( pred.equals( "" ) || pred.startsWith( "http://semoss.org" ) ) ) {
					String inst = Utility.getInstanceName( objClassName );
					//add the to: and from: labels
					if ( count == 0 ) {
						if ( this.getItemCount() > 0 ) {
							addSeparator();
						}
						if ( prefix.equals( "" ) ) {
							addLabel( "To:" );
						}
						else {
							addLabel( "From:" );
						}
					}
					count++;

					logger.debug( "Adding Relation " + objClassName );

					if ( prefix.equals( "" ) ) {
						hash.put( "OBJECT_TYPE", objClassName );
					}
					else {
						hash.put( "SUBJECT_TYPE", objClassName );
					}
					if ( engine.getEngineType() == IEngine.ENGINE_TYPE.JENA ) {
						hash.put( "PREDICATE", pred );
					}

					String nFillQuery = Utility.fillParam( typeQuery, hash );
					logger.debug( nFillQuery );
					NeighborMenuItem nItem = new NeighborMenuItem( inst, gps, nFillQuery );
					if ( engine.getEngineType() == IEngine.ENGINE_TYPE.JENA ) {
						nItem = new NeighborMenuItem( "->" + Utility.getInstanceName( pred )
								+ "->" + inst, gps, nFillQuery );
					}
					add( nItem );
					//hash.put(objClassName, predClassName);
				}
				// for each of these relationship add a relationitem

			}
		}
		populated = true;
		repaint();
	}

	/**
	 * Invoked when the mouse button has been clicked (pressed and released) on a
	 * component.
	 *
	 * @param arg0 MouseEvent
	 */
	@Override
	public void mouseClicked( MouseEvent arg0 ) {
		//addRelations("");
		//addRelations("_2");
	}

	/**
	 * Invoked when the mouse enters a component.
	 *
	 * @param arg0 MouseEvent
	 */
	@Override
	public void mouseEntered( MouseEvent arg0 ) {
		if ( !populated ) {
			addRelations( "" );
			addRelations( "_2" );
		}
		//	addRelations();

	}

	/**
	 * Adds label.
	 *
	 * @param label Label, in string form.
	 */
	public void addLabel( String label ) {
		add( new JLabel( label ) );
	}

	/**
	 * Invoked when the mouse exits a component.
	 *
	 * @param arg0 MouseEvent
	 */
	@Override
	public void mouseExited( MouseEvent arg0 ) {

	}

	/**
	 * Invoked when a mouse button has been pressed on a component.
	 *
	 * @param arg0 MouseEvent
	 */
	@Override
	public void mousePressed( MouseEvent arg0 ) {

	}

	/**
	 * Invoked when a mouse button has been released on a component.
	 *
	 * @param arg0 MouseEvent
	 */
	@Override
	public void mouseReleased( MouseEvent arg0 ) {

	}
}
