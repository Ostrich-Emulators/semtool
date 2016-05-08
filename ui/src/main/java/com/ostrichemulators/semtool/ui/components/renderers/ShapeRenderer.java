/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.renderers;

import com.ostrichemulators.semtool.om.NamedShape;
import com.ostrichemulators.semtool.ui.helpers.DefaultColorShapeRepository;
import java.awt.Color;
import java.awt.Component;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;

/**
 *
 * @author ryan
 */
public class ShapeRenderer extends DefaultListCellRenderer {

	private static final ImageIcon EMPTY;
	private static final Map<Shape, Icon> icons = new HashMap<>();
	private final DefaultColorShapeRepository repo = new DefaultColorShapeRepository();

	static {
		BufferedImage img = new BufferedImage( 16, 16, BufferedImage.TYPE_INT_ARGB );
		EMPTY = new ImageIcon( img );
	}

	public ShapeRenderer() {
	}

	public ShapeRenderer( double sz ) {
		repo.setIconSize( sz );
		for ( NamedShape s : NamedShape.values() ) {
			icons.put( repo.getRawShape( s ), repo.getIcon( s ) );
		}
	}

	public void setSize( double sz ) {
		repo.setIconSize( sz );
	}

	@Override
	public Component getListCellRendererComponent( JList<?> list, Object val,
			int index, boolean sel, boolean focus ) {

		if ( null == val ) {
			return super.getListCellRendererComponent( list, val, index, sel, focus );
		}

		super.getListCellRendererComponent( list, "", index, sel, focus );
		Shape s = null;
		if ( val instanceof Shape ) {
			s = Shape.class.cast( val );
		}
		else if ( val instanceof NamedShape ) {
			s = NamedShape.class.cast( val ).getShape( repo.getIconSize() );
		}
		//icons.put( s, repo.getIcon( s ) );
		setIcon( repo.getIcon( s, null, Color.BLACK ) );
		return this;
	}
}
