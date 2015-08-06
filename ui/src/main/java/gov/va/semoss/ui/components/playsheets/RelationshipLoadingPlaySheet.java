/**
 * *****************************************************************************
 * Copyright 2013 SEMOSS.ORG
 *
 * This file is part of SEMOSS.
 *
 * SEMOSS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SEMOSS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SEMOSS. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package gov.va.semoss.ui.components.playsheets;

import gov.va.semoss.poi.main.LoadingSheetData;
import gov.va.semoss.poi.main.LoadingSheetData.LoadingNodeAndPropertyValues;
import gov.va.semoss.ui.actions.DbAction;
import gov.va.semoss.ui.components.models.LoadingSheetModel;
import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.log4j.Logger;
import org.openrdf.model.Value;

/**
 */
public class RelationshipLoadingPlaySheet extends LoadingPlaySheetBase {

	private static final Logger log
			= Logger.getLogger( RelationshipLoadingPlaySheet.class );
	private final JTextField relationship = new JTextField();
	private final String defaultRelationship;

	public RelationshipLoadingPlaySheet( String rel, List<Value[]> valdata,
			List<String> headers ) {
		super( LoadingSheetModel.forRel( rel, valdata, headers ) );
		defaultRelationship = rel;

		if ( headers.size() < 2 ) {
			throw new IllegalArgumentException( "Incomplete headers given" );
		}

		init();
	}

	public RelationshipLoadingPlaySheet( LoadingSheetData lsd ) {
		this( lsd, false );
	}

	public RelationshipLoadingPlaySheet( LoadingSheetData lsd, boolean allowInserts ) {
		super( new LoadingSheetModel( lsd ) );

		defaultRelationship = lsd.getRelname();
		init();

		getModel().setReadOnly( !allowInserts );
		getModel().setAllowInsertsInPlace( allowInserts );

		setTitle( lsd.getName() );
		setHeaders( lsd.getHeaders() );
	}

	private void init() {
		JLabel lbl = new JLabel( "Relationship name:" );
		relationship.setText( defaultRelationship );

		JPanel pnl = new JPanel( new BorderLayout() );
		pnl.add( lbl, BorderLayout.WEST );
		pnl.add( relationship, BorderLayout.CENTER );

		add( pnl, BorderLayout.NORTH );

		relationship.addKeyListener( new KeyAdapter() {

			@Override
			public void keyReleased( KeyEvent e ) {
				LoadingSheetModel model = getLoadingModel();

				if ( model.isRealTimeChecking() ) {
					model.setRelationshipName( relationship.getText() );
					RelationshipLoadingPlaySheet.this.setErrorLabel();
				}

				lbl.setIcon( model.getModelErrorColumns().contains( -1 )
						? DbAction.getIcon( "error" ) : null );
			}
		} );
	}

	@Override
	public boolean okToLoad() {
		return !relationship.getText().isEmpty();
	}

	@Override
	public boolean correct() {
		Object str = JOptionPane.showInputDialog( null, "Please specify a relationship name",
				"Invalid Relationship", JOptionPane.QUESTION_MESSAGE, null, null,
				defaultRelationship );
		if ( !( null == str || str.toString().isEmpty() ) ) {
			relationship.setText( str.toString() );
			return true;
		}
		return false;
	}
}
