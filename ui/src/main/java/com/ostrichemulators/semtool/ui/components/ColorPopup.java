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
package com.ostrichemulators.semtool.ui.components;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.log4j.Logger;

import com.ostrichemulators.semtool.ui.components.playsheets.GraphPlaySheet;
import com.ostrichemulators.semtool.util.Utility;
import com.ostrichemulators.semtool.om.GraphColorRepository;
import com.ostrichemulators.semtool.om.GraphElement;
import com.ostrichemulators.semtool.user.LocalUserImpl;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Map;

/**
 * This class sets the visualization viewer for a popup menu.
 */
public class ColorPopup extends JMenu {

	private static final long serialVersionUID = -4784260297860900414L;
	private static final Logger log = Logger.getLogger( Utility.class );

	public ColorPopup( GraphPlaySheet gps, Collection<? extends GraphElement> vertices ) {
		super( "Modify Color" );

		GraphColorRepository gcr = GraphColorRepository.instance();
		for ( Map.Entry<String, Color> en : gcr.getNamedColorMap().entrySet() ) {
			JMenuItem menuItem = new JMenuItem( en.getKey() );
			menuItem.addActionListener( new AbstractAction() {
				private static final long serialVersionUID = -8338447465448152673L;

				@Override
				public void actionPerformed( ActionEvent e ) {
					for ( GraphElement v : vertices ) {

						v.setColor( en.getValue() );

						try {
							gcr.updateColor( v.getType(), en.getValue() );
							LocalUserImpl.getLocalUser().setProperty( v.getType().getLocalName() + "_COLOR", en.getKey() );
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
