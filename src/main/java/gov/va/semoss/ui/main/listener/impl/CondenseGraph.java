/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.main.listener.impl;

import gov.va.semoss.ui.components.GraphCondensePanel;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

/**
 *
 * @author ryan
 */
public class CondenseGraph extends AbstractAction {

	private final GraphPlaySheet gps;

	public CondenseGraph( GraphPlaySheet ps ) {
		super( "Condense Graph" );
		putValue( Action.SHORT_DESCRIPTION,
				"Condense the graph by removing intermediate edges" );
		gps = ps;
	}

	@Override
	public void actionPerformed( ActionEvent e ) {
		GraphCondensePanel gcp = new GraphCondensePanel( gps );
		String options[] = { "Okay", "Cancel" };

		int opt = JOptionPane.showOptionDialog( null, gcp, "Condense Graph",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
				options[0] );
		if ( 0 == opt ) {
			JOptionPane.showMessageDialog( null, gcp.getEdgeTypeToRemove() + "->"
					+ gcp.getRemovalStrategy() );
		}
	}

}
