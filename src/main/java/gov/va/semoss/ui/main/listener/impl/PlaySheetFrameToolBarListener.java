/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.main.listener.impl;

import gov.va.semoss.om.Insight;
import gov.va.semoss.ui.components.PlaySheetFrame;
import gov.va.semoss.ui.components.playsheets.PlaySheetCentralComponent;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
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
	public void internalFrameOpened( InternalFrameEvent e ) { //Utility.showMessage("We're Opened!"); 
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
		
		//Determine whether to enable/disable the "Overlay" CheckBox, based upon
		//how the renderer of the selected visualization compares with that of the 
		//currently selected question:
		JDesktopPane pane = DIHelper.getInstance().getDesktop();
		PlaySheetFrame psf = PlaySheetFrame.class.cast( pane.getSelectedFrame() );
		JComboBox<Insight> cmb = (JComboBox) DIHelper.getInstance().getLocalProp( Constants.QUESTION_LIST_FIELD );
		Insight insight = cmb.getItemAt( cmb.getSelectedIndex() );
		String output = insight.getOutput();
		JCheckBox appendChkBox = (JCheckBox) DIHelper.getInstance().getLocalProp( Constants.APPEND );
		
		// the frame will be activated before there's a playsheet attached to it
		// make sure we have a playsheet before continuing
		PlaySheetCentralComponent pscc = psf.getActivePlaySheet();
		if( null != pscc && output.equals( pscc.getClass().getCanonicalName())){
			appendChkBox.setEnabled(true);
		}else{
			appendChkBox.setEnabled(false);
		}
	}

	@Override
	public void internalFrameDeactivated( InternalFrameEvent e ) {
		jtb.removeAll();
		jtb.revalidate();
		//Disable "Overlay" CheckBox. (Note: The Activated method may re-enable this CheckBox):
		JCheckBox appendChkBox = (JCheckBox) DIHelper.getInstance().getLocalProp( Constants.APPEND );
		appendChkBox.setEnabled(false);
	}
}
