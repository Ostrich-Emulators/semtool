/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.actions;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.util.EngineManagementException;
import gov.va.semoss.rdf.engine.util.EngineOperationAdapter;
import gov.va.semoss.rdf.engine.util.EngineOperationListener;
import gov.va.semoss.rdf.engine.util.EngineUtil;

import gov.va.semoss.rdf.engine.util.VocabularyRegistry;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import org.apache.log4j.Logger;

import gov.va.semoss.util.Utility;
import gov.va.semoss.ui.components.FileBrowsePanel;
import gov.va.semoss.ui.components.ProgressTask;

import java.io.IOException;
import java.util.Collection;

import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class ImportInsightsAction extends DbAction {

	private static final Logger log
			= Logger.getLogger( ImportInsightsAction.class );
	private final Frame frame;
	private File importfile;
	private final boolean clear;

	public ImportInsightsAction( String optg, boolean clearfirst, Frame frame ) {
		super( optg, ( clearfirst ? "Replace" : "Add" ) + " Insights",
				"semantic-webdoc" );
		this.frame = frame;
		putValue( AbstractAction.SHORT_DESCRIPTION,
				( clearfirst ? "Replace" : "Add" ) + " insights from a file" );
		putValue( AbstractAction.MNEMONIC_KEY,
				( clearfirst ? KeyEvent.VK_R : KeyEvent.VK_A )  );
		clear = clearfirst;
	}

	@Override
	protected boolean preAction( ActionEvent ae ) {
		Preferences prefs
				= Preferences.userNodeForPackage( ImportInsightsAction.class );
		File emptypref = FileBrowsePanel.getLocationForEmptyPref( prefs,
				"lastinsightsimp" );
		JFileChooser chsr = new JFileChooser( emptypref );
		chsr.setDialogTitle( "Select Insights File" );
		chsr.setApproveButtonText( "Import" );
		chsr.setFileFilter( FileBrowsePanel.getInsightTypesFilter() );

		int retval = chsr.showOpenDialog( frame );
		if ( JFileChooser.APPROVE_OPTION == retval ) {
			importfile = chsr.getSelectedFile();

			if ( null != importfile ) {
				prefs.put( "lastinsightsimp", importfile.getParent() );
				return true;
			}
		}
		return false;
	}

	@Override
	protected ProgressTask getTask( ActionEvent ae ) {
		ProgressTask pt = new ProgressTask( "Importing "
				+ importfile.getAbsolutePath() + " Insights to " + getEngineName(),
				new Runnable() {
					@Override
					public void run() {
						try {
							EngineOperationListener eol = new EngineOperationAdapter() {
								@Override
								public void insightsModified( IEngine eng, Collection<URI> ps,
										Collection<URI> is ) {
									if ( eng.equals( getEngine() ) ) {
										EngineUtil.getInstance().removeEngineOpListener( this );
										int numPs = ps.size();
										Utility.showMessage( "Imported " + numPs + " perspective"
												+ ( numPs > 1 ? "s" : "" ) + " to " + getEngineName() );
									}
								}
							};

							EngineUtil.getInstance().addEngineOpListener( eol );
							EngineUtil.getInstance().importInsights( getEngine(), importfile,
									clear, VocabularyRegistry.getVocabularies( true ).values() );
						}
						catch ( IOException | EngineManagementException re ) {
							Utility.showError( re.getLocalizedMessage() );
							log.error( re, re );
						}
					}
				} );

		return pt;
	}
}
