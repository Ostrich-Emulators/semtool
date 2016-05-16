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

import edu.uci.ics.jung.graph.Forest;
import com.ostrichemulators.semtool.om.SEMOSSVertex;

import com.ostrichemulators.semtool.ui.components.playsheets.GraphPlaySheet;
import com.ostrichemulators.semtool.ui.components.playsheets.TreeGraphPlaySheet;
import edu.uci.ics.jung.algorithms.layout.Layout;
import java.awt.event.ActionEvent;
import java.util.Collection;
import javax.swing.AbstractAction;
import org.apache.log4j.Logger;

/**
 * This class is used to configure menu items in the layout.
 */
public class LayoutMenuItem extends AbstractAction {

	private static final long serialVersionUID = 3303474056533355632L;

	private final GraphPlaySheet gps;
	private final Class<? extends Layout> layout;
	private final Collection<SEMOSSVertex> verts;

	/**
	 * Constructor for LayoutMenuItem.
	 *
	 * @param layout String
	 * @param ps IPlaySheet
	 * @param verts
	 */
	public LayoutMenuItem( Class<? extends Layout> layout, GraphPlaySheet ps,
			Collection<SEMOSSVertex> verts ) {
		super( layout.getSimpleName() );
		this.gps = ps;
		this.layout = layout;
		this.verts = verts;
	}

	@Override
	public void actionPerformed( ActionEvent ae ) {
		boolean done = false;
		if ( !( gps.getGraphData().getGraph() instanceof Forest ) ) {
			// if we're not already a tree, but the user selected a tree layout
			// *AND* at least one tree root, then convert to a tree before
			// processing the layout

			if ( LayoutPopup.TREELAYOUTS.contains( layout ) && !verts.isEmpty() ) {
				Logger.getLogger( getClass() ).debug( "automatically converting to a tree layout" );

				TreeGraphPlaySheet tgps = new TreeGraphPlaySheet( gps.getGraphData().getGraph(),
						gps.getView().getPickedVertexState().getPicked(), layout );
				tgps.setTitle( "Tree Conversion" );
				gps.addSibling( tgps );
				done = true;
			}
		}

		if ( !done ) {
			// normal, non-tree layout
			gps.getView().setGraphLayout( this.layout );
		}
	}
}
