/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.actions;

import gov.va.semoss.poi.main.NodeLoadingSheetData;
import gov.va.semoss.poi.main.RelationshipLoadingSheetData;
import gov.va.semoss.ui.components.LoadingPlaySheetFrame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.apache.log4j.Logger;
import gov.va.semoss.ui.components.ProgressTask;
import javax.swing.JDesktopPane;

/**
 *
 * @author ryan
 */
public class CreateLoadingSheetAction extends DbAction {

	private static final Logger log = Logger.getLogger( CreateLoadingSheetAction.class );

	public static enum Type {

		RELATION, NODE
	};

	private final Type type;
	private final JDesktopPane desktop;

	public CreateLoadingSheetAction( String optg, JDesktopPane pane, Type type ) {
		super( optg, ( Type.NODE == type ? "Node" : "Relationship" ) + " Loading Sheet" );
		this.type = type;
		this.desktop = pane;
		putValue( AbstractAction.SHORT_DESCRIPTION, "Creates an empty Loading Sheet" );
	}

	@Override
	public void actionPerformed( ActionEvent ae ) {
		LoadingPlaySheetFrame frame = new LoadingPlaySheetFrame( getEngine() );
		frame.setTitle( "Loading Sheet Data" );

		if ( Type.RELATION == type ) {
			NodeLoadingSheetData nlsd = new NodeLoadingSheetData( "", "" );
			frame.add( nlsd );
		}
		else {
			RelationshipLoadingSheetData rlsd
					= new RelationshipLoadingSheetData( "", "", "", "" );
			frame.add( rlsd );
		}

		desktop.add( frame );
	}

	@Override
	protected ProgressTask getTask( ActionEvent e ) {
		throw new UnsupportedOperationException( "not supported" );
	}
}
