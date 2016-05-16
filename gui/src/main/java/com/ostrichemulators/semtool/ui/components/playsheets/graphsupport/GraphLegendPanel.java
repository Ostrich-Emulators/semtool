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

import com.ostrichemulators.semtool.om.GraphColorShapeRepository;
import com.ostrichemulators.semtool.om.NamedShape;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import com.ostrichemulators.semtool.om.SEMOSSEdge;
import com.ostrichemulators.semtool.om.SEMOSSVertex;
import com.ostrichemulators.semtool.ui.components.WrapLayout;
import com.ostrichemulators.semtool.ui.components.api.GraphListener;
import com.ostrichemulators.semtool.ui.components.playsheets.GraphPlaySheet;
import com.ostrichemulators.semtool.util.MultiMap;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;

/**
 * This class is used to create the legend for visualizations.
 */
public final class GraphLegendPanel extends JPanel implements GraphListener {

	private static final long serialVersionUID = -2364666196260002413L;
	private Map<Value, String> labels = new HashMap<>();

	/**
	 * Create the panel.
	 *
	 * @param labels
	 */
	public GraphLegendPanel( Map<Value, String> labels ) {
		setLayout( new WrapLayout( WrapLayout.LEFT, 5, 5 ) );
		setBorder( BorderFactory.createLineBorder( Color.LIGHT_GRAY, 1 ) );
		this.labels = labels;
	}

	@Override
	public void graphUpdated( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph, GraphPlaySheet gps ) {
		final SemossGraphVisualization view = gps.getView();

		MultiMap<URI, SEMOSSVertex> types = new MultiMap<>();
		Map<URI, NamedShape> shapes = new HashMap<>();
		Map<URI, Color> colors = new HashMap<>();
		GraphColorShapeRepository repo = gps.getShapeRepository();
		Collection<SEMOSSVertex> vs = gps.getVisibleGraph().getVertices();
		for ( SEMOSSVertex v : vs ) {
			URI type = new URIImpl( v.getType().stringValue() );
			types.add( type, v );
			shapes.put( type, repo.getShape( v ) );
			colors.put( type, repo.getColor( v ) );
		}

		removeAll();

		for ( Map.Entry<URI, List<SEMOSSVertex>> en : types.entrySet() ) {
			String label = labels.get( en.getKey() );

			MultiMap<ShapeColorHelper, SEMOSSVertex> mm = new MultiMap<>();
			for ( SEMOSSVertex v : en.getValue() ) {
				mm.add( new ShapeColorHelper( repo.getShape( v ), repo.getColor( v ) ), v );
			}

			for ( Map.Entry<ShapeColorHelper, List<SEMOSSVertex>> sch : mm.entrySet() ) {
				String text = label + " (" + sch.getValue().size() + ")";
				PaintLabel pl = new PaintLabel( text, sch.getKey().shape, sch.getKey().color );

				if ( 1 == sch.getValue().size() ) {
					pl.setToolTipText( sch.getValue().get( 0 ).getLabel() );
				}

				add( pl );
				pl.addMouseListener( new MouseAdapter() {

					@Override
					public void mouseClicked( MouseEvent e ) {
						super.mouseClicked( e );
						if ( !SwingUtilities.isRightMouseButton( e ) ) {
							List<SEMOSSVertex> selVs = sch.getValue();

							view.clearHighlighting();
							view.highlight( selVs, null );
						}
					}

					@Override
					public void mousePressed( MouseEvent e ) {
						if ( SwingUtilities.isRightMouseButton( e ) ) {
							e.consume();

							JPopupMenu menu = new JPopupMenu();
							menu.add( new ColorPopup( gps, sch.getValue() ) );
							menu.add( new ShapePopup( gps, sch.getValue() ) );
							menu.show( pl, e.getX(), e.getY() );
						}
					}
				} );
			}
		}

		updateUI();
		repaint();
	}

	@Override
	public void layoutChanged( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
			String oldlayout, Layout<SEMOSSVertex, SEMOSSEdge> newlayout,
			GraphPlaySheet gps ) {
		// nothing to update in this case
	}

	private class ShapeColorHelper {

		public final NamedShape shape;
		public final Color color;

		public ShapeColorHelper( NamedShape s, Color c ) {
			shape = s;
			color = c;
		}

		@Override
		public int hashCode() {
			int hash = 5;
			hash = 13 * hash + Objects.hashCode( this.shape );
			hash = 13 * hash + Objects.hashCode( this.color );
			return hash;
		}

		@Override
		public boolean equals( Object obj ) {
			if ( obj == null ) {
				return false;
			}
			if ( getClass() != obj.getClass() ) {
				return false;
			}
			final ShapeColorHelper other = (ShapeColorHelper) obj;
			if ( !Objects.equals( this.shape, other.shape ) ) {
				return false;
			}
			if ( !Objects.equals( this.color, other.color ) ) {
				return false;
			}
			return true;
		}

	}
}
