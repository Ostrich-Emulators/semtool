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
package com.ostrichemulators.semtool.ui.components.playsheets.graphsupport;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
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
	private Dimension dim = new Dimension( 30, 30 );

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

		double imgwidth = dim.getWidth() - ( 2 * padding );
		double imgheight = dim.getHeight() - ( 2 * padding );

		Rectangle2D shapebounds = shape.getBounds2D();

		BufferedImage bi = new BufferedImage( dim.width, dim.height,
				BufferedImage.TYPE_INT_ARGB );
		// Get the buffered image's graphics context
		Graphics2D g = bi.createGraphics();
		g.translate( padding, padding );

		// Apply the scale
		g.scale( imgwidth / shapebounds.getWidth(), imgheight / shapebounds.getHeight() );
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
