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
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Perspective;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.ui.components.ExecuteQueryProcessor;
import gov.va.semoss.ui.components.ParamComboBox;
import gov.va.semoss.ui.components.api.IChakraListener;
import gov.va.semoss.ui.components.api.IPlaySheet;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.tabbedqueries.TabbedQueries;
import gov.va.semoss.ui.components.OperationsProgress;
import gov.va.semoss.ui.components.PlaySheetFrame;
import gov.va.semoss.ui.components.ProgressTask;
import gov.va.semoss.ui.components.PlayPane;
import gov.va.semoss.ui.components.playsheets.PlaySheetCentralComponent;

/**
 * 1. Get information from the Question for the query 2. Process the query to
 * create a graph 3. Create a playsheet and fill it with the respective
 * information 4. Set all the controls reference within the PlaySheet
 */
public class ProcessQueryListener extends AbstractAction implements IChakraListener {
	private static final Logger logger = Logger.getLogger( ProcessQueryListener.class );
	private static final long serialVersionUID = 5236863287462387L;
	
	// where all the parameters are set
	// this will implement a cardlayout and then on top of that the param panel
	JPanel paramPanel = null;
	// right hand side panel
	JComponent rightPanel = null;
	TabbedQueries sparql = null;
	boolean custom = false;
	boolean append = false;
	JCheckBox appendChkBox;
	JComboBox<Insight> cboInsights;

	/**
	 * Method actionPerformed. Dictates what actions to take when an Action Event
	 * is performed.
	 *
	 * @param actionevent ActionEvent - The event that triggers the actions in the
	 * method.
	 */
	@Override
	public void actionPerformed( ActionEvent actionevent ) {
		//Open the "Display Pane": 
		PlayPane.rightTabs.setSelectedIndex( 0 );

		// get all the component
		// get the current panel showing - need to do the isVisible
		// currently assumes all queries are SPARQL, needs some filtering if
		// there are other types of queries
		// especially the ones that would use JGraph
		// get the query
		//initiate executeQueryProcessor
		appendChkBox = (JCheckBox) DIHelper.getInstance().getLocalProp( Constants.APPEND );
		boolean appending = appendChkBox.isSelected();

		//Get the currently selected perspective for the playsheet:
		JComboBox<Perspective> cboPerspectives
				= (JComboBox<Perspective>) DIHelper.getInstance().getLocalProp( Constants.PERSPECTIVE_SELECTOR );
		Perspective perspective = cboPerspectives.getItemAt( cboPerspectives.getSelectedIndex() );

		//set custom and append variables to processor
		ExecuteQueryProcessor exQueryProcessor = new ExecuteQueryProcessor();
		exQueryProcessor.setAppendBoolean( appending );
		exQueryProcessor.setPerspective( perspective );

		// get the selected repository, in case someone selects multiple, it'll always use first one
		IEngine eng = DIHelper.getInstance().getRdfEngine();

		//Setup playsheet: 
		cboInsights = (JComboBox) DIHelper.getInstance().getLocalProp( Constants.QUESTION_LIST_FIELD );
		Insight insight = cboInsights.getItemAt( cboInsights.getSelectedIndex() );
		String output = insight.getOutput();

		try {
			Class<?> k = Class.forName( output );
			if( !( PlaySheetCentralComponent.class.isAssignableFrom( k ) ) ){
				throw new IllegalArgumentException( "Defunct playsheet class: " + output );
			}
		}
		catch ( ClassNotFoundException e ) {
			throw new IllegalArgumentException( "Unhandled playsheet class: " + output,
					e );
		}
		//get Swing UI and set ParamHash";
		JPanel panel = (JPanel) DIHelper.getInstance().getLocalProp( Constants.PARAM_PANEL_FIELD );
		DIHelper.getInstance().setLocalProperty( Constants.UNDO_BOOLEAN, false );

		// get the currently visible panel
		Component[] comps = panel.getComponents();
		JComponent curPanel = null;
		for ( Component comp : comps ) {
			if ( comp.isVisible() ) {
				curPanel = (JComponent) comp;
				break;
			}
		}

		Map<String, String> paramHash = new HashMap<>();;
		if ( null != curPanel ) {
			// get all the param field
			Component[] fields = curPanel.getComponents();

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
		}

		JDesktopPane pane = DIHelper.getInstance().getDesktop();
		doitNewSkool( perspective, insight, paramHash, appending, pane );
	}

	private void doitNewSkool( Perspective persp, Insight insight,
			Map<String, String> paramHash, boolean appending, JDesktopPane pane ) {
		String output = insight.getOutput();

		IEngine eng = DIHelper.getInstance().getRdfEngine();
		String query = ExecuteQueryProcessor.getSparql( insight, paramHash );
		ProgressTask pt = null;
		if ( appending ) {
			PlaySheetFrame psf = PlaySheetFrame.class.cast( pane.getSelectedFrame() );
			String title = persp.getLabel() + "-Insight-" + insight.getOrder( persp.getUri() );

			pt = psf.getOverlayTask( query, insight.getLabel(), title );
		}
		else {
			String title = persp.getLabel() + "-Insight-" + insight.getOrder( persp.getUri() );

			PlaySheetCentralComponent pscc = null;
			try {
				Object o = Class.forName( output ).newInstance();
				pscc = PlaySheetCentralComponent.class.cast( o );
			}
			catch ( ClassNotFoundException | InstantiationException | IllegalAccessException e ) {
				throw new IllegalArgumentException( output
						+ " not yet updated to the new handling", e );
			}

			PlaySheetFrame psf = new PlaySheetFrame( eng );
			pscc.setTitle( insight.getLabel() );
			psf.addTab( title, pscc );

			psf.setTitle( insight.getLabel() );
			DIHelper.getInstance().getDesktop().add( psf );

			pt = psf.getCreateTask( query );
		}

		OperationsProgress op = OperationsProgress.getInstance( PlayPane.UIPROGRESS );
		op.add( pt );
	}

	/**
	 * Method setRightPanel. Sets the right panel that the listener will access.
	 *
	 * @param view JComponent
	 */
	public void setRightPanel( JComponent view ) {
		this.rightPanel = view;
	}

	/**
	 * Method setView. Sets a JComponent that the listener will access and/or
	 * modify when an action event occurs.
	 *
	 * @param view the component that the listener will access
	 */
	@Override
	public void setView( JComponent view ) {
		this.sparql = (TabbedQueries) view;
	}
}
