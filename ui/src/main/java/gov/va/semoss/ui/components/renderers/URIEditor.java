/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.renderers;

import java.awt.Component;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 *
 * @author ryan
 */
public class URIEditor extends DefaultCellEditor {

	public URIEditor() {
		super( new JTextField() );
	}

	@Override
	public Component getTableCellEditorComponent( JTable table, Object value,
			boolean sel, int r, int c ) {
		JTextField cmp = JTextField.class.cast( super.getTableCellEditorComponent( 
				table, value.toString(), sel, r, c ) );
		cmp.setBorder( null );
		return cmp;
	}

	@Override
	public Object getCellEditorValue() {
		Object o = super.getCellEditorValue();
		URI uri = null;
		try {
			uri = new URIImpl( o.toString() );
		}
		catch ( Exception e ) {
			Logger.getLogger( getClass() ).warn( e, e );
		}

		return uri;
	}
}
