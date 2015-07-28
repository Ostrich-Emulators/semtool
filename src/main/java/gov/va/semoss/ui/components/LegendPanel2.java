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

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.ui.components.api.GraphListener;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.ui.helpers.GraphShapeRepository;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.MultiMap;
import gov.va.semoss.util.Utility;

import java.awt.Color;
import java.awt.Shape;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.JPanel;

import org.openrdf.model.URI;

/**
 * This class is used to create the legend for visualizations.
 */
public class LegendPanel2 extends JPanel implements GraphListener {

	private static final long serialVersionUID = -2364666196260002413L;

	/**
	 * Create the panel.
	 */
	public LegendPanel2() {
		setLayout( new WrapLayout( WrapLayout.LEFT, 15, 15 ) );
		setToolTipText( "You can adjust the shape and color by going to the cosmetics tab on the navigation panel" );
	}

	@Override
	public void graphUpdated( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph, GraphPlaySheet gps ) {
		MultiMap<URI, SEMOSSVertex> types = new MultiMap<>();
		Map<URI, Shape> shapes = new HashMap<>();
		Map<URI, Color> colors = new HashMap<>();
		for ( SEMOSSVertex v : gps.getVisibleGraph().getVertices() ) {
			types.add( v.getType(), v );
			shapes.put( v.getType(), GraphShapeRepository.instance().getLegendShape( v.getShape() ) );
			colors.put( v.getType(), v.getColor() );
		}

		IEngine eng = DIHelper.getInstance().getRdfEngine();
		Map<URI, String> labels = Utility.getInstanceLabels( shapes.keySet(), eng );

		removeAll();

		for ( Map.Entry<URI, List<SEMOSSVertex>> en : types.entrySet() ) {
			String label = labels.get( en.getKey() );

			MultiMap<ShapeColorHelper, SEMOSSVertex> mm = new MultiMap<>();
			for ( SEMOSSVertex v : en.getValue() ) {
				mm.add( new ShapeColorHelper( GraphShapeRepository.instance().getLegendShape( v.getShape() ),
						v.getColor() ), v );
			}

			for ( Map.Entry<ShapeColorHelper, List<SEMOSSVertex>> sch : mm.entrySet() ) {
				String text = label + " (" + sch.getValue().size() + ")";
				add( new PaintLabel( text, sch.getKey().shape, sch.getKey().color ) );
			}
		}

		updateUI();
		repaint();
	}

	@Override
	public void layoutChanged( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
			String oldlayout, Layout<SEMOSSVertex, SEMOSSEdge> newlayout ) {
		// nothing to update in this case
	}

	private class ShapeColorHelper {

		public final Shape shape;
		public final Color color;

		public ShapeColorHelper( Shape s, Color c ) {
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
