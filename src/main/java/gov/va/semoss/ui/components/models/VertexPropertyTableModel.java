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
package gov.va.semoss.ui.components.models;

import edu.uci.ics.jung.graph.Graph;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;

import gov.va.semoss.util.Constants;


import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * This class is used to create a table model for vertex properties.
 */
public class VertexPropertyTableModel extends NodeEdgePropertyTableModel<SEMOSSVertex> {

	public VertexPropertyTableModel( SEMOSSVertex vertex,
			Graph<SEMOSSVertex, SEMOSSEdge> graph ) {
		super( vertex, graph );
		
		addRow( new PropertyRow( Constants.IN_EDGE_CNT,
				new LiteralImpl( Integer.toString( graph.getInEdges( vertex ).size() ),
						XMLSchema.INT ), true ) );
		addRow( new PropertyRow( Constants.OUT_EDGE_CNT,
				new LiteralImpl( Integer.toString( graph.getOutEdges( vertex ).size() ),
						XMLSchema.INT ), true ) );
	}
}
