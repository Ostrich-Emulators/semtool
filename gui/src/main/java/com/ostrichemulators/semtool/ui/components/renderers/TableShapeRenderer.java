/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.renderers;

import com.ostrichemulators.semtool.om.NamedShape;
import com.ostrichemulators.semtool.util.IconBuilder;
import java.awt.Color;
import java.awt.Component;
import java.awt.Shape;

import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author ryan
 */
public class TableShapeRenderer extends DefaultTableCellRenderer {

	private static final Map<Object, Icon> icons = new HashMap<>();
	double iconsize = -1;

	public TableShapeRenderer() {
	}

	public TableShapeRenderer( double sz ) {
		iconsize = sz;
	}

	public void setSize( double sz ) {
		iconsize = sz;
	}

	@Override
	public Component getTableCellRendererComponent( JTable table, Object val,
			boolean isSelected, boolean hasFocus, int row, int column ) {

		if ( null == val ) {
			return super.getTableCellRendererComponent( table, val, isSelected,
					hasFocus, row, column );
		}

		super.getTableCellRendererComponent( table, "", isSelected,
				hasFocus, row, column );
		if ( !icons.containsKey( val ) ) {
			IconBuilder bldr = ( val instanceof Shape
					? new IconBuilder( Shape.class.cast( val ) )
					: new IconBuilder( NamedShape.class.cast( val ) ) );
			bldr.setIconSize( iconsize < 1 ? getWidth() : iconsize );
			bldr.setPadding( 1 );
			bldr.setStroke( Color.BLACK );

			icons.put( val, bldr.build() );
		}
		setIcon( icons.get( val ) );

		return this;
	}
}
