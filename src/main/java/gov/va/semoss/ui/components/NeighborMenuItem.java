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

import java.util.List;

import org.apache.log4j.Logger;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.ui.helpers.PlaysheetOverlayRunner;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.QuestionPlaySheetStore;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * This class is used to create a menu item for the neighborhood.
 */
public class NeighborMenuItem extends AbstractAction {

	String query;
	IEngine engine = null;
	String predicateURI = null;
	String name = null;
	private final GraphPlaySheet gps;

	private static final Logger logger = Logger.getLogger( NeighborMenuItem.class );

	public NeighborMenuItem( String name, GraphPlaySheet ps, String query, IEngine engine ) {
		super( name );
		this.name = name;
		this.query = query;
		this.engine = engine;
		this.gps = ps;
	}

	@Override
	public void actionPerformed( ActionEvent ae ) {
		logger.warn( "this class must be refactored" );
		// Here I need to get the active sheet
		// get everything with respect the selected node type
		// and then create the filter on top of the query
		// use the @filter@ to get this done / some of the 			

		// need to create playsheet extend runner
		Runnable playRunner = new PlaysheetOverlayRunner( gps );
		gps.setQuery( query );
		Thread playThread = new Thread( playRunner );
		playThread.start();
	}
}
