/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.renderers;

import com.ostrichemulators.semtool.ui.helpers.DefaultGraphShapeRepository;
import com.ostrichemulators.semtool.ui.helpers.DefaultGraphShapeRepository.NamedShape;
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
	private final DefaultGraphShapeRepository repo = new DefaultGraphShapeRepository();

	static {
		BufferedImage img = new BufferedImage( 16, 16, BufferedImage.TYPE_INT_ARGB );
		EMPTY = new ImageIcon( img );
	}

	public ShapeRenderer() {
	}

	public ShapeRenderer( double sz ) {
		repo.setIconSize( sz );
		for ( NamedShape s : DefaultGraphShapeRepository.NamedShape.values() ) {
			icons.put( repo.getShape( s ), repo.createIcon( s ) );
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
		Shape s = Shape.class.cast( val );
		icons.put( s, repo.createIcon( Shape.class.cast( val ) ) );
		setIcon( icons.get( s ) );
		return this;
	}
}
