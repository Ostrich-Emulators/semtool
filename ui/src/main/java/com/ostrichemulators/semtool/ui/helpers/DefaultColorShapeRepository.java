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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import java.util.Random;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 * The Graph Shape Repository is responsible for serving as a single point of
 * storage and retrieval for NamedShape throughout the SEMOSS system (shapes in
 * graphs and in graph-legends, such as lists and combo-boxes).
 *
 * @author Wayne Warren
 *
 */
public class DefaultColorShapeRepository implements GraphColorShapeRepository {

	private final Logger log = Logger.getLogger( DefaultColorShapeRepository.class );
	private static final double DEFAULT_SIZE = 24;

	private final Random random = new Random();
	private final Map<URI, NamedShape> shapelkp = new HashMap<>();
	private final Map<URI, Color> colorlkp = new HashMap<>();
	private final Map<URI, URL> imglkp = new HashMap<>();
	private final Map<URI, ImageIcon> generatedIcons = new HashMap<>();
	private double padding;
	private double size;
	private boolean saveToPrefs = false;
	private final Preferences prefs = Preferences.userNodeForPackage( getClass() );

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

	public void setSaveToPreferences( boolean b ) {
		if ( b ) {
			Pattern pat = Pattern.compile( "^(.+)_(IMAGE|SHAPE|COLOR)$" );
			try {
				for ( String key : prefs.keys() ) {
					Matcher m = pat.matcher( key );
					if ( m.matches() ) {
						URI uri = new URIImpl( m.group( 1 ) );
						String type = m.group( 2 );
						String val = prefs.get( key, "" );
						switch ( type ) {
							case "IMAGE":
								set( uri, new URL( val ) );
								break;
							case "SHAPE":
								shapelkp.put( uri, NamedShape.valueOf( val ) );
								break;
							case "COLOR":
								String vals[] = val.split( "," );
								colorlkp.put( uri, new Color( Integer.parseInt( vals[0] ),
										Integer.parseInt( vals[1] ), Integer.parseInt( vals[2] ) ) );
								break;
						}
					}
				}
			}
			catch ( BackingStoreException | MalformedURLException | NumberFormatException e ) {
				log.warn( e, e );
			}
		}

		saveToPrefs = b;
	}

	private void trysave( URI uri ) {
		if ( saveToPrefs ) {
			if ( null == uri ) {
				for ( URI u : imglkp.keySet() ) {
					trysave( u );
				}
				for ( URI u : shapelkp.keySet() ) {
					trysave( u );
				}
			}
			else {
				if ( imglkp.containsKey( uri ) ) {
					prefs.put( uri.stringValue() + "_IMAGE", imglkp.get( uri ).toExternalForm() );
				}
				else {
					if ( shapelkp.containsKey( uri ) ) {
						prefs.put( uri.stringValue() + "_SHAPE", shapelkp.get( uri ).toString() );
					}

					if ( colorlkp.containsKey( uri ) ) {
						Color c = colorlkp.get( uri );
						prefs.put( uri.stringValue() + "_COLOR",
								String.format( "%d,%d,%d", c.getRed(), c.getGreen(), c.getBlue() ) );
					}
				}
			}
		}
	}

	@Override
	public void importFrom( GraphColorShapeRepository repo ) {
		shapelkp.putAll( repo.getShapes() );
		colorlkp.putAll( repo.getColors() );
		imglkp.putAll( repo.getIcons() );
		trysave( null );
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
		trysave( ge );
	}

	@Override
	public void set( GraphElement ge, URL imageloc ) {
		set( ge.getURI(), imageloc );
	}

	@Override
	public void set( URI ge, URL imageloc ) {
		imglkp.put( ge, imageloc );
		generatedIcons.remove( ge );
		trysave( ge );
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
			trysave( ge );
		}
		return shapelkp.get( ge );
	}

	@Override
	public Color getColor( URI ge ) {
		if ( !colorlkp.containsKey( ge ) ) {
			Color col = COLORS[random.nextInt( COLORS.length )];
			colorlkp.put( ge, col );
			trysave( ge );
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

	@Override
	public ImageIcon getIcon( Color fill ) {
		BufferedImage img = new BufferedImage( (int) size, (int) size, BufferedImage.TYPE_INT_ARGB );
		Graphics2D g = img.createGraphics();
		g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );
		g.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
		g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

		g.setColor( fill );
		int h = ( size > 8 ? (int) size - 8 : (int) size );
		int w = ( size > 4 ? (int) size - 4 : (int) size );
		g.fillOval( 0, 2, w, h );
		g.dispose();
		return new ImageIcon( img );
	}
}
