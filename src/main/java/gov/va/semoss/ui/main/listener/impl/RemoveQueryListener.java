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
package gov.va.semoss.ui.main.listener.impl;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;
import gov.va.semoss.rdf.engine.api.IEngine;

import gov.va.semoss.ui.components.ParamComboBox;
import gov.va.semoss.ui.components.RepositoryList;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.QuestionPlaySheetStore;
import gov.va.semoss.util.Utility;

/**
 * 1. Get information from the textarea for the query 2. Process the query to
 * Remove the graph 3. Create a playsheet and fill it with the respective
 * information 4. Set all the controls reference within the PlaySheet
 *
 */
public class RemoveQueryListener extends SparqlAreaListener {

	private static final Logger logger = Logger.getLogger( RemoveQueryListener.class );

	// where all the parameters are set
	// this will implement a cardlayout and then on top of that the param panel
	JPanel paramPanel = null;

	// right hand side panel
	JComponent rightPanel = null;

	/**
	 * Method actionPerformed. Dictates what actions to take when an Action Event
	 * is performed.
	 *
	 * @param actionevent ActionEvent - The event that triggers the actions in the
	 * method.
	 */
	@Override
	public void actionPerformed( ActionEvent actionevent ) {
		// get all the component
		// get the current panel showing - need to do the isVisible
		// currently assumes all queries are SPARQL, needs some filtering if there are other types of queries
		// especially the ones that would use JGraph
		if ( QuestionPlaySheetStore.getInstance().getActiveSheet() != null ) {
			clearQuery();

			// get the query
			// gets the panel component and parameters
			JPanel panel = (JPanel) DIHelper.getInstance().getLocalProp( Constants.PARAM_PANEL_FIELD );
			DIHelper.getInstance().setLocalProperty( Constants.UNDO_BOOLEAN, false );
			// get the currently visible panel
			Component[] comps = panel.getComponents();
			JComponent curPanel = null;
			for ( int compIndex = 0; compIndex < comps.length && curPanel == null; compIndex++ ) {
				if ( comps[compIndex].isVisible() ) {
					curPanel = (JComponent) comps[compIndex];
				}
			}

			// get all the param field
			Component[] fields = curPanel.getComponents();
			Map<String, String> paramHash = new HashMap<>();
			String title = " - ";
			for ( Component field : fields ) {
				if ( field instanceof ParamComboBox ) {
					String fieldName = ( (ParamComboBox) field ).getParamName();
					String fieldValue = ( (ParamComboBox) field ).getSelectedItem() + "";
					paramHash.put( fieldName, fieldValue );
					title = title + " " + fieldValue;
				}
			}
			// now get the text area
			logger.debug( "Param Hash is set to " + paramHash );
			this.sparql.setText( gov.va.semoss.util.Utility.fillParam( this.sparql.getText(), paramHash ) );

			// Feed all of this information to the playsheet
			// get the layout class based on the query
			Properties prop = DIHelper.getInstance().getCoreProp();

			// uses pattern QUERY_Layout
			// need to get the key first here >>>>
			JComboBox questionList = (JComboBox) DIHelper.getInstance().getLocalProp( Constants.QUESTION_LIST_FIELD );
			String id = DIHelper.getInstance().getIDForQuestion( questionList.getSelectedItem() + "" );
			String keyToSearch = id + "_" + Constants.LAYOUT;
			String layoutValue = prop.getProperty( keyToSearch );

			// now just do class.forName for this layout Value and set it inside playsheet
			// need to template this out and there has to be a directive to identify 
			// specifically what sheet we need to refer to
			RepositoryList list = DIHelper.getInstance().getRepoList();
			// get the selected repository
			List<IEngine> repos = list.getSelectedValuesList();

			logger.info( "Layout value set to [" + layoutValue + "]" );
			logger.debug( "Repository is " + repos.toString() );

			logger.warn( "this function has been refactored, and probably does nothing" );

			GraphPlaySheet playSheet
					= (GraphPlaySheet) QuestionPlaySheetStore.getInstance().getActiveSheet();
			playSheet.updateGraph();
		}
	}

	/**
	 * Method clearQuery. Clears the query from the question box.
	 */
	public void clearQuery() {
		JComboBox questionBox = (JComboBox) DIHelper.getInstance().getLocalProp( Constants.QUESTION_LIST_FIELD );
		// get the currently selected index
		String question = (String) questionBox.getSelectedItem();
		// get the question Hash from the DI Helper to get the question name
		// get the ID for the question
		if ( question != null ) {
			String id = DIHelper.getInstance().getIDForQuestion( question );

			// now get the SPARQL query for this id
			String spql = DIHelper.getInstance().getProperty( id + "_" + Constants.QUERY );

			// get all the parameters and names from the SPARQL
			Map<String, String> paramHash = Utility.getParams( spql );
			// for each of the params pick out the type now
			for ( String key : paramHash.keySet() ) {
				StringTokenizer tokens = new StringTokenizer( key, "-" );
				// the first token is the name of the variable
				String varName = tokens.nextToken();
				String varType = Constants.EMPTY;
				if ( tokens.hasMoreTokens() ) {
					varType = tokens.nextToken();
				}
				logger.debug( varName + "<<>>" + varType );
				paramHash.put( key, "@" + varName + "@" );
			}
			spql = Utility.fillParam( spql, paramHash );
			logger.debug( spql + "<<<" );

			// just replace the SPARQL Area - Dont do anything else
			JTextArea area = (JTextArea) DIHelper.getInstance().getLocalProp( Constants.SPARQL_AREA_FIELD );
			area.setText( spql );
		}
	}

	/**
	 * Method setRightPanel. Sets the right panel that the listener will access.
	 *
	 * @param view JComponent
	 */
	public void setRightPanel( JComponent view ) {
		this.rightPanel = view;
	}

}
