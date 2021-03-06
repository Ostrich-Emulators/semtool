/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.main.listener.impl;

import com.ostrichemulators.semtool.ui.components.PlaySheetFrame;
import com.ostrichemulators.semtool.util.DIHelper;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 *
 * @author ryan
 */
public class PlaySheetFrameToolBarListener
		implements InternalFrameListener, ChangeListener {

	private final JToolBar jtb;

	public PlaySheetFrameToolBarListener( JToolBar tb ) {
		jtb = tb;
	}

	@Override
	public void internalFrameOpened( InternalFrameEvent e ) {
	}

	@Override
	public void internalFrameClosing( InternalFrameEvent e ) {
	}

	@Override
	public void internalFrameClosed( InternalFrameEvent e ) {
	}

	@Override
	public void internalFrameIconified( InternalFrameEvent e ) {
	}

	@Override
	public void internalFrameDeiconified( InternalFrameEvent e ) {
	}

	@Override
	public void internalFrameActivated( InternalFrameEvent e ) {
		jtb.removeAll();
		if ( e.getInternalFrame() instanceof PlaySheetFrame ) {
			PlaySheetFrame.class.cast( e.getInternalFrame() ).populateToolbar( jtb );
		}
		jtb.revalidate();
	}

	@Override
	public void internalFrameDeactivated( InternalFrameEvent e ) {
		jtb.removeAll();
		jtb.revalidate();
	}

	@Override
	public void stateChanged( ChangeEvent e ) {
		JDesktopPane desktop = DIHelper.getInstance().getDesktop();
		JInternalFrame jif = desktop.getSelectedFrame();
		if ( null != jif ) {
			internalFrameActivated( new InternalFrameEvent( jif, 0 ) );
		}
	}
}
