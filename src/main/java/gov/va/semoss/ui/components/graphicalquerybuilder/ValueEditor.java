/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import gov.va.semoss.ui.components.graphicalquerybuilder.ConstraintPanel.ConstraintValue;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 *
 * @author ryan
 */
public class ValueEditor extends AbstractCellEditor
		implements TableCellEditor, ActionListener {

	private static final String EDIT = "edit";
	private final JButton button = new JButton();
	private Value value;
	private Map<URI, String> types;
	private URI type;
	private boolean checked;
	private QueryNodeEdgeBase nodeedge;

	public ValueEditor() {
		//Set up the editor (from the table's point of view),
		//which is a button.
		//This button brings up the color chooser dialog,
		//which is the editor from the user's point of view.
		button.setActionCommand( EDIT );
		button.addActionListener( this );
		button.setBorderPainted( false );
	}

	public void setType( URI type ) {
		this.type = type;
	}

	public void setChecked( boolean b ) {
		checked = b;
	}

	public void setChoices( Map<URI, String> tt ) {
		types = tt;
	}

	public void setNode( QueryNodeEdgeBase b ) {
		nodeedge = b;
	}

	/**
	 * Handles events from the editor button and from the dialog's OK button.
	 */
	@Override
	public void actionPerformed( ActionEvent e ) {
		if ( EDIT.equals( e.getActionCommand() ) ) {
			try {
				ConstraintValue cv = null;
				if ( null == types ) {
					Collection<ConstraintValue> cvs
							= ConstraintPanel.getValues( type, EDIT, Arrays.asList( value ), checked );
					if ( null == cvs || cvs.isEmpty() ) {
						cv = null;
					}
					else {
						cv = cvs.iterator().next();
					}
				}
				else {
					// editing a type
					cv = ConstraintPanel.getValue( type, "Type", URI.class.cast( value ),
							types, checked );
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
		this.value = Value.class.cast( value );
		return button;
	}
}
