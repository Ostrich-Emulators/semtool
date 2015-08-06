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

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComponent;
import org.apache.log4j.Logger;

import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;

/**
 * Controls the buttons to start the training modules from the Help tab.
 */
public class StartTrainingListener extends AbstractListener {
	private static final Logger log = Logger.getLogger( StartTrainingListener.class );
	/**
	 * Method actionPerformed.  Dictates what actions to take when an Action Event is performed.
	 * @param actionevent ActionEvent - The event that triggers the actions in the method.
	 */
	@Override
	public void actionPerformed(ActionEvent actionevent) {
		String workingDir = DIHelper.getInstance().getProperty(Constants.BASE_FOLDER);
		JButton htmlButton= (JButton)DIHelper.getInstance().getLocalProp(Constants.HTML_TRAINING_BUTTON);
		JButton pptButton= (JButton)DIHelper.getInstance().getLocalProp(Constants.PPT_TRAINING_BUTTON);
		File file = null;
		if (actionevent.getSource() == htmlButton)
		{
			file = new File(workingDir+"/training/html/Level1-Training/index.html");
		}
		else if (actionevent.getSource() == pptButton)
		{
			file = new File(workingDir+"/training/powerpoint/<SomePowerPointFile>.ppsx");
		}
		
		Desktop desktop = null;
		 if (Desktop.isDesktopSupported()) {
		        desktop = Desktop.getDesktop();
		        try {
					desktop.open(file);
				} catch (IOException e) {
					log.error( e );
				}
		 }
		 
		
	}

	/**
	 * Method setView. Sets a JComponent that the listener will access and/or modify when an action event occurs.  
	 * @param view the component that the listener will access
	 */
	@Override
	public void setView(JComponent view) {

	}
}
