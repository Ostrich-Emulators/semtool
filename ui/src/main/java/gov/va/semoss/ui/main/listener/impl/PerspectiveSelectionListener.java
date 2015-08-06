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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import org.apache.log4j.Logger;
import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Perspective;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.util.DIHelper;

/**
 * Controls selection of the perspective from the left hand pane.
 */
public class PerspectiveSelectionListener extends AbstractListener {
	public JComponent view = null;
	private static final Logger logger = Logger.getLogger(PerspectiveSelectionListener.class);	
	
	// needs to find what is being selected from event
	// based on that refresh the view of questions for that given perspective
	
	/**
	 * Method actionPerformed.  Dictates what actions to take when an Action Event is performed.
	 * @param e ActionEvent - The event that triggers the actions in the method.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
    JComboBox<Perspective> bx = (JComboBox<Perspective>) e.getSource();
    Perspective perspective;
    if( null == bx.getSelectedItem() ){
      logger.warn( "no perspective selected!" );
      perspective = null;
    }
    else {
      // don't know why we need to cast this
      perspective = bx.getItemAt( bx.getSelectedIndex() );
    }
    
    logger.debug( "Selected " + perspective + " <> " + view );
    JComboBox<Insight> qp = (JComboBox<Insight>) view;

    // List<String> tTip = new ArrayList<>();
    qp.removeAllItems();

    IEngine engine = DIHelper.getInstance().getRdfEngine();
    List<Insight> insights = new ArrayList<>();
    for( Insight u : engine.getInsightManager().getInsights( perspective )){
      qp.addItem( u );
    }
	}

	/**
	 * Method setView. Sets a JComponent that the listener will access and/or modify when an action event occurs.  
	 * @param view the component that the listener will access
	 */
	@Override
	public void setView(JComponent view) {
		logger.debug("View is set " + view);
		this.view = view;
	}
}
