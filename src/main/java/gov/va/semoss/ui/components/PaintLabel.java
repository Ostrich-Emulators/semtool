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
package gov.va.semoss.ui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;

import java.awt.image.BufferedImage;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * This class is used to paint a text string on a display area.
 */
public class PaintLabel extends JPanel {

	private Shape shape = null;
	private Color color = null;
	private Dimension dim = new Dimension( 20, 20 );
	private final JLabel label = new JLabel( "", null, JLabel.CENTER );
	private final JLabel image = new JLabel( "", null, JLabel.CENTER );

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
		setLayout( new BoxLayout( this, BoxLayout.PAGE_AXIS ) );
		label.setHorizontalTextPosition( JLabel.CENTER );
		label.setAlignmentX( JLabel.CENTER_ALIGNMENT );
		image.setAlignmentX( JLabel.CENTER_ALIGNMENT );

		add( image );
		add( label );

		set( text, s, c );
	}

	public final void setTextAndTooltip( String s ) {
		label.setText( s );
		label.setToolTipText( s );
		image.setToolTipText( s );
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

	private void makeShapeIcon() {
		BufferedImage bi
				= new BufferedImage( dim.width, dim.height, BufferedImage.TYPE_INT_ARGB );
		Graphics2D g = bi.createGraphics();
		g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );
		g.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
		g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

		if ( null != color ) {
			g.setColor( color );
		}

		if ( null != shape ) {
			g.fill( shape );
		}
		g.dispose();

		image.setIcon( new ImageIcon( bi ) );
	}
}
