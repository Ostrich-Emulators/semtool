/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.apache.log4j.Logger;
import gov.va.semoss.ui.components.ImportCreateDbPanel;
import gov.va.semoss.ui.components.ProgressTask;

/**
 *
 * @author ryan
 */
public class CreateDbAction extends DbAction {

	private static final Logger log = Logger.getLogger( CreateDbAction.class );
	private Frame frame;

	public CreateDbAction( String optg, Frame frame ) {
		super( optg, CREATE, "adddb" );
		putValue( AbstractAction.SHORT_DESCRIPTION, "Create a new database" );
	}

	@Override
	public void actionPerformed( ActionEvent e ) {
		ImportCreateDbPanel.showDialog( frame );
	}

	@Override
	protected ProgressTask getTask( ActionEvent e ) {
		throw new UnsupportedOperationException( "use actionPerformed instead" );
	}
}
