/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.models;

import com.ostrichemulators.semtool.user.User;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.ValueFactoryImpl;

/**
 *
 * @author ryan
 */
public class NamespaceTableModel extends ValueTableModel {

	private final boolean editable;

	public NamespaceTableModel( boolean editable ) {
		this( editable, true );
	}

	public NamespaceTableModel( boolean editable, boolean allowinserts ) {
		this.editable = editable;
		setAllowInsertsInPlace( editable && allowinserts );
	}

	public void setNamespaces( Map<String, String> ns ) {
		List<Value[]> vals = new ArrayList<>();
		ValueFactory vf = new ValueFactoryImpl();
		for ( Map.Entry<String, String> en : ns.entrySet() ) {
			Value row[] = { vf.createLiteral( en.getKey() ), vf.createLiteral( en.getValue() ) };
			vals.add( row );
		}

		setData( vals, Arrays.asList( "Prefix", "Namespace" ) );
	}

	public Map<String, String> getNamespaces() {
		LinkedHashMap<String, String> ret = new LinkedHashMap<>();
		for ( int r = 0; r < getRowCount(); r++ ) {
			if ( !isInsertRow( r ) ) {
				ret.put( getValueAt( r, 0 ).toString(), getValueAt( r, 1 ).toString() );
			}
		}

		return ret;
	}

	@Override
	public boolean isCellEditable( int row, int col ) {
		return ( editable && super.isCellEditable( row, col ) );
	}
}
