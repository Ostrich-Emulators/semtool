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
package com.ostrichemulators.semtool.ui.components.playsheets.graphsupport;

import com.ostrichemulators.semtool.om.SEMOSSVertex;
import com.ostrichemulators.semtool.ui.components.playsheets.GraphPlaySheet;
import com.ostrichemulators.semtool.ui.helpers.GraphShapeRepository;
import com.ostrichemulators.semtool.ui.helpers.GraphShapeRepository.Shapes;
import com.ostrichemulators.semtool.util.Utility;

import java.awt.event.ActionEvent;
import java.util.Collection;


import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.log4j.Logger;

/**
 * This class is used to display information about shapes in a popup menu.
 */
public class ShapePopup extends JMenu {

	private static final long serialVersionUID = 3874311709020126729L;
	private static final Logger log = Logger.getLogger( Utility.class );

	public ShapePopup( GraphPlaySheet gps, Collection<SEMOSSVertex> vertices ) {
		super( "Modify Shape" );

		GraphShapeRepository repo = gps.getShapeRepository();

		for ( Shapes en : GraphShapeRepository.Shapes.values() ) {
			JMenuItem menuItem = new JMenuItem( repo.createIcon( en ) );
			menuItem.addActionListener( new AbstractAction() {
				private static final long serialVersionUID = -8338448713648152673L;

				@Override
				public void actionPerformed( ActionEvent e ) {
					for ( SEMOSSVertex v : vertices ) {
						v.setShape( repo.getShape( en ) );
						
						try {
							repo.setShape( v.getURI(), v.getShape() );
						}
						catch ( Exception ex ) {
							// TODO Auto-generated catch block
							log.error( ex, ex );
						}

					}
				}
			} );
			add( menuItem );
		}
	}
}
