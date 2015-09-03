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

import org.apache.log4j.Logger;

import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.ui.helpers.DynamicColorRepository;
import gov.va.semoss.ui.main.Starter;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.Utility;
import gov.va.semoss.om.GraphColorRepository;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * This class sets the visualization viewer for a popup menu.
 */
public class ColorPopup extends JMenu {

	private static final long serialVersionUID = -4784260297860900414L;
	private static final Logger log = Logger.getLogger( Utility.class );


	public ColorPopup( GraphPlaySheet gps, Collection<SEMOSSVertex> vertices ) {
		super( "Modify Color" );

		DynamicColorRepository gcr = DynamicColorRepository.instance();
		for( Map.Entry<String, Color> en : gcr.getNamedColorMap().entrySet() ){
			JMenuItem menuItem = new JMenuItem( en.getKey() );
			menuItem.addActionListener( new AbstractAction() {
				private static final long serialVersionUID = -8338447465448152673L;

				@Override
				public void actionPerformed( ActionEvent e ) {
					for( SEMOSSVertex v : vertices ){
						
						v.setColor( en.getValue() );
					
						try {
							Properties props = DIHelper.getInstance().getCoreProp();
							props.setProperty(v.getType().getLocalName()+"_COLOR", en.getKey());
							gcr.updateColor(v.getType(), en.getValue());
							java.net.URL url = Starter.class.getResource("/semoss.properties");
							java.io.File pout = new java.io.File(url.toURI());
					        java.io.OutputStream out;
							out = new java.io.FileOutputStream( pout );
							props.store(out, "This is an optional header comment string");
					        out.close();
							} catch (Exception ex) {
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
