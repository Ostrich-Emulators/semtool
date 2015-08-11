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
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.GuiUtility;
import gov.va.semoss.util.Utility;
import java.util.Collection;
import org.openrdf.model.URI;

/**
 */
public class TFRelationPopup extends JMenu implements MouseListener {

	private static final long serialVersionUID = 4029501595464135469L;

	private GraphPlaySheet ps = null;
	private final Collection<SEMOSSVertex> pickedVertex;
	private static final Logger logger = Logger.getLogger( TFRelationPopup.class );

	private static final String mainQuery = Constants.NEIGHBORHOOD_TYPE_QUERY;
	private static final String neighborQuery = Constants.TRAVERSE_FREELY_QUERY;
	private boolean populated = false;

	/**
	 * Constructor for TFRelationPopup.
	 *
	 * @param name String
	 * @param ps IPlaySheet
	 * @param pickedVertex DBCMVertex[]
	 */
	public TFRelationPopup( SEMOSSVertex vertex, GraphPlaySheet ps,
			Collection<SEMOSSVertex> pickedVertex ) {
		super("Traverse Freely: All "
				+ GuiUtility.getInstanceLabel( vertex.getType(), ps.getEngine() ) + "(s) " );

		this.ps = ps;
		this.pickedVertex = pickedVertex;

		addMouseListener( this );
	}

	/**
	 * Executes query and adds appropriate relations.
	 *
	 * @param prefix	Prefix used to create the type query.
	 */
	public void addRelations( String prefix ) {
		// get the selected repository
		IEngine engine = ps.getEngine();
		// execute the query
		// add all the relationships
		// the relationship needs to have the subject - selected vertex
		// need to add the relationship to the relationship URI
		// and the predicate selected
		// the listener should then trigger the graph play sheet possibly
		// and for each relationship add the listener
		String typeQuery
				= DIHelper.getInstance().getProperty( TFRelationPopup.neighborQuery + prefix );

		Map<String, String> hash = new HashMap<>();
		String ignoreURI = DIHelper.getInstance().getProperty( Constants.IGNORE_URI );
		int count = 0;
		List<String> typeV = new ArrayList<>();
		for ( SEMOSSVertex thisVert : pickedVertex ) {

			String query2
					= DIHelper.getInstance().getProperty( TFRelationPopup.mainQuery + prefix );

			String typeName = GuiUtility.getConceptType( engine, thisVert.getURI().stringValue() );
			if ( typeV.contains( typeName ) ) {
				continue;
			}
			else {
				typeV.add( typeName );
			}
			URI type = thisVert.getType();
			if ( prefix.equals( "" ) ) {
				hash.put( "SUBJECT_TYPE", typeName );
			}
			else {
				hash.put( "OBJECT_TYPE", typeName );
			}

			// get the filter values
			String fileName = "";

			List<SEMOSSVertex> vertVector = ps.getVerticesByType().get( type );
			logger.debug( "Vert vector size is " + vertVector.size() );

			if ( engine.getEngineType() == IEngine.ENGINE_TYPE.JENA ) {
				for ( int vertIndex = 0; vertIndex < vertVector.size(); vertIndex++ ) {
					if ( vertIndex == 0 ) {
						fileName = "<" + vertVector.get( vertIndex ).getURI() + ">";
					}
					else {
						fileName = fileName + "<" + vertVector.get( vertIndex ).getURI() + ">";
					}
				}
			}
			else {
				for ( int vertIndex = 0; vertIndex < vertVector.size(); vertIndex++ ) {
					if ( vertIndex == 0 ) {
						fileName = "(<" + vertVector.get( vertIndex ).getURI() + ">)";
					}
					else {
						fileName = fileName + "(<" + vertVector.get( vertIndex ).getURI() + ">)";
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
					String instance = GuiUtility.getInstanceName( objClassName );

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

					NeighborMenuItem nItem = new NeighborMenuItem( instance, ps, nFillQuery );
					if ( engine.getEngineType() == IEngine.ENGINE_TYPE.JENA ) {
						nItem = new NeighborMenuItem( "->" + GuiUtility.getInstanceName( pred )
								+ "->" + instance, ps, nFillQuery );
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
	}

	/**
	 * Invoked when the mouse enters a component. Adds relations if it is not
	 * populated.
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
