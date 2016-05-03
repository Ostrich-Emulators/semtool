/**
 * *****************************************************************************
 * Copyright 2013 SEMOSS.ORG
 *
 * This file is part of SEMOSS.
 *
 * SEMOSS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SEMOSS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SEMOSS. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package com.ostrichemulators.semtool.ui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 * This class is used to paint a text string on a display area.
 */
public class PaintLabel extends JButton {

	private static final long serialVersionUID = 990020151L;
	private Shape shape = null;
	private Color color = null;
	private Dimension dim = new Dimension( 20, 20 );

	/**
	 * Constructor for PaintLabel.
	 *
	 * @param text Text string to be painted.
	 */
	public PaintLabel( String text ) {
		this( text, null, null );
	}

	public PaintLabel( String text, Shape s, Color c ) {
		super();
		setHorizontalTextPosition( JButton.CENTER );
		setVerticalTextPosition( JButton.BOTTOM );
		set( text, s, c );
		setContentAreaFilled( false );
	}

	public final void setTextAndTooltip( String s ) {
		setText( s );
		setToolTipText( s );
	}

	/**
	 * Sets the shape of the display area.
	 *
	 * @param shape Shape.
	 */
	public void setShape( Shape shape ) {
		this.shape = shape;
		makeShapeIcon();
	}

	/**
	 * Sets the color of the display area.
	 *
	 * @param color Color to be displayed.
	 */
	public void setColor( Color color ) {
		this.color = color;
		makeShapeIcon();
	}

	public final void set( String txt, Shape s, Color c ) {
		setTextAndTooltip( txt );
		shape = s;
		color = c;
		makeShapeIcon();
	}

	public void setIconDimension( Dimension d ) {
		dim = d;
		makeShapeIcon();
	}

	/**
	 * Create a vector-graphics based Shape Icon for display in a JLabel or
	 * JButton
	 *
	 * @param color The color of the fill of the Shape Icon
	 * @param shape The awt.Shape that the fill color is to occupy
	 * @param dim The desired dimensions of the icon
	 * @return
	 */
	public static ImageIcon makeShapeIcon( Color color, Shape shape, Dimension dim ) {
		// Set some padding around the ImageIcon, for better display
		int padding = 2;
		// Get the bounds of the shape, for use in subsequent calculations
		Rectangle rect = shape.getBounds();
		// Add the padding (2 sides) to the height and width around the icon
		int totalWidth = dim.width + ( padding * 2 );
		int totalHeight = dim.height + ( padding * 2 );
		// Create the Buffered Image, using the bounds of the Shape instance it will contain, plus the padding
		BufferedImage bi = new BufferedImage( totalWidth, totalHeight, BufferedImage.TYPE_INT_ARGB );
		// Get the buffered image's graphics context
		Graphics2D g = bi.createGraphics();
		// If the origin of the shape is offset from zero (this shouldn't happen), then translate the x and y to compensate
		// to compensate
		int x = 0;
		int y = 0;
		if ( rect.x != 0 ) {
			x = ( rect.x ) * ( -1 );
		}
		if ( rect.y != 0 ) {
			y = ( rect.y ) * ( -1 );
		}
		// Move the graphics to compensate for the x and y offset found in the shape, above
		g.translate( x, y );
		// Move the graphics cursor to account for the padding
		g.translate( padding, padding );
		// If the dim requested is different from the shape extents, apply a scale
		double requestedWidth = dim.getWidth();
		double requestedHeight = dim.getHeight();
		double scaleX = 1.0d;
		double scaleY = 1.0d;
		if ( requestedWidth != ( (double) rect.width ) ) {
			scaleX = requestedWidth / ( (double) rect.width );
		}
		if ( requestedHeight != ( (double) rect.height ) ) {
			scaleY = requestedHeight / ( (double) rect.width );
		}
		// Apply the scale
		g.scale( scaleX, scaleY );
		g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );
		g.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
		g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

		if ( null != color ) {
			g.setColor( color );
		}

		g.fill( shape );
		g.dispose();

		return new ImageIcon( bi );
	}

	private void makeShapeIcon() {
		ImageIcon ii = makeShapeIcon( color, shape, dim );
		setIcon( ii );
	}
}
