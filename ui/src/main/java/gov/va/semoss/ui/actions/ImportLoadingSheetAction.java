/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.actions;

import gov.va.semoss.ui.components.ImportCreateDbPanel;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;

import gov.va.semoss.ui.components.ImportExistingDbPanel;
import gov.va.semoss.ui.components.ProgressTask;

/**
 *
 * @author ryan
 */
public class ImportLoadingSheetAction extends DbAction {

	private static final Logger log = Logger.getLogger( ImportLoadingSheetAction.class );
	protected Frame frame;

	public ImportLoadingSheetAction( String optg, Frame frame ) {
		this( optg, IMPORTLS, frame, "excel" );
	}

	public ImportLoadingSheetAction( String optg, String name, Frame frame, 
			String iconname ) {
		super( optg, name, iconname );
		putValue( SHORT_DESCRIPTION, "Import loading sheets" );
		putValue( AbstractAction.MNEMONIC_KEY, KeyEvent.VK_E );
	}

	@Override
	public void actionPerformed( ActionEvent e ) {
		if( null == getEngine() ){
			ImportCreateDbPanel.showDialog( frame );
		}
		else{
			ImportExistingDbPanel.showDialog( frame, getEngine(), new ArrayList<>() );
		}
	}

	@Override
	protected ProgressTask getTask( ActionEvent e ) {
		throw new UnsupportedOperationException( "not supported" );
	}
}
