/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.actions;

import gov.va.semoss.poi.main.ImportData;
import gov.va.semoss.poi.main.ImportFileReader;
import gov.va.semoss.poi.main.ImportValidationException;
import gov.va.semoss.poi.main.LoadingSheetData;
import gov.va.semoss.poi.main.POIReader;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.impl.InMemorySesameEngine;
import gov.va.semoss.rdf.engine.util.EngineLoader;
import gov.va.semoss.rdf.engine.util.EngineManagementException;
import gov.va.semoss.rdf.engine.util.EngineUtil;
import gov.va.semoss.ui.actions.OpenAction.FileHandling;
import gov.va.semoss.ui.components.CustomSparqlPanel;
import gov.va.semoss.ui.components.FileBrowsePanel;
import gov.va.semoss.ui.components.FileBrowsePanel.CustomFileFilter;
import gov.va.semoss.ui.components.LoadingPlaySheetFrame;
import gov.va.semoss.ui.components.PlaySheetFrame;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;

import gov.va.semoss.ui.components.ProgressTask;
import gov.va.semoss.ui.components.SemossFileView;
import gov.va.semoss.ui.components.models.ValueTableModel;
import gov.va.semoss.ui.components.playsheets.GridRAWPlaySheet;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.MultiMap;
import gov.va.semoss.util.Utility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public class OpenSparqlAction extends DbAction {

	private static final Logger log = Logger.getLogger( OpenSparqlAction.class );
	private final List<File> files = new ArrayList<>();
	private final Frame frame;
	private final CustomSparqlPanel csp;

	public OpenSparqlAction( String optg, Frame frame, CustomSparqlPanel csp ) {
		super( optg, "Open SPARQL File", "semantic_dataset1" );
		this.frame = frame;
		this.csp = csp;
		putValue( SHORT_DESCRIPTION, "Open SPARQL File" );
		putValue( AbstractAction.MNEMONIC_KEY, KeyEvent.VK_S );
	}

	@Override
	public void actionPerformed( ActionEvent ae ) {
		files.clear();
		Preferences prefs = Preferences.userNodeForPackage( getClass() );
		File f = FileBrowsePanel.getLocationForEmptyPref( prefs, "lastimpdir" );

		JFileChooser chsr = new JFileChooser( f );
		chsr.setFileSelectionMode( JFileChooser.FILES_ONLY );
		chsr.setMultiSelectionEnabled( false );
		chsr.setFileView( new SemossFileView() );
		chsr.setFileFilter( new CustomFileFilter( "SPARQL Files", "spq", "sparql" ) );
		chsr.setDialogTitle( "Select SPARQL File to Open" );
		chsr.setMultiSelectionEnabled( true );

		if ( JFileChooser.APPROVE_OPTION == chsr.showOpenDialog( frame ) ) {
			prefs.put( "lastimpdir", chsr.getSelectedFile().getParent() );

			csp.loadFileToEmptyEditor( chsr.getSelectedFile() );
		}
	}

	@Override
	protected ProgressTask getTask( ActionEvent ae ) {
		throw new UnsupportedOperationException( "Not supported yet." );
	}
}
