package com.ostrichemulators.semtool.ui.helpers;

import com.ostrichemulators.semtool.om.GraphColorShapeRepository;
import com.ostrichemulators.semtool.om.GraphElement;
import com.ostrichemulators.semtool.om.NamedShape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;

/**
 * The Graph Shape Repository is responsible for serving as a single point of
 * storage and retrieval for NamedShape throughout the SEMOSS system (shapes in
 * graphs and in graph-legends, such as lists and combo-boxes).
 *
 * @author Wayne Warren
 *
 */
public class DefaultColorShapeRepository implements GraphColorShapeRepository {

	private final Logger log = Logger.getLogger(DefaultColorShapeRepository.class );
	private static final double DEFAULT_SIZE = 24;

	public static final Color COLORS[] = {
		new Color( 31, 119, 180 ), // blue
		new Color( 44, 160, 44 ), // green
		new Color( 214, 39, 40 ), // red
		new Color( 143, 99, 42 ), // brown
		new Color( 254, 208, 2 ), // yellow
		new Color( 255, 127, 14 ), // orange
		new Color( 148, 103, 189 ), // purple
		new Color( 23, 190, 207 ), // aqua
		new Color( 241, 47, 158 ), // pink
		Color.GRAY
	};

	private final Random random = new Random();
	private final Map<URI, NamedShape> shapelkp = new HashMap<>();
	private final Map<URI, Color> colorlkp = new HashMap<>();
	private final Map<URI, URL> imglkp = new HashMap<>();
	private final Map<URI, ImageIcon> generatedIcons = new HashMap<>();
	private double padding;
	private double size;

	/**
	 * Default constructor
	 */
	public DefaultColorShapeRepository() {
		this( DEFAULT_SIZE );
	}

	public DefaultColorShapeRepository( double sz ) {
		this( sz, 0 );
	}

	public DefaultColorShapeRepository( double sz, double pad ) {
		size = sz;
		padding = pad;
	}

	@Override
	public void setIconSize( double sz ) {
		size = sz;
	}

	@Override
	public void setIconPadding( double pad ) {
		padding = pad;
	}

	@Override
	public double getIconSize() {
		return size;
	}

	@Override
	public double getIconPadding() {
		return padding;
	}

	@Override
	public void set( GraphElement ge, Color color, NamedShape shape ) {
		set( ge.getURI(), color, shape );
	}

	@Override
	public void set( URI ge, Color color, NamedShape shape ) {
		shapelkp.put( ge, shape );
		colorlkp.put( ge, color );
		generatedIcons.remove( ge );
	}

	@Override
	public void set( GraphElement ge, URL imageloc ) {
		set( ge.getURI(), imageloc );
	}

	@Override
	public void set( URI ge, URL imageloc ) {
		imglkp.put( ge, imageloc );
		generatedIcons.remove( ge );
	}

	@Override
	public NamedShape getShape( GraphElement ge ) {
		return ( shapelkp.containsKey( ge.getURI() )
				? getShape( ge.getURI() )
				: getShape( ge.getType() ) );
	}

	@Override
	public Color getColor( GraphElement ge ) {
		return ( colorlkp.containsKey( ge.getURI() )
				? getColor( ge.getURI() )
				: getColor( ge.getType() ) );
	}

	@Override
	public NamedShape getShape( URI ge ) {
		if ( !shapelkp.containsKey( ge ) ) {
			NamedShape[] shapes = NamedShape.values();
			NamedShape shape = shapes[random.nextInt( shapes.length )];
			shapelkp.put( ge, shape );
		}
		return shapelkp.get( ge );
	}

	@Override
	public Color getColor( URI ge ) {
		if ( !colorlkp.containsKey( ge ) ) {
			Color col = COLORS[random.nextInt( COLORS.length )];
			colorlkp.put( ge, col );
		}

		return colorlkp.get( ge );
	}

	@Override
	public Map<URI, Color> getColors() {
		return new HashMap<>( colorlkp );
	}

	@Override
	public Map<URI, NamedShape> getShapes() {
		return new HashMap<>( shapelkp );
	}

	@Override
	public Map<URI, URL> getIcons() {
		return new HashMap<>( imglkp );
	}

	@Override
	public ImageIcon getIcon( GraphElement ge ) {
		return ( generatedIcons.containsKey( ge.getURI() )
				? getIcon( ge.getURI() )
				: getIcon( ge.getType() ) );
	}

	@Override
	public ImageIcon getIcon( URI uri ) {
		return generatedIcons.getOrDefault( uri, genIcon( uri ) );
	}

	@Override
	public ImageIcon getIcon( Shape shape, Color fill, Color line ) {
		BufferedImage img = new BufferedImage( (int) size, (int) size, BufferedImage.TYPE_INT_ARGB );

		Rectangle2D rect = shape.getBounds2D();
		double side = rect.getWidth();
		double maxshapesize = size - ( 2 * padding );

		// Get the buffered image's graphics context
		Graphics2D g = img.createGraphics();
		if ( side > maxshapesize ) {
			g.scale( maxshapesize / side, maxshapesize / side );
		}

		g.translate( padding, padding );

		// make it look nice (hopefully)
		g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );
		g.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
		g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

		if ( null != fill ) {
			g.setPaint( fill );
			g.fill( shape );
		}
		if ( null != line ) {
			g.setPaint( line );
			g.draw( shape );
		}
		g.dispose();

		return new ImageIcon( img );

	}

	@Override
	public ImageIcon getIcon( NamedShape shape ) {
		return getIcon( shape.getShape( size ), null, Color.BLACK );
	}

	@Override
	public ImageIcon getIcon( URI uri, double sz ) {
		ImageIcon old = getIcon( uri );

		double scale = sz / old.getIconWidth();
		if ( scale != 1.0 ) {
			BufferedImage img = new BufferedImage( (int) sz, (int) sz,
					BufferedImage.TYPE_INT_ARGB );

			// Get the buffered image's graphics context
			Graphics2D g = img.createGraphics();
			g.scale( scale, scale );

			// make it look nice (hopefully)
			g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );
			g.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
			g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

			Color col = getColor( uri );
			NamedShape shape = getShape( uri );
			g.setPaint( col );
			g.draw( shape.getShape( size ) );
			g.dispose();

			return new ImageIcon( img );
		}

		// no changes, just return the original
		return old;
	}

	private ImageIcon genIcon( URI uri ) {
		if ( imglkp.containsKey( uri ) ) {
			try ( InputStream is = imglkp.get( uri ).openStream() ) {
				return new ImageIcon( ImageIO.read( is ) );
			}
			catch ( Exception e ) {
				log.warn( e, e );
			}
		}

		return getIcon( getShape( uri ) );
	}

	@Override
	public Shape getRawShape( GraphElement ge ) {
		return getRawShape( getShape( ge ) );
	}

	@Override
	public Shape getRawShape( URI uri ) {
		return getRawShape( getShape( uri ) );
	}

	@Override
	public Shape getRawShape( NamedShape ns ) {
		return ns.getShape( size );
	}

	@Override
	public boolean hasShape( URI uri ) {
		return shapelkp.containsKey( uri );
	}
}
