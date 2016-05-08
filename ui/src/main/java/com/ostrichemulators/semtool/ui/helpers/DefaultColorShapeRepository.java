package com.ostrichemulators.semtool.ui.helpers;

import com.ostrichemulators.semtool.om.GraphColorShapeRepository;
import com.ostrichemulators.semtool.om.GraphElement;
import com.ostrichemulators.semtool.om.NamedShape;

import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import java.util.Random;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

	private final Random random = new Random();
	private final Map<URI, NamedShape> shapelkp = new HashMap<>();
	private final Map<URI, Color> colorlkp = new HashMap<>();
	private final Map<URI, URL> imglkp = new HashMap<>();
	private boolean saveToPrefs = false;
	private final Preferences prefs = Preferences.userNodeForPackage( getClass() );

	public DefaultColorShapeRepository() {
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
	public void set( GraphElement ge, Color color, NamedShape shape ) {
		set( ge.getURI(), color, shape );
	}

	@Override
	public void set( URI ge, Color color, NamedShape shape ) {
		shapelkp.put( ge, shape );
		colorlkp.put( ge, color );
		trysave( ge );
	}

	@Override
	public void set( GraphElement ge, URL imageloc ) {
		set( ge.getURI(), imageloc );
	}

	@Override
	public void set( URI ge, URL imageloc ) {
		imglkp.put( ge, imageloc );
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
	public boolean hasShape( URI uri ) {
		return shapelkp.containsKey( uri );
	}
}
