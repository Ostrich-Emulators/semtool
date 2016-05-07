package com.ostrichemulators.semtool.ui.helpers;

import com.ostrichemulators.semtool.om.GraphColorShapeRepository;
import com.ostrichemulators.semtool.om.GraphElement;
import com.ostrichemulators.semtool.om.NamedShape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
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
public class DefaultGraphShapeRepository implements GraphColorShapeRepository {

	private final Logger log = Logger.getLogger( DefaultGraphShapeRepository.class );
	private static final double DEFAULT_SIZE = 24;

	private static final Color COLORS[] = {
		Color.BLUE,
		Color.GREEN,
		Color.RED,
		Color.GRAY,
		Color.MAGENTA,
		Color.YELLOW,
		Color.ORANGE,
		Color.PINK,
		Color.CYAN,
		new Color( 23, 190, 207 ) // aqua
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
	public DefaultGraphShapeRepository() {
		this( DEFAULT_SIZE );
	}

	public DefaultGraphShapeRepository( double sz ) {
		this( sz, 0 );
	}

	public DefaultGraphShapeRepository( double sz, double pad ) {
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
		return shapelkp.getOrDefault( ge, nextShape( ge ) );
	}

	@Override
	public Color getColor( URI ge ) {
		return colorlkp.getOrDefault( ge, nextColor( ge ) );
	}

	private NamedShape nextShape( URI ge ) {
		NamedShape[] shapes = NamedShape.values();
		NamedShape shape = shapes[random.nextInt( shapes.length )];
		shapelkp.put( ge, shape );
		return shape;
	}

	private Color nextColor( URI ge ) {
		Color col = COLORS[random.nextInt( COLORS.length )];
		colorlkp.put( ge, col );
		return col;
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
	public ImageIcon getIcon( Shape shape ){
		BufferedImage img = new BufferedImage( (int) size, (int) size, BufferedImage.TYPE_INT_ARGB );

		// Get the buffered image's graphics context
		Graphics2D g = img.createGraphics();
		g.translate( padding, padding );

		// make it look nice (hopefully)
		g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );
		g.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
		g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

		g.draw( shape );
		g.dispose();

		return new ImageIcon( img );

	}

	@Override
	public ImageIcon getIcon( NamedShape shape ) {
		return getIcon( shape.getShape( size ) );
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
