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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;

import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.ui.helpers.TypeColorShapeTable;

/**
 * This class is used to display information about shapes in a popup menu.
 */
public class ShapePopup extends JMenu {

	private static final long serialVersionUID = 3874311709020126729L;
	private SEMOSSVertex[] pickedVertex = null;

	public ShapePopup( String _name, GraphPlaySheet gps, SEMOSSVertex[] _pickedVertex ) {
		super( _name );
		pickedVertex = _pickedVertex;

		String[] shapes = TypeColorShapeTable.getAllShapes();
		for ( String shape : shapes ) {
			ShapeMenuItem item = new ShapeMenuItem( shape, gps, pickedVertex );
			add( item );
		}
	}
}
