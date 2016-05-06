/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.renderers;

import com.ostrichemulators.semtool.ui.helpers.GraphShapeRepository;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Shape;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author ryan
 */
public class TableShapeRenderer extends DefaultTableCellRenderer {

	private final GraphShapeRepository repo = new GraphShapeRepository();

	@Override
	public Component getTableCellRendererComponent( JTable table, Object val,
			boolean isSelected, boolean hasFocus, int row, int column ) {

		if ( null == val ) {
			return super.getTableCellRendererComponent( table, val, isSelected,
					hasFocus, row, column );
		}

		String s = repo.getShapeName( Shape.class.cast( val ) );
		return super.getTableCellRendererComponent( table, s, isSelected,
				hasFocus, row, column );
	}

	public static void shapeify( JLabel lbl, Shape s, Dimension d ) {
//		if ( null == s ) {
//			lbl.setIcon( EMPTY );
//		}
//		else {
//			if ( !icons.containsKey( s ) || true ) {
//				BufferedImage img
//						= new BufferedImage( 22, 22, BufferedImage.TYPE_INT_ARGB );
//				Graphics2D g = img.createGraphics();
//				g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );
//				g.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
//				g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
//
//				double scalex = d.width/22d;
//				double scaley = d.height/22d;
//				
//				g.scale( scalex, scaley );
//				
//				g.setColor( Color.BLACK );
//				g.draw( s );
//				g.dispose();
//
//				icons.put( s, new ImageIcon( img ) );
//			}
//		}
//		lbl.setIcon( icons.get( s ) );
	}

}
