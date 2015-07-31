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
package gov.va.semoss.ui.main.listener.impl;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Graph;
import gov.va.semoss.om.AbstractNodeEdgeBase;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.ui.components.api.GraphListener;
import gov.va.semoss.ui.components.models.FilterRow;
import gov.va.semoss.ui.components.models.VertexFilterTableModel;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.ui.components.renderers.LabeledPairTableCellRenderer;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.Utility;

import java.awt.Component;
import java.util.Arrays;
import static javafx.scene.input.KeyCode.T;
import javax.swing.JTable;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 */
public class GraphPlaySheetListener implements InternalFrameListener, GraphListener {

	private final GraphPlaySheet ps;

	public GraphPlaySheetListener( GraphPlaySheet gps ) {
		ps = gps;
	}

	/**
	 * Method internalFrameActivated. Gets the playsheet that is being activated
	 * TODO unused method Method internalFrameActivated. Gets the playsheet that
	 * is being activated
	 *
	 * @param e InternalFrameEvent
	 */
	@Override
	public void internalFrameActivated( InternalFrameEvent e ) {
		Graph<SEMOSSVertex, SEMOSSEdge> g = ps.getGraphData().getGraph();
		Utility.addModelToJTable( new VertexFilterTableModel<>(g, g.getVertices(), 
				"Node Type" ), Constants.FILTER_TABLE );
		Utility.addModelToJTable( new VertexFilterTableModel<>( g, g.getEdges(), 
				"Edge Type" ), Constants.EDGE_TABLE );
	}

	@Override
	public void graphUpdated( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
			GraphPlaySheet gps ) {
		if ( gps != Utility.getActiveGraphPlaysheet() ) {
			return;
		}

		Graph<SEMOSSVertex, SEMOSSEdge> g = ps.getGraphData().getGraph();
		Utility.addModelToJTable( new VertexFilterTableModel<>(g, g.getVertices(), 
				"Node Type" ), Constants.FILTER_TABLE );
		Utility.addModelToJTable( new VertexFilterTableModel<>( g, g.getEdges(), 
				"Edge Type" ), Constants.EDGE_TABLE );

		JTable vtable = DIHelper.getJTable( Constants.FILTER_TABLE );
		JTable etable = DIHelper.getJTable( Constants.EDGE_TABLE );

		ShowRenderer valrend = new ShowRenderer( gps.getEngine() );
		for ( JTable tbl : Arrays.asList( vtable, etable ) ) {
			tbl.getColumnModel().getColumn( 1 ).setCellRenderer( valrend );
			tbl.getColumnModel().getColumn( 2 ).setCellRenderer( valrend );
		}
	}

	@Override
	public void layoutChanged( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
			String oldlayout, Layout<SEMOSSVertex, SEMOSSEdge> newlayout ) {
		// don't care
	}

	/**
	 * Method internalFrameClosed. TODO unused method Method internalFrameClosed.
	 *
	 * @param e InternalFrameEvent
	 */
	@Override
	public void internalFrameClosed( InternalFrameEvent e ) {
		Utility.resetJTable( Constants.FILTER_TABLE );
		Utility.resetJTable( Constants.EDGE_TABLE );
		Utility.resetJTable( Constants.PROP_TABLE );
	}

	@Override
	public void internalFrameOpened( InternalFrameEvent e ) {
	}

	@Override
	public void internalFrameClosing( InternalFrameEvent e ) {
	}

	@Override
	public void internalFrameIconified( InternalFrameEvent e ) {
	}

	@Override
	public void internalFrameDeiconified( InternalFrameEvent e ) {
	}

	@Override
	public void internalFrameDeactivated( InternalFrameEvent e ) {
	}

	private class ShowRenderer extends LabeledPairTableCellRenderer<Value> {

		private final IEngine engine;

		public ShowRenderer( IEngine engine ) {
			this.engine = engine;
		}

		@Override
		public Component getTableCellRendererComponent( JTable table, Object value,
				boolean sel, boolean foc, int r, int c ) {

			VertexFilterTableModel<? extends AbstractNodeEdgeBase> model
					= (VertexFilterTableModel<? extends AbstractNodeEdgeBase>) table.getModel();
			FilterRow<? extends AbstractNodeEdgeBase> row = model.getRawRow( r );

			if ( row.isHeader() ) {
				return super.getTableCellRendererComponent( table,
						( 1 == c ? row.type : "Set For All" ), sel, foc, r, c );
			}
			else {
				return super.getTableCellRendererComponent( table,
						( 1 == c ? null : row.instance.getURI() ), sel, foc, r, c );
			}
		}

		@Override
		protected String getLabelForCacheMiss( Value val ) {
			if ( null == val ) {
				return "";
			}

			String ret;
			if ( val instanceof URI ) {
				URI uri = URI.class.cast( val );
				ret = ( null == engine ? uri.getLocalName()
						: Utility.getInstanceLabel( Resource.class.cast( val ), engine ) );
				cache( val, ret );
			}
			else if ( val instanceof Literal ) {
				ret = Literal.class.cast( val ).getLabel();
			}
			else {
				ret = val.stringValue();
			}
			return ret;
		}
	}
}
