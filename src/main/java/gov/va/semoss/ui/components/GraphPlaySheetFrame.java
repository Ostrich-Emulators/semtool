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

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.ui.components.playsheets.PlaySheetCentralComponent;

import org.apache.log4j.Logger;

/**
 */
public class GraphPlaySheetFrame extends PlaySheetFrame {

	private static final long serialVersionUID = 4699492732234656487L;
	protected static final Logger log = Logger.getLogger(GraphPlaySheetFrame.class );
	private GraphPlaySheet gps;

	public GraphPlaySheetFrame( IEngine eng ) {
		super( eng );
	}

	public GraphPlaySheet getGraphComponent(){
		return gps;
	}

	@Override
	public void addTab( String title, PlaySheetCentralComponent c ) {
		if( c instanceof GraphPlaySheet  && getPlaySheets().isEmpty() ){
			// our first graph becomes our default graph
			gps = GraphPlaySheet.class.cast( c );
		}
		super.addTab( title, c );
	}
	
	@Override
	public ProgressTask getCreateTask( final String query ) {
		gps.setQuery( query );
		return super.getCreateTask( query );
	}
}
