/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.renderers;

import com.ostrichemulators.semtool.om.NamedShape;
import com.ostrichemulators.semtool.ui.helpers.DefaultGraphShapeRepository;
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

	private static final Map<Shape, Icon> icons = new HashMap<>();
	private final DefaultGraphShapeRepository repo = new DefaultGraphShapeRepository();

	public TableShapeRenderer() {
	}

	public TableShapeRenderer( double sz ) {
		repo.setIconSize( sz );
		for ( NamedShape s : NamedShape.values() ) {
			icons.put( repo.getRawShape( s ), repo.getIcon( s ) );
		}
	}

	public void setSize( double sz ) {
		repo.setIconSize( sz );
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
		Shape s = Shape.class.cast( val );
		icons.put( s, repo.getIcon( Shape.class.cast( val ) ) );
		setIcon( icons.get( s ) );
		return this;
	}
}
