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
 *****************************************************************************
 */
package gov.va.semoss.ui.components;

import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.util.DIHelper;
import java.awt.event.ActionEvent;
import java.util.Collection;
import javax.swing.AbstractAction;

/**
 * This is the abstract base class that executes specific queries and paints the
 * item menu.
 */
public class ColorMenuItem extends AbstractAction {

	private static final long serialVersionUID = -5260297432865485162L;
	private final Collection<SEMOSSVertex> pickedVertex;
	private final String color;
	private final GraphPlaySheet gps;

	public ColorMenuItem( String _color, GraphPlaySheet ps, Collection<SEMOSSVertex> _pickedVertex ) {
		super( _color );
		color = _color;
		pickedVertex = _pickedVertex;
		gps = ps;
	}

	@Override
	public void actionPerformed( ActionEvent ae ) {
		for ( SEMOSSVertex vert : pickedVertex ) {
			vert.setColor( DIHelper.getColor( color ) );
			vert.setColorString( color );
		}

		gps.getView().repaint();
	}
}
