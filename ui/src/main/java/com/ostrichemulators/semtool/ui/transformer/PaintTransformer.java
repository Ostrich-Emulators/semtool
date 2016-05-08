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
package com.ostrichemulators.semtool.ui.transformer;

import com.ostrichemulators.semtool.om.GraphColorShapeRepository;
import com.ostrichemulators.semtool.om.GraphElement;
import java.awt.Color;
import java.awt.Paint;

/**
 * Transforms the color of vertices/nodes on the graph.
 */
public class PaintTransformer<T extends GraphElement> extends SelectingTransformer<T, Paint> {

	private GraphColorShapeRepository repo;

	public void setColorShapeRepository( GraphColorShapeRepository repo ) {
		this.repo = repo;
	}

	@Override
	protected Paint transformNormal( T t ) {
		return ( t.isNode() ? repo.getColor( t ) : Color.GRAY );
	}

	@Override
	protected Paint transformSelected( T t ) {
		return transformNormal( t );
	}

	@Override
	protected Paint transformNotSelected( T t, boolean skel ) {
		return ( skel ? Color.white : transformNormal( t ) );
	}
}
