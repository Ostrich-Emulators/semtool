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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;

import gov.va.semoss.ui.components.api.IChakraListener;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.PlaySheetEnum;
import gov.va.semoss.tabbedqueries.TabbedQueries;

/**
 * Shows a hint for the custom SPARQL query upon click of the show hint button.
 */
public class ShowSparqlHintListener implements IChakraListener {

	TabbedQueries view = null;

	/**
	 * Method setModel.  Sets the model that the listener will access.
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
		TabbedQueries area = (TabbedQueries)DIHelper.getInstance().getLocalProp(Constants.SPARQL_AREA_FIELD);
		JButton btnShowHint = (JButton)DIHelper.getInstance().getLocalProp(Constants.SHOW_HINT);
		JComboBox playSheetComboBox = (JComboBox)DIHelper.getInstance().getLocalProp(Constants.PLAYSHEET_COMBOBOXLIST);
		String selectedPlaySheet = (String)playSheetComboBox.getSelectedItem();
		
			if(btnShowHint.isEnabled()){
				// playsheet starting with "*" are those that are not included in predefined
				// list in util.PlaySheetEnum
				if(selectedPlaySheet.startsWith("*"))
				{
					//Set the sparql area with no hint:
					if(area.getTabCount() == 1){
					   area.setSelectedIndex(0);
	                   //This pause allows the newly created tab to fully instantiate:
	                   try{
	                       Thread.sleep(1000);
	                   }catch(Exception ee){}
					}
					area.setTextOfSelectedTab("Hint: not available");
				}
				else{
					//Set the sparql area with the hint:
					if(area.getTabCount() == 1){
					   area.setSelectedIndex(0);
	                   //This pause allows the newly created tab to fully instantiate:
	                   try{
	                       Thread.sleep(1000);
	                   }catch(Exception ee){}
					}
					area.setTextOfSelectedTab(PlaySheetEnum.getHintFromName(selectedPlaySheet));
				}
			}
	
	}
	
	/**
	 * Method setView. Sets a JComponent that the listener will access and/or modify when an action event occurs.  
	 * @param view the component that the listener will access
	 */
	@Override
	public void setView(JComponent view) {
		this.view = (TabbedQueries)view;	
	}
}
