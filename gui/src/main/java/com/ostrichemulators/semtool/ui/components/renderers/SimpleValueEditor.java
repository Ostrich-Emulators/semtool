/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.renderers;

import java.awt.Component;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.ValueFactoryImpl;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

/**
 *
 * @author ryan
 */
public class SimpleValueEditor extends DefaultCellEditor {

	private URI datatype;

	public SimpleValueEditor() {
		super( new JTextField() );
	}

	@Override
	public Component getTableCellEditorComponent( JTable table, Object value,
			boolean sel, int r, int c ) {
		Value val = Value.class.cast( value );
		datatype = ( val instanceof Literal
				? Literal.class.cast( val ).getDatatype() : XMLSchema.ANYURI );

		JTextField cmp = JTextField.class.cast( super.getTableCellEditorComponent(
				table, val.stringValue(), sel, r, c ) );
		cmp.setBorder( null );
		return cmp;
	}

	@Override
	public Object getCellEditorValue() {
		Object o = super.getCellEditorValue();
		ValueFactory vf = new ValueFactoryImpl();
		Value val = null;
		try {
			val = vf.createLiteral( o.toString(), datatype );
		}
		catch ( Exception e ) {
			Logger.getLogger( getClass() ).warn( e, e );
		}

		return val;
	}
}
