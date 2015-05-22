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

import gov.va.semoss.om.AbstractNodeEdgeBase;
import java.awt.Color;
import java.awt.Paint;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.collections15.Transformer;

/**
 * Transforms the color of vertices/nodes on the graph.
 */
public class PaintTransformer<T extends AbstractNodeEdgeBase> implements Transformer<T, Paint> {

	private Set<T> selecteds = new HashSet<>();

	public void reset() {
		selecteds.clear();
	}

	public void setSelected( Collection<T> s ) {
		selecteds.clear();
		select( s );
	}

	public void select( T s ) {
		selecteds.add( s );
	}

	public void select( Collection<T> s ) {
		if( null != s ){
			selecteds.addAll( s );
		}
	}

	public Set<T> getSelected() {
		return selecteds;
	}

	/**
	 * Method transform. Get the DI Helper to find what is needed to get for
	 * vertex
	 *
	 * @param arg0 DBCMVertex - The edge of which this returns the properties.
	 *
	 * @return Paint - The type of Paint.
	 */
	@Override
	public Paint transform( T vertex ) {
		if ( !( selecteds.isEmpty() || selecteds.contains( vertex ) ) ) {
			return Color.white;
		}

		return vertex.getColor();
	}
}
