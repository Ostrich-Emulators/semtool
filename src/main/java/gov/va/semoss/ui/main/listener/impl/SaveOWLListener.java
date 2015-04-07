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
package gov.va.semoss.ui.main.listener.impl;

import java.awt.event.ActionEvent;

import javax.swing.JComponent;

import org.apache.log4j.Logger;

import gov.va.semoss.ui.components.api.IChakraListener;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.util.QuestionPlaySheetStore;

/**
 * Controls the saving of the OWL file.
 */
public class SaveOWLListener implements IChakraListener {

	Logger logger = Logger.getLogger(getClass());
	
	/**
	 * Method actionPerformed.  Dictates what actions to take when an Action Event is performed.
	 * @param actionevent ActionEvent - The event that triggers the actions in the method.
	 */
	@Override
	public void actionPerformed(ActionEvent actionevent) {
		logger.debug("Saving the OWL");
		GraphPlaySheet ps = (GraphPlaySheet)QuestionPlaySheetStore.getInstance().getActiveSheet();
		saveIt( ps );		
	}
	
	// temporary placeholder
	// once complete, this will be written into the prop file back
	/**
	 * Method saveIt.  Saves the configuration of the engine based on the graph play sheet.
	 * @param ps the playsheet
	 */
	public void saveIt( GraphPlaySheet ps )
	{
		// get the core properties
		ps.exportDB();
		ps.getEngine().saveConfiguration();
	}
	
	/**
	 * Method setView. Sets a JComponent that the listener will access and/or modify when an action event occurs.  
	 * @param view the component that the listener will access
	 */
	@Override
	public void setView(JComponent view) {

	}
}
