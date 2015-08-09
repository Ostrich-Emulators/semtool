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
package gov.va.semoss.ui.components.playsheets;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import org.apache.log4j.Logger;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.om.TreeGraphDataModel;
import gov.va.semoss.ui.main.listener.impl.DuplicatingPickedStateListener;
import gov.va.semoss.ui.main.listener.impl.GraphNodeListener;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

/**
 */
public class TreeGraphPlaySheet extends GraphPlaySheet {

	private static final Logger log = Logger.getLogger( TreeGraphPlaySheet.class );
	private TreeGraphDataModel model;

	/**
	 * Constructor for GraphPlaySheetFrame.
	 */
	public TreeGraphPlaySheet() {
		this( new TreeGraphDataModel(), Constants.TREE_LAYOUT );
	}

	public TreeGraphPlaySheet( TreeGraphDataModel model, String layoutname ) {
		super( model );
		this.model = model;
		log.debug( "new TreeGrap PlaySheet" );

		setLayoutName( layoutname );
		controlPanel.setForTree( true );
		fixVis();

		for ( SEMOSSVertex v : model.getForest().getVertices() ) {
			v.addPropertyChangeListener( this );
		}

		for ( SEMOSSEdge e : model.getForest().getEdges() ) {
			e.addPropertyChangeListener( this );
		}
	}

	private void fixVis() {
		VisualizationViewer<SEMOSSVertex, SEMOSSEdge> view = getView();
		GraphNodeListener gl = new GraphNodeListener( this );
		gl.setMode( ModalGraphMouse.Mode.PICKING );
		view.setGraphMouse( gl );

		setPicker( new DuplicatingPickedStateListener( view, this ) );
	}

	public Set<SEMOSSVertex> getDuplicates( SEMOSSVertex v ) {
		return model.getDuplicatesOf( v );
	}

	@Override
	public SEMOSSVertex getRealVertex( SEMOSSVertex v ) {
		return model.getRealVertex( v );
	}

	/**
	 * Sets the layout of the visualization. The name must be a key pointing to a
	 * a class name in the semoss.properties file. If any error occurs, the layout
	 * is clearSelected to {@link Constants#FR}. (Not all layouts can support all
	 * graph topologies)
	 *
	 * @return true if the desired layout was applied
	 * @param layout String
	 */
	@Override
	public boolean setLayoutName( String newName ) {
		String oldName = this.layoutName;
		this.layoutName = newName;

		Class<?> layoutClass = (Class<?>) DIHelper.getInstance().getLocalProp( layoutName );
		log.debug( "Create layout from layoutName " + layoutName
				+ ", and layoutClass " + layoutClass );

		DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph = getVisibleGraph();

		boolean ok = false;
		Layout<SEMOSSVertex, SEMOSSEdge> layout = null;
		try {
			Constructor<?> constructor = layoutClass.getConstructor( Forest.class );
			layout = (Layout<SEMOSSVertex, SEMOSSEdge>) constructor.newInstance( asForest() );
			ok = true;
		}
		catch ( NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
			log.warn( "no forest constructor for " + layoutName + " layout" );
		}

		if ( null == layout ) {
			try {
				Constructor<?> constructor = layoutClass.getConstructor( Graph.class );
				layout = (Layout<SEMOSSVertex, SEMOSSEdge>) constructor.newInstance( graph );
				ok = true;
			}
			catch ( NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
				log.error( "could not create layout", e );
			}
		}

		if ( null == layout ) {
			layout = new FRLayout<>( graph );
		}

		fitGraphinWindow();
		layout.initialize();
		try {
			double scale = 0.85;
			Dimension d = getView().getSize();
			d.setSize( d.getWidth() * scale, d.getHeight() * scale );
			layout.setSize( d );
		}
		catch ( UnsupportedOperationException uoe ) {
			// you can set the layout size for some layouts...but there's no way to
			// know which ones
		}

		getView().setGraphLayout( layout );

		fireLayoutUpdated( graph, oldName, layout );

		return ok;
	}
}
