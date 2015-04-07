/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import gov.va.semoss.ui.components.ImportExistingDbPanel;
import java.util.ArrayList;

/**
 *
 * @author ryan
 */
public class OpenAction extends ImportLoadingSheetAction {

	public OpenAction( String optg, Frame frame ) {
		super( optg, "Open File", frame, "" );
		putValue( SHORT_DESCRIPTION, "Import Spreadsheet" );
	}

	@Override
	public void actionPerformed( ActionEvent ae ) {
		ImportExistingDbPanel.showDialog( frame, getEngine(), new ArrayList<>() );
	}
}
