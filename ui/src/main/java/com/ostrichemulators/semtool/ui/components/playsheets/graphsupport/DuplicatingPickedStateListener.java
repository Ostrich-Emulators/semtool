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
package com.ostrichemulators.semtool.ui.components.playsheets.graphsupport;

import edu.uci.ics.jung.visualization.RenderContext;
import java.awt.event.ItemEvent;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import com.ostrichemulators.semtool.om.SEMOSSEdge;
import com.ostrichemulators.semtool.om.SEMOSSVertex;
import com.ostrichemulators.semtool.ui.components.playsheets.TreeGraphPlaySheet;
import com.ostrichemulators.semtool.ui.transformer.LabelFontTransformer;
import com.ostrichemulators.semtool.ui.transformer.PaintTransformer;
import com.ostrichemulators.semtool.ui.transformer.VertexShapeTransformer;
import java.awt.event.ItemListener;
import java.util.Arrays;

/**
 * Controls what happens when a picked state occurs.
 */
public class DuplicatingPickedStateListener implements ItemListener {

	private static final Logger logger = Logger.getLogger( DuplicatingPickedStateListener.class );
	private final VisualizationViewer<SEMOSSVertex, SEMOSSEdge> viewer;
	private final TreeGraphPlaySheet tps;

	public DuplicatingPickedStateListener( VisualizationViewer<SEMOSSVertex, SEMOSSEdge> v,
			TreeGraphPlaySheet ps ) {
		viewer = v;
		tps = ps;
	}

	/**
	 * Method itemStateChanged. Changes the state of the graph play sheet.
	 *
	 * @param e ItemEvent
	 */
	@Override
	public void itemStateChanged( ItemEvent e ) {
		RenderContext<SEMOSSVertex, SEMOSSEdge> rc = viewer.getRenderContext();

		//need to check if there are any size resets that need to be done
		VertexShapeTransformer vst = (VertexShapeTransformer) rc.getVertexShapeTransformer();

		// increase/decrease the size of nodes as they get selected/unselected
		if ( e.getItem() instanceof SEMOSSVertex ) {
			SEMOSSVertex v = SEMOSSVertex.class.cast( e.getItem() );

			logger.debug( ( ItemEvent.DESELECTED == e.getStateChange() ? "Deselecting"
					: "Selecting" ) + " node: " + v );

			double delta = ( ItemEvent.DESELECTED == e.getStateChange()
					? -VertexShapeTransformer.STEPSIZE : VertexShapeTransformer.STEPSIZE );
			vst.changeSize( delta, Arrays.asList( v ) );
		}

		//Need vertex to highlight when click in skeleton mode... Here we need to 
		// get the already selected vertices so that we can add to them
		Set<SEMOSSVertex> selectedVertices = new HashSet<>();
		LabelFontTransformer<SEMOSSVertex> vlft = null;

		if ( tps.getSearchPanel().isHighlightButtonSelected() ) {
			vlft = (LabelFontTransformer<SEMOSSVertex>) rc.getVertexFontTransformer();
			selectedVertices.addAll( vlft.getSelected() );
		}

		selectedVertices.addAll( viewer.getPickedVertexState().getPicked() );

		selectedVertices = expandSelection( selectedVertices );
		for ( SEMOSSVertex v : selectedVertices ) {
			viewer.getPickedVertexState().pick( v, true );
		}

		if ( null != vlft ) {
			vlft.setSelected( selectedVertices );
			PaintTransformer<SEMOSSVertex> ptx
					= (PaintTransformer<SEMOSSVertex>) viewer.getRenderContext().
					getVertexFillPaintTransformer();
			ptx.setSelected( selectedVertices );
		}
	}

	private Set<SEMOSSVertex> expandSelection( Set<SEMOSSVertex> selected ) {
		// make sure we pick all the duplicates of what's selected
		Set<SEMOSSVertex> newselection = new HashSet<>( selected );
		for ( SEMOSSVertex v : selected ) {
			newselection.addAll( tps.getDuplicates( v ) );
		}

		return newselection;
	}
}