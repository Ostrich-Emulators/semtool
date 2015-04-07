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
package gov.va.semoss.ui.main.listener.impl;

import java.awt.event.ActionEvent;

import org.apache.log4j.Logger;

import gov.va.semoss.ui.components.playsheets.BrowserTabSheet3;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * This listener implements the minimum spanning tree.
 */
public class ChartListener extends AbstractAction {
	private static final long serialVersionUID = 8704080303366828420L;
	
	private final static Logger logger = Logger.getLogger( ChartListener.class );
	private final GraphPlaySheet gps;

	public ChartListener( GraphPlaySheet _gps ) {
		super( "Create Custom Chart" );
		
		putValue( Action.SHORT_DESCRIPTION, "Invoke a screen to build a custom chart" );
		gps = _gps;
	} 

	@Override
	public void actionPerformed( ActionEvent e ) {
		logger.debug( "ChartListener adding a Charter tab." );

		BrowserTabSheet3 tab
				= new BrowserTabSheet3( "/html/RDFSemossCharts/app/index.html",
						gps );
		gps.getPlaySheetFrame().addTab( "Custom Chart", tab );
		tab.pullData();
	}
}
