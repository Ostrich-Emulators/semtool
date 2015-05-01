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
 * Controls the customize SPARQL query button.
 */
public class CustomizeSparqlBtnListener implements IChakraListener {

	TabbedQueries view = null;

	/**
	 * Method setModel.  Sets the model that the listener will access.
	 * @param model JComponent
	 */
	public void setModel(JComponent model) {
	}

	/**
	 * Method actionPerformed.  Dictates what actions to take when an Action Event is performed.
	 * @param e ActionEvent - The event that triggers the actions in the method.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		throw new UnsupportedOperationException( "this method has been completed refactored" );
		
		
//		TabbedQueries area = (TabbedQueries) DIHelper.getInstance().getLocalProp(Constants.SPARQL_AREA_FIELD);
//
//		JComboBox playSheetComboBox = (JComboBox) DIHelper.getInstance()
//				.getLocalProp(Constants.PLAYSHEET_COMBOBOXLIST);
//		JButton btnShowHint = (JButton) DIHelper.getInstance().getLocalProp(
//				Constants.SHOW_HINT);
//		JButton btnGetQuestionSparql = (JButton) DIHelper.getInstance()
//				.getLocalProp(Constants.GET_CURRENT_SPARQL);
//		String selectedPlaySheet = (String) playSheetComboBox.getSelectedItem();
//		
//			//Necessary, because this listener serves the "Custom" Sparqle button,
//			//as well as the "PlaySheet" combo-box. So, it may be called when the
//			//only action taken was a change in that combo-box--in which case,
//			//TabbedQueries should be left alone:
//			if(area.isEnabled() == false){
//			   area.enableTabbedQueries();
//			}
//			btnShowHint.setEnabled(true);
//			playSheetComboBox.setEnabled(true);
//			btnGetQuestionSparql.setEnabled(true);
//
//		if (playSheetComboBox.isEnabled()) {
//			if(area.getSelectedIndex() >= 0){
//			   // playsheet starting with "*" are those that are not included in predefined
//			   // list in util.PlaySheetEnum:
//			   if (selectedPlaySheet.startsWith("*") && area.getTextOfSelectedTab().isEmpty()){
//			   	   area.setTextOfSelectedTab("Hint: not available");
//			   } 
//			   // if sparql area is empty and user switches to a different playsheet
//			   else if (area.getTextOfSelectedTab().isEmpty()){
//				   //Set text with current playsheet hint:
//				   area.setTextOfSelectedTab(PlaySheetEnum.getHintFromName(selectedPlaySheet));
//			   } 
//			   //If sparql area currently has a hint...
//			   else if (area.getTextOfSelectedTab().startsWith("Hint:")){
//				   //Set text with current playsheet hint:
//				   area.setTextOfSelectedTab(PlaySheetEnum.getHintFromName(selectedPlaySheet));
//			   }
//			}
//		}		
	}

	/**
	 * Method setView. Sets a JComponent that the listener will access and/or modify when an action event occurs.  
	 * @param view the component that the listener will access
	 */
	@Override
	public void setView(JComponent view) {
		this.view = (TabbedQueries) view;
	}
}
