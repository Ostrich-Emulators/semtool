/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.util.DBToLoadingSheetExporter;
import gov.va.semoss.ui.components.graphicalquerybuilder.ConstraintPanel.ConstraintValue;
import gov.va.semoss.util.Utility;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

/**
 *
 * @author ryan
 */
public class ValueEditor extends AbstractCellEditor
		implements TableCellEditor, ActionListener {

	private static final String EDIT = "edit";
	private final JButton button = new JButton();
	private final IEngine engine;
	private Value value;
	private List<URI> concepts;
	private URI type;
	private boolean checked;

	public ValueEditor( IEngine e ) {
		//Set up the editor (from the table's point of view),
		//which is a button.
		//This button brings up the color chooser dialog,
		//which is the editor from the user's point of view.
		button.setActionCommand( EDIT );
		button.addActionListener( this );
		button.setBorderPainted( false );
		engine = e;
	}

	/**
	 * Handles events from the editor button and from the dialog's OK button.
	 */
	@Override
	public void actionPerformed( ActionEvent e ) {
		if ( EDIT.equals( e.getActionCommand() ) ) {
			try {
				ConstraintValue cv = null;
				if ( null == concepts ) {
					cv = ConstraintPanel.getValue( type, EDIT, value, checked );
				}
				else {
					// editing a type
					cv = ConstraintPanel.getValue( type, "Type", URI.class.cast( value ),
							Utility.sortUrisByLabel( Utility.getInstanceLabels( concepts, engine ) ),
							checked );
				}

				if ( null != cv ) {
					value = cv.val;
				}
			}
			finally {
				//Make the renderer reappear.
				fireEditingStopped();
			}
		}
		else { //User pressed dialog's "OK" button.
			Logger.getLogger( getClass() ).debug( "here I am?" );
		}
	}

	//Implement the one CellEditor method that AbstractCellEditor doesn't.
	@Override
	public Object getCellEditorValue() {
		return value;
	}

	//Implement the one method defined by TableCellEditor.
	@Override
	public Component getTableCellEditorComponent( JTable table,
			Object value, boolean isSelected, int row, int col ) {

		type = URI.class.cast( table.getValueAt( row, col - 1 ) );
		if ( RDF.TYPE.equals( type ) ) {
			// we're changing the type, so we need a dropdown of concept types
			concepts = DBToLoadingSheetExporter.createConceptList( engine );
		}

		// included in return?
		checked = Boolean.class.cast( table.getValueAt( row, col + 2 ) );

		this.value = Value.class.cast( value );
		return button;
	}
}
