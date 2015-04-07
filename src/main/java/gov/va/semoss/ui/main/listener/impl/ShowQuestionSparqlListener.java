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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.openrdf.model.URI;

import gov.va.semoss.om.Insight;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.ui.components.ParamComboBox;
import gov.va.semoss.ui.components.api.IChakraListener;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.PlaySheetEnum;
import gov.va.semoss.util.Utility;
import gov.va.semoss.tabbedqueries.TabbedQueries;

/**
 * If the Copy-Down button is clickd, shows the current SPARQL query
 * for the selected question:
 */
public class ShowQuestionSparqlListener implements IChakraListener {

	TabbedQueries view = null;

	/**
	 * Method setModel.  Sets the model that the query will access.
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
		JButton btnGetQuestionSparql = (JButton)DIHelper.getInstance().getLocalProp(Constants.GET_CURRENT_SPARQL);
		TabbedQueries area = (TabbedQueries) DIHelper.getInstance().getLocalProp(Constants.SPARQL_AREA_FIELD);
		// get the selected engine
		IEngine engine = (IEngine) DIHelper.getInstance().getRdfEngine();
		// get the selected question's insight:
        JComboBox<Insight> cmb = (JComboBox<Insight>) DIHelper.getInstance().getLocalProp( Constants.QUESTION_LIST_FIELD );
        Insight insight = cmb.getItemAt( cmb.getSelectedIndex() );
		//Get sparql text from the insight:
		String sparql = Utility.normalizeParam(insight.getSparql());
		// when get current sparql question btn is pressed
        if ( btnGetQuestionSparql.isEnabled() ) {
		   // code is taken from QuestionListener performs param filling so that the query that is inputed is complete

           // populate query to display based on parameters 
           JPanel panel = (JPanel) DIHelper.getInstance().getLocalProp( Constants.PARAM_PANEL_FIELD );
           DIHelper.getInstance().setLocalProperty( Constants.UNDO_BOOLEAN, false );
           // get the currently visible panel
           Component[] comps = panel.getComponents();
           JComponent curPanel = null;
           for ( int compIndex = 0; compIndex < comps.length
               && curPanel == null; compIndex++ ) {
             if ( comps[compIndex].isVisible() ) {
               curPanel = (JComponent) comps[compIndex];
             }
           }

           // get all the param field
           Component[] fields = curPanel.getComponents();
           Map<String, String> paramHash = new HashMap<>();

           for ( Component field : fields ) {
             if ( field instanceof ParamComboBox ) {
                String fieldName = ( (ParamComboBox) field ).getParamName();
                String fieldValue = ( (ParamComboBox) field ).getSelectedItem() + "";
                String uriFill = ( (ParamComboBox) field ).getURI( fieldValue );
                if ( uriFill == null ) {
                   uriFill = fieldValue;
                }
                paramHash.put( fieldName, uriFill );
             }
           }
           sparql = gov.va.semoss.util.Utility.fillParam( sparql, paramHash );

           //Set the sparql area with the query:
           if ( area.getTabCount() == 1 ) {
              area.setSelectedIndex( 0 );
           }
           area.setTextOfSelectedTab( sparql );

           //Change the playsheet selected to the layout of the imported question query:
           //---------------------------------------------------------------------------
           String layoutValue = insight.getOutput();
           JComboBox playSheetComboBox = (JComboBox) DIHelper.getInstance().getLocalProp( Constants.PLAYSHEET_COMBOBOXLIST );
           //Set the model each time a question is choosen to include playsheets that are not in PlaySheetEnum.
           //(This step is really moot, because "QuestionListener.java" will disable the Copy-Down button
           // for these cases.)
           playSheetComboBox.setModel( new DefaultComboBoxModel( PlaySheetEnum.getAllSheetNames().toArray() ) );
           if ( !PlaySheetEnum.getAllSheetClasses().contains( layoutValue ) ) {
              String addPlaySheet = layoutValue.substring( layoutValue.lastIndexOf( "." ) + 1 );
              playSheetComboBox.addItem("?"+addPlaySheet );
              playSheetComboBox.setSelectedItem("?"+addPlaySheet );
           }
           else {
              playSheetComboBox.setSelectedItem( PlaySheetEnum.getNameFromClass( layoutValue ) );
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
