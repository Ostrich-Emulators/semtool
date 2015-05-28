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

import gov.va.semoss.om.SEMOSSEdge;
import static gov.va.semoss.ui.transformer.SelectingTransformer.SelectedState.NOT_SELECTED;

/**
 * Transforms the edges of a graph so they can be highlighted.
 */
public class ArrowPaintTransformer extends SelectingTransformer<SEMOSSEdge, Paint> {

	@Override
	protected Paint transformNormal( SEMOSSEdge t ) {
		return t.getColor();
	}

	@Override
	protected Paint transformSelected( SEMOSSEdge t ) {
		return t.getColor();
	}

	@Override
	protected Paint transformNotSelected( SEMOSSEdge t, boolean skel ) {
		return ( skel ? Color.white : t.getColor() );
	}
}
