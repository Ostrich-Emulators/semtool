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
package gov.va.semoss.ui.transformer;

import java.awt.Color;
import java.awt.Paint;

import org.apache.commons.collections15.Transformer;

import gov.va.semoss.om.SEMOSSEdge;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Transforms the edges of a graph so they can be highlighted.
 */
public class ArrowDrawPaintTransformer implements Transformer<SEMOSSEdge, Paint> {

	Set<SEMOSSEdge> edges = new HashSet<>();

	/**
	 * Constructor for ArrowDrawPaintTransformer.
	 */
	public ArrowDrawPaintTransformer() {

	}

	/**
	 * Method setEdges. Sets the hashtable of edges.
	 *
	 * @param edges Hashtable<String,DBCMEdge>
	 */
	public void setEdges( Collection<SEMOSSEdge> eds ) {
		edges.clear();
		if( null != eds ){
			edges.addAll( eds );
		}
	}

	/**
	 * Method transform. If the hashtable of edges contains the edge parameter,
	 * paint it black.
	 *
	 * @param edge DBCMEdge
	 *
	 * @return Paint - the color of the paint.
	 */
	@Override
	public Paint transform( SEMOSSEdge edge ) {
		return ( edges.contains( edge ) ? edge.getColor() : Color.white );
	}
}
