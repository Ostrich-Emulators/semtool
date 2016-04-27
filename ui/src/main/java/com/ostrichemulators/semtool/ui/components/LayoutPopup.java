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
package com.ostrichemulators.semtool.ui.components;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Forest;
import com.ostrichemulators.semtool.om.SEMOSSEdge;
import com.ostrichemulators.semtool.om.SEMOSSVertex;
import javax.swing.JMenu;

import com.ostrichemulators.semtool.ui.components.playsheets.GraphPlaySheet;
import com.ostrichemulators.semtool.util.Constants;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is used to create a popup that allows the user to pick the layout.
 */
public class LayoutPopup extends JMenu {

	private static final String[] layoutNames = { Constants.FR, Constants.KK,
		Constants.SPRING, Constants.ISO, Constants.CIRCLE_LAYOUT,
		Constants.TREE_LAYOUT, Constants.RADIAL_TREE_LAYOUT, Constants.BALLOON_LAYOUT };

	/**
	 * Constructor for LayoutPopup.
	 *
	 * @param name String
	 * @param ps IPlaySheet
	 */
	public LayoutPopup( String name, GraphPlaySheet ps, Collection<SEMOSSVertex> verts ) {
		super( name );

		DirectedGraph<SEMOSSVertex, SEMOSSEdge> viz = ps.getVisibleGraph();
		boolean forestok = ( viz instanceof Forest || !verts.isEmpty() );
		Set<String> treelayouts = new HashSet<>();
		treelayouts.add( Constants.TREE_LAYOUT );
		treelayouts.add( Constants.RADIAL_TREE_LAYOUT );
		treelayouts.add( Constants.BALLOON_LAYOUT );

		for ( String layoutName : layoutNames ) {
			LayoutMenuItem mi = new LayoutMenuItem( layoutName, ps, verts );
			add( mi );

			if ( treelayouts.contains( layoutName ) ) {
				mi.setEnabled( forestok );
			}
		}
	}
}
