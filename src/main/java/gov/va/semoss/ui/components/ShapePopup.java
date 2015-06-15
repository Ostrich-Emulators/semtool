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
import gov.va.semoss.ui.helpers.TypeColorShapeTable;

import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * This class is used to display information about shapes in a popup menu.
 */
public class ShapePopup extends JMenu {
	private static final long serialVersionUID = 3874311709020126729L;
	
	public ShapePopup( String _name, GraphPlaySheet gps, Collection<SEMOSSVertex> vertices ) {
		super( _name );

		for ( String shape : TypeColorShapeTable.getAllShapes() ) {
			JMenuItem menuItem = new JMenuItem(shape);
			menuItem.addActionListener(new AbstractAction() {
				private static final long serialVersionUID = -8338448713648152673L;
				@Override
				public void actionPerformed(ActionEvent e) {
					gps.setShapes(vertices, shape);
				}
			});
			add(menuItem);
		}
	}
}