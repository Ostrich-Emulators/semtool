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
package gov.va.semoss.ui.components;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.ui.helpers.TypeColorShapeTable;

import java.awt.event.ActionEvent;
import java.util.Collection;

/**
 * This class sets the visualization viewer for a popup menu.
 */
public class ColorPopup extends JMenu {

	private static final long serialVersionUID = -4784260297860900414L;

	public ColorPopup( String _name, GraphPlaySheet gps, Collection<SEMOSSVertex> vertices ) {
		super( _name );

		for ( String color : TypeColorShapeTable.getAllColors() ) {
			JMenuItem menuItem = new JMenuItem( color );
			menuItem.addActionListener( new AbstractAction() {
				private static final long serialVersionUID = -8338447465448152673L;

				@Override
				public void actionPerformed( ActionEvent e ) {
					gps.setColors( vertices, color );
				}
			} );
			add( menuItem );
		}
	}
}
