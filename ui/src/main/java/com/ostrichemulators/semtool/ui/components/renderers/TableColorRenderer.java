/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.renderers;

import com.ostrichemulators.semtool.om.GraphColorRepository;
import com.ostrichemulators.semtool.util.Constants;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author ryanI
 */
public class TableColorRenderer extends DefaultTableCellRenderer {

	private static final ImageIcon EMPTY;
	private static final Map<Color, Icon> icons = new HashMap<>();

	static {
		BufferedImage img = new BufferedImage( 18, 18, BufferedImage.TYPE_INT_ARGB );
		EMPTY = new ImageIcon( img );
	}

	@Override
	public Component getTableCellRendererComponent( JTable t, Object val,
			boolean sel, boolean focus, int r, int c ) {

		if ( null == val ) {
			val = GraphColorRepository.instance().getColor( Constants.TRANSPARENT );
		}

		Color color = Color.class.cast( val );
		super.getTableCellRendererComponent( t, "", sel, focus, r, c );
		colorify( this, color );

		return this;
	}

	public static void colorify( JLabel lbl, Color s ) {
		if ( null == s ) {
			lbl.setIcon( null );
		}
		else {
//			lbl.setBackground( s );
//		}
			if ( !icons.containsKey( s ) ) {
				BufferedImage img = new BufferedImage( 18, 18, BufferedImage.TYPE_INT_ARGB );
				Graphics2D g = img.createGraphics();
				g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );
				g.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
				g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

				g.setColor( s );
				g.fillOval( 0, 2, 18, 14 );
				g.dispose();

				icons.put( s, new ImageIcon( img ) );
			}
			lbl.setIcon( icons.get( s ) );
		}
	}
}
