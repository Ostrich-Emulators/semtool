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
 *****************************************************************************
 */
package gov.va.semoss.ui.main.listener.impl;

import java.awt.event.ActionEvent;

import javax.swing.JComponent;
import javax.swing.JTable;

import org.apache.log4j.Logger;

import gov.va.semoss.ui.components.api.IChakraListener;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.ui.helpers.GraphColorRepository;
import gov.va.semoss.ui.helpers.GraphShapeRepository;
import gov.va.semoss.ui.helpers.TypeColorShapeTable;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.QuestionPlaySheetStore;

/**
 * Repaints and clears the active sheet when refresh is pressed.
 */
public class ColorShapeClearRefreshListener implements IChakraListener {

	private static final Logger logger
			= Logger.getLogger( ColorShapeClearRefreshListener.class );

	@Override
	public void actionPerformed( ActionEvent actionevent ) {
		logger.info( "Calling action performed - refine view" );
		GraphPlaySheet playSheet 
				= (GraphPlaySheet) QuestionPlaySheetStore.getInstance().getActiveSheet();
		GraphShapeRepository.instance().clearAll();
		GraphColorRepository.instance().clearAll();

		playSheet.getVertexLabelFontTransformer().clearSizeData();
		playSheet.getEdgeLabelFontTransformer().clearSizeData();

		JTable table = (JTable) DIHelper.getInstance().getLocalProp( Constants.COLOR_SHAPE_TABLE );
		table.repaint();

		playSheet.updateGraph();
		// playSheet.repaint();
		playSheet.setVisible( true );
	}

	/**
	 * Method setView. Sets a JComponent that the listener will access and/or
	 * modify when an action event occurs.
	 *
	 * @param view the component that the listener will access
	 */
	@Override
	public void setView( JComponent view ) {

	}
}
