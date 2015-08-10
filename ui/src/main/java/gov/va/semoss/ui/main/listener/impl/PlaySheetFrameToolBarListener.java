/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.main.listener.impl;

import gov.va.semoss.ui.components.PlaySheetFrame;
import javax.swing.JToolBar;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 *
 * @author ryan
 */
public class PlaySheetFrameToolBarListener implements InternalFrameListener {

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
}
