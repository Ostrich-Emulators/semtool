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

import com.ostrichemulators.semtool.om.SEMOSSVertex;
import java.awt.BasicStroke;
import java.awt.Stroke;

/**
 */
public class VertexStrokeTransformer<T extends SEMOSSVertex>
		extends SelectingTransformer<T, Stroke> {

	private final Stroke selected = new BasicStroke( 1f );
	private final Stroke normal = new BasicStroke( 0f );

	@Override
	protected Stroke transformNormal( SEMOSSVertex t ) {
		return normal;
	}

	@Override
	protected Stroke transformSelected( SEMOSSVertex t ) {
		return selected;
	}

	@Override
	protected Stroke transformNotSelected( SEMOSSVertex t, boolean skel ) {
		return normal;
	}
}
