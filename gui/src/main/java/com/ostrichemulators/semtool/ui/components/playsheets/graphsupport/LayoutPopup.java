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

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Forest;
import com.ostrichemulators.semtool.om.SEMOSSEdge;
import com.ostrichemulators.semtool.om.SEMOSSVertex;
import javax.swing.JMenu;

import com.ostrichemulators.semtool.ui.components.playsheets.GraphPlaySheet;
import edu.uci.ics.jung.algorithms.layout.BalloonLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.RadialTreeLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is used to create a popup that allows the user to pick the layout.
 */
public class LayoutPopup extends JMenu {

	private static final List<Class<? extends Layout>> LAYOUTS = Arrays.asList(
			FRLayout.class,
			KKLayout.class,
			SpringLayout.class,
			ISOMLayout.class,
			CircleLayout.class,
			TreeLayout.class,
			RadialTreeLayout.class,
			BalloonLayout.class );

	public static final Set<Class<? extends Layout>> TREELAYOUTS
			= new HashSet<>( Arrays.asList(
							TreeLayout.class,
							RadialTreeLayout.class,
							BalloonLayout.class ) );

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

		for ( Class<? extends Layout> layout : LAYOUTS ) {
			LayoutMenuItem mi = new LayoutMenuItem( layout, ps, verts );
			add( mi );

			if ( TREELAYOUTS.contains( layout ) ) {
				mi.setEnabled( forestok );
			}
		}
	}
}
