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

import java.awt.CardLayout;
import java.awt.event.ActionEvent;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import gov.va.semoss.om.Insight;
import gov.va.semoss.ui.components.ParamPanel;
import gov.va.semoss.ui.components.api.IChakraListener;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;

/**
 *  listens for the change in questions, then refreshes the sparql area with the actual question in SPARQL
 *	 parses the SPARQL to find out all the parameters
 *	 refreshes the panel with all the parameters
 */
public class QuestionListener implements IChakraListener {

	JPanel view = null; // reference to the param panel
	String prevQuestionId = "";
	private static final Logger logger = Logger.getLogger(QuestionListener.class);

	/**
	 * Method actionPerformed.  Dictates what actions to take when an Action Event is performed.
	 * @param actionevent ActionEvent - The event that triggers the actions in the method.
	 */
  @Override
  public void actionPerformed( ActionEvent actionevent ) {
    JComboBox<Insight> questionBox = (JComboBox<Insight>) actionevent.getSource();
    // get the currently selected index
    Insight question = Insight.class.cast( questionBox.getSelectedItem() );
		// get the question Hash from the DI Helper to get the question name
    // get the ID for the question
    if ( question != null ) {      
      //String id = DIHelper.getInstance().getIDForQuestion(question);
			//id = in.getId();

			// now get the SPARQL query for this id
      //String sparql = DIHelper.getInstance().getProperty(id + "_" + Constants.QUERY);
      String sparql = question.getSparql();
      logger.debug( "Sparql is " + sparql );

      ParamPanel panel = new ParamPanel();
      panel.setInsight( question );

      panel.paintParam();

			// finally add the param to the core panel
      // confused about how to add this need to revisit
      JPanel mainPanel = (JPanel) DIHelper.getInstance().getLocalProp( Constants.PARAM_PANEL_FIELD );
      mainPanel.add( panel, question + "_1" ); // mark it to the question index
      CardLayout layout = (CardLayout) mainPanel.getLayout();
      layout.show( mainPanel, question + "_1" );

    }
  }

	/**
	 * Method setView. Sets a JComponent that the listener will access and/or modify when an action event occurs.  
	 * @param view the component that the listener will access
	 */
	@Override
	public void setView(JComponent view) {
		this.view = (JPanel)view;

	}
}
