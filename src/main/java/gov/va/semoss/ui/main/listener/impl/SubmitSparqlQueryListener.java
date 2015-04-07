package gov.va.semoss.ui.main.listener.impl;

import gov.va.semoss.om.Insight;
import gov.va.semoss.rdf.engine.api.IEngine;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import gov.va.semoss.ui.components.ExecuteQueryProcessor;
import gov.va.semoss.ui.components.OperationsProgress.OperationsProgressListener;
import gov.va.semoss.ui.components.api.IChakraListener;
import gov.va.semoss.ui.components.api.IPlaySheet;
import gov.va.semoss.ui.helpers.PlaysheetCreateRunner;
import gov.va.semoss.ui.helpers.PlaysheetOverlayRunner;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.Utility;
import gov.va.semoss.tabbedqueries.TabbedQueries;
import gov.va.semoss.ui.components.GraphPlaySheetFrame;
import gov.va.semoss.ui.components.OperationsProgress;
import gov.va.semoss.ui.components.PlayPane;
import gov.va.semoss.ui.components.PlaySheetFrame;
import gov.va.semoss.ui.components.ProgressTask;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.ui.components.playsheets.PlaySheetCentralComponent;
import gov.va.semoss.util.PlaySheetEnum;

public class SubmitSparqlQueryListener extends AbstractAction implements IChakraListener {
	
	private static final long serialVersionUID = 5236863287462387L;
	// where all the parameters are set
	// this will implement a cardlayout and then on top of that the param panel
	JPanel paramPanel = null;
	// right hand side panel
	JComponent rightPanel = null;
	private static final Logger logger = Logger.getLogger( SubmitSparqlQueryListener.class );
	TabbedQueries sparql = null;
	boolean custom = false;
	boolean append = false;
	//Main Tab's "Overlay" CheckBox:
	JCheckBox appendChkBox;
	//Main Tab's "Insights" ComboBox:
	JComboBox<Insight> cboInsights;	
	
	/**
	 * Handles submissions from the "Custom Sparql Query" window. This method will
	 * also handle Overlays (appends) to the currently selected grid or graph.
	 *
	 * @param actionevent
	 */
	@Override
	public void actionPerformed( ActionEvent actionevent ) {
		//"Custom Sparql Query" window's "Overlay" CheckBox:
		JCheckBox appendQueryChkBox = (JCheckBox) DIHelper.getInstance().getLocalProp( Constants.APPEND_SPARQL_QUERY );
		//Main Tab's "Overlay" CheckBox:
		appendChkBox = (JCheckBox) DIHelper.getInstance().getLocalProp( Constants.APPEND );
		//Main Tab's "Insights" ComboBox:
		cboInsights	= (JComboBox) DIHelper.getInstance().getLocalProp( Constants.QUESTION_LIST_FIELD );
		
		boolean appending = appendQueryChkBox.isSelected();

		//set custom and append variables to processor
		ExecuteQueryProcessor exQueryProcessor = new ExecuteQueryProcessor();
		exQueryProcessor.setAppendBoolean( appending );
		exQueryProcessor.setCustomBoolean( false );

		//Setup playsheet, exQueryProcessor will also take care of append or create query: 
		TabbedQueries tabbedQueries = (TabbedQueries) DIHelper.getInstance().getLocalProp( Constants.SPARQL_AREA_FIELD );
		String query = tabbedQueries.getTextOfSelectedTab();
		String playSheetString = ( (JComboBox) DIHelper.getInstance().getLocalProp( Constants.PLAYSHEET_COMBOBOXLIST ) ).getSelectedItem() + "";
		IEngine eng = DIHelper.getInstance().getRdfEngine();
		JDesktopPane pane = DIHelper.getInstance().getDesktop();
		String tabTitle = tabbedQueries.getTitleAt( tabbedQueries.getSelectedIndex() );

		try {
			String classname = PlaySheetEnum.getClassFromName( playSheetString );
			Class<?> k = Class.forName( classname );
			if ( PlaySheetCentralComponent.class.isAssignableFrom( k ) ) {
				doitNewSkool( classname, query, eng, appending, pane, tabTitle );
				return;
			}
		}
		catch ( ClassNotFoundException e ) {
			// don't care
		}
		
		exQueryProcessor.processCustomQuery( query, playSheetString, appending );

		// get the selected repository, in case someone selects multiple, it'll always use first one
		//Get playsheet, and add title of currently selected tab in TabbedQueries.
		//Then figure out if its append or create and then call the right threadrunners:
		IPlaySheet playSheet = exQueryProcessor.getPlaySheet();
		if ( null != playSheet ) {
			// RPB: I don't know when we'd ever get here since the only code path is
			// when the query is an update, and that always returns a null playsheet
			logger.warn( "how did you get here? playsheet: " + playSheetString );
			
			playSheet.setTitle( tabTitle );
			//If this is a Grid or Raw Grid, then set the playsheet's insight name
			//to the current tab's title:
			Runnable playRunner;
			
			if ( appendQueryChkBox.isSelected() ) {
				logger.debug( "Appending from Query CheckBox" );
				playRunner = new PlaysheetOverlayRunner( playSheet );
			}
			else {
				playSheet.setJDesktopPane( pane );
				playRunner = new PlaysheetCreateRunner( playSheet );
			}
			
			Thread playThread = new Thread( playRunner );
			playThread.start();
			
		}
	}
	
	private void doitNewSkool( String output, String query, IEngine eng,
			boolean appending, JDesktopPane pane, String title ) {
		
		ProgressTask pt = null;
		if ( appending ) {
			PlaySheetFrame psf = PlaySheetFrame.class.cast( pane.getSelectedFrame() );
            
			pt = psf.getOverlayTask( query, title, title );
		}
		else {
			PlaySheetCentralComponent pscc = null;
			try {
				Object o = Class.forName( output ).newInstance();
				pscc = PlaySheetCentralComponent.class.cast( o );
			}
			catch ( ClassNotFoundException | InstantiationException | IllegalAccessException e ) {
				throw new IllegalArgumentException( output
						+ " not yet updated to the new handling", e );
			}
			PlaySheetFrame psf = ( GraphPlaySheet.class.isAssignableFrom( pscc.getClass() )
					? new GraphPlaySheetFrame( eng ) : new PlaySheetFrame( eng ) );
			pscc.setTitle( title );
			psf.addTab( title, pscc );
			
			psf.setTitle( title );
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
