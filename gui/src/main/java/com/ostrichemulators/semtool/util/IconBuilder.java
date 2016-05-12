/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.util;

import com.ostrichemulators.semtool.om.NamedShape;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class IconBuilder {

	private static final Logger log = Logger.getLogger( IconBuilder.class );
	private final URL imgloc;
	private NamedShape shape;
	private Shape rawshape;
	private Color fill;
	private Color border;
	private double padding;
	private double iconsize;
	private boolean centerAtTL = false;

	public IconBuilder( NamedShape s, Color c ) {
		shape = s;
		fill = c;
		imgloc = null;
	}

	public IconBuilder( NamedShape s ) {
		shape = s;
		imgloc = null;
	}

	public IconBuilder( Color c ) {
		fill = c;
		imgloc = null;
	}

	public IconBuilder( Color fill, Color line ) {
		this.fill = fill;
		border = line;
		imgloc = null;
	}

	public IconBuilder( Shape s, Color c ) {
		rawshape = s;
		fill = c;
		imgloc = null;
	}

	public IconBuilder( Shape s ) {
		rawshape = s;
		imgloc = null;
	}

	public IconBuilder( URL u ) {
		imgloc = u;
	}

	public IconBuilder setPadding( double d ) {
		padding = d;
		return this;
	}

	public IconBuilder setIconSize( double s ) {
		iconsize = ( s < 1 ? 1 : s );
		return this;
	}

	public IconBuilder setCenterAtTopLeft( boolean b ) {
		centerAtTL = b;
		return this;
	}

	public IconBuilder setFill( Color c ) {
		fill = c;
		return this;
	}

	public IconBuilder setStroke( Color c ) {
		border = c;
		return this;
	}

	public ImageIcon build() {
		int pad = (int) ( centerAtTL ? -iconsize / 2 : Math.rint( padding ) );
		if ( null != imgloc ) {
			return external( pad );
		}

		int size = (int) Math.rint( iconsize );
		int shapesize = size - ( 2 * pad );

		Shape myshape = getCorrectlySizedShape( shapesize );

		// Get the buffered image's graphics context
		BufferedImage img = new BufferedImage( size, size, BufferedImage.TYPE_INT_ARGB );
		Graphics2D g = img.createGraphics();
		g.translate( padding, padding );

		// make it look nice (hopefully)
		g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );
		g.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
		g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

		if ( null != fill ) {
			g.setPaint( fill );
			g.fill( myshape );
		}
		if ( null != border ) {
			g.setPaint( border );
			g.draw( myshape );
		}
		g.dispose();

		return new ImageIcon( img );
	}

	private ImageIcon external( int pad ) {
		BufferedImage external = null;
		try ( InputStream is = imgloc.openStream() ) {
			external = ImageIO.read( is );
		}
		catch ( IOException ioe ) {
			log.warn( ioe, ioe );
			external = new BufferedImage( (int) iconsize, (int) iconsize, BufferedImage.TYPE_INT_ARGB );
		}

		int imgwidth;
		int imgheight;
		boolean doscale = false;

		// if no icon size is set, just use icon's original size, but add padding
		if ( iconsize <= 0 ) {
			imgwidth = external.getWidth();
			imgheight = external.getHeight();
		}
		else {
			imgwidth = (int) Math.rint( iconsize - ( 2 * pad ) );
			imgheight = imgwidth;
			doscale = true;
		}

		int totalW = imgwidth + ( 2 * pad );
		int totalH = imgheight + ( 2 * pad );
		BufferedImage img = new BufferedImage( totalW, totalH, BufferedImage.TYPE_INT_ARGB );

		Graphics2D g = img.createGraphics();
		if ( doscale ) {
			g.scale( imgwidth / external.getWidth(), imgheight / external.getHeight() );
		}
		g.drawImage( img, null, pad, pad );
		g.dispose();
		return new ImageIcon( img );
	}

	private Shape getCorrectlySizedShape( int shapesize ) {
		Shape myshape;
		if ( null == shape ) {
			if ( null == rawshape ) {
				// no shape found...create an oval
				myshape = new Ellipse2D.Double( 0, 0, shapesize, shapesize * 0.75 );
			}
			else {
				Rectangle2D rect = rawshape.getBounds2D();
				double scalex = shapesize / rect.getWidth();
				double scaley = shapesize / rect.getHeight();

				AffineTransform at = AffineTransform.getScaleInstance( scalex, scaley );
				myshape = at.createTransformedShape( rawshape );
			}
		}
		else {
			myshape = shape.getShape( shapesize );
		}

		return myshape;
	}
}
