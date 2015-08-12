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

import javax.swing.JMenu;

import org.apache.log4j.Logger;

import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.util.NodeDerivationTools;
import gov.va.semoss.rdf.query.util.impl.ModelQueryAdapter;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.ui.transformer.LabelTransformer;
import gov.va.semoss.util.GuiUtility;
import gov.va.semoss.util.Utility;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JLabel;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDFS;

/**
 * This class is used to create a popup menu for the TF instance relation.
 */
public class TraverseFreelyPopup extends JMenu implements MouseListener {

	private static final Logger logger
			= Logger.getLogger( TraverseFreelyPopup.class );
	private static final long serialVersionUID = -3788376340326236013L;
	private final GraphPlaySheet gps;
	private final Collection<SEMOSSVertex> pickedVertex;
	private final boolean isInstance;
	private boolean populated = false;

	private final IEngine engine;

	public TraverseFreelyPopup( SEMOSSVertex vertex, IEngine e,
			GraphPlaySheet ps, Collection<SEMOSSVertex> pickedVertex, boolean instance ) {
		super( "Traverse Freely: "
				+ ( instance
						? LabelTransformer.chop( GuiUtility.getInstanceLabel( vertex.getURI(), e ), 30 )
						: "All " + GuiUtility.getInstanceLabel( vertex.getType(), e ) + "(s) " ) );
		this.isInstance = instance;
		this.gps = ps;
		this.engine = e;
		this.pickedVertex = pickedVertex;

		addMouseListener( this );
	}

	/**
	 * Queries the database to get neighboring instance types
	 *
	 * @param subjectStyle are the <code>pickedVertex</code>s subjects or objects?
	 * @return the number of items added
	 */
	public int addRelations( boolean subjectStyle ) {
		// execute the query
		// add all the relationships
		// the relationship needs to have the subject - selected vertex
		// need to add the relationship to the relationship URI
		// and the predicate selected
		// the listener should then trigger the graph play sheet possibly
		// and for each relationship add the listener
		Set<URI> neighborTypes = new HashSet<>();
		Set<URI> instances = new HashSet<>();

		for ( SEMOSSVertex thisVert : pickedVertex ) {
			instances.add( thisVert.getURI() );
		}

		if ( isInstance ) {
			for ( URI instance : instances ) {
				neighborTypes.addAll(
						NodeDerivationTools.getConnectedConceptTypes( instance, engine,
								subjectStyle ) );
			}
		}
		else {
			neighborTypes.addAll(
					NodeDerivationTools.getConnectedConceptTypes( instances, engine,
							subjectStyle ) );
		}

		for ( URI nt : neighborTypes ) {
			logger.debug( "neighbor type: " + nt );
		}

		neighborTypes.removeAll( Arrays.asList( RDFS.RESOURCE, RDFS.CLASS,
				OWL.NOTHING, OWL.THING, OWL.CLASS ) );

		Map<URI, String> labelmap
				= GuiUtility.getInstanceLabels( neighborTypes, engine );
		labelmap = Utility.sortUrisByLabel( labelmap );

		if ( !labelmap.isEmpty() ) {
			add( new JLabel( subjectStyle ? "To:" : "From:" ) );
		}

		for ( Map.Entry<URI, String> en : labelmap.entrySet() ) {
			add( new NeighborMenuItem( en.getValue(), gps,
					getExpander( instances, en.getKey(), subjectStyle ) ) );
		}

		populated = true;
		return neighborTypes.size();
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
	 * Invoked when the mouse enters a component.
	 *
	 * @param arg0 MouseEvent
	 */
	@Override
	public void mouseEntered( MouseEvent arg0 ) {
		if ( !populated ) {
			int added = addRelations( true );
			if ( added > 0 ) {
				addSeparator();
			}
			addRelations( false );
		}
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

	private static ModelQueryAdapter getExpander( Collection<URI> instances, URI totype,
			boolean instanceIsSubject ) {
		StringBuilder query = new StringBuilder( "CONSTRUCT { ?subject ?predicate ?object } " )
				.append( "WHERE { " )
				.append( "  ?subject ?predicate ?object ." )
				.append( "  ?subject a ?subtype ." )
				.append( "  ?object a ?objtype ." );
		if ( instanceIsSubject ) {
			query.append( " VALUES ?subject {" )
					.append( Utility.implode( instances, "<", ">", " " ) )
					.append( "} ." );
		}
		else {
			query.append( " VALUES ?object {" )
					.append( Utility.implode( instances, "<", ">", " " ) )
					.append( "}" );
		}
		query.append( "  MINUS { ?subject a ?object } " )
				.append( "}" );

		logger.debug( "expander query is: " + query );
		logger.debug( ( instanceIsSubject ? "objtype" : "subtype" ) + ": " + totype );

		ModelQueryAdapter mqa = new ModelQueryAdapter( query.toString() );
		mqa.bind( instanceIsSubject ? "objtype" : "subtype", totype );
		return mqa;
	}
}
