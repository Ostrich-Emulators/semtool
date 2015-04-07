/*******************************************************************************
 * Copyright 2013 SEMOSS.ORG
 * 
 * This file is part of SEMOSS.
 * 
 * SEMOSS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * SEMOSS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SEMOSS.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package gov.va.semoss.ui.helpers;



import gov.va.semoss.ui.components.GridFilterData;
import gov.va.semoss.ui.components.GraphPlaySheetFrame;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.ui.components.playsheets.GridRAWPlaySheet;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class helps with running the remove view method for a playsheet.
 */
public class PlaysheetRemoveRunner implements Runnable{
	GraphPlaySheet playSheet = null;
	
	/**
	 * Constructor for PlaysheetRemoveRunner.
	 * @param playSheet GraphPlaySheetFrame
	 */
	public PlaysheetRemoveRunner(GraphPlaySheet _playSheet) {
		playSheet = _playSheet;
	}

	/**
	 * Method run.  Instantiates a graph play sheet on the local playsheet, and runs createRemoveGrid.
	 */
	@Override
	public void run() {
		playSheet.removeView();
		createRemoveGrid(playSheet);
	}
	
	/**
	 * Method createRemoveGrid.  Removes the edges from grid filter data.
	 * @param gPlaySheet GraphPlaySheetFrame - the playsheet to run.
	 */
	public void createRemoveGrid(GraphPlaySheet gPlaySheet) {
		// RPB: ?? the gfd is never populated ?
		
		GridFilterData gfd = new GridFilterData();
		String[] columnName = {"Edges Removed"};
		gfd.setColumnNames(columnName);		
		// JTable table = new JTable( gfd );
		
		GridRAWPlaySheet grid = new GridRAWPlaySheet();
		grid.create( new ArrayList<>(), Arrays.asList( columnName ) );		
		gPlaySheet.getPlaySheetFrame().addTab( grid );
	}
	
	/**
	 * Method setPlaySheet. Sets the local playsheet to the parameter.
	 * @param playSheet GraphPlaySheetFrame
	 */
	public void setPlaySheet(GraphPlaySheet _playSheet) {
		playSheet = _playSheet;
	}
}
