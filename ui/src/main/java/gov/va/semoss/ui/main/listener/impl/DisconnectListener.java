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
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.ui.components.RepositoryList;
import gov.va.semoss.ui.components.api.IChakraListener;
import gov.va.semoss.util.DIHelper;

/**
 * Controls disconnecting from the repository.
 */
public class DisconnectListener implements IChakraListener {

	JTextField view = null;
	
	Logger logger = Logger.getLogger(getClass());
	
	
	/**
	 * Method setModel. Sets the model that the listener will access.
	 * @param model JComponent
	 */
	public void setModel(JComponent model)
	{
	}
	
	/**
	 * Method actionPerformed.  Dictates what actions to take when an Action Event is performed.
	 * @param e ActionEvent - The event that triggers the actions in the method.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		RepositoryList list = DIHelper.getInstance().getRepoList();
		// get the selected repository
		List<IEngine> repos = list.getSelectedValuesList();

    for ( IEngine engine : repos ) {
      String repoName = engine.getEngineName();
      if ( engine.isConnected() ) {
        logger.debug( "Attempting to disconnect " + repoName );
        engine.closeDB();
        logger.debug( "Successfully disconnected " + repoName );        
      }
      else {
        logger.debug( " Repository is not connected " + repoName );
      }
    }
	}

	/**
	 * Method setView. Sets a JComponent that the listener will access and/or modify when an action event occurs.  
	 * @param view the component that the listener will access
	 */
	@Override
	public void setView(JComponent view) {
		this.view = (JTextField)view;		
	}

}
