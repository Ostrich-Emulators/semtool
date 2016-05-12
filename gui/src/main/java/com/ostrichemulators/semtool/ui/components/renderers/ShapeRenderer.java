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
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;

/**
 *
 * @author ryan
 */
public class ShapeRenderer extends DefaultListCellRenderer {

	private static final ImageIcon EMPTY;
	private static final Map<Object, ImageIcon> icons = new HashMap<>();
	private double iconsize = -1;

	static {
		BufferedImage img = new BufferedImage( 16, 16, BufferedImage.TYPE_INT_ARGB );
		EMPTY = new ImageIcon( img );
	}

	public ShapeRenderer() {
	}

	public ShapeRenderer( double sz ) {
		iconsize = (int) Math.rint( sz );
	}

	public void setSize( double sz ) {
		iconsize = (int) Math.rint( sz );
	}

	@Override
	public Component getListCellRendererComponent( JList<?> list, Object val,
			int index, boolean sel, boolean focus ) {

		if ( null == val ) {
			return super.getListCellRendererComponent( list, val, index, sel, focus );
		}

		super.getListCellRendererComponent( list, "", index, sel, focus );
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
