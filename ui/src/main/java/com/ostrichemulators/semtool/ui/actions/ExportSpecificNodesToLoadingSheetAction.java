/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;

import com.ostrichemulators.semtool.ui.components.ExportSpecificNodesPanel;
import com.ostrichemulators.semtool.ui.components.ProgressTask;

/**
 *
 * @author john.marquiss
 */
public class ExportSpecificNodesToLoadingSheetAction extends DbAction {
	private static final long serialVersionUID = 7432545689907490137L;
	private final Frame parent;

	public ExportSpecificNodesToLoadingSheetAction( String optg, Frame _parent ) {
		super( optg, EXPORTLSSOMENODES, "excel" );
		
		parent = _parent;
		putValue( AbstractAction.SHORT_DESCRIPTION, "Export Specific Nodes" );
		putValue( AbstractAction.MNEMONIC_KEY, KeyEvent.VK_S );
	}

	@Override
	public void actionPerformed( ActionEvent e ) {
		JDialog dlg = new JDialog( parent, true );
		dlg.setTitle( "Export Specified Nodes to Loading Sheets" );
		dlg.add( new ExportSpecificNodesPanel( getEngine() ) );
		dlg.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
		dlg.pack();
		dlg.setLocationRelativeTo(parent);
		dlg.setVisible( true );
	}

	@Override
	protected ProgressTask getTask( ActionEvent e ) {
		throw new UnsupportedOperationException( "not supported" );
	}
}