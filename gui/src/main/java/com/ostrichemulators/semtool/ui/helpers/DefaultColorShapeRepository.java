package com.ostrichemulators.semtool.ui.helpers;

import com.ostrichemulators.semtool.om.GraphColorShapeRepository;
import com.ostrichemulators.semtool.om.GraphColorShapeRepositoryListener;
import com.ostrichemulators.semtool.om.GraphElement;
import com.ostrichemulators.semtool.om.NamedShape;

import com.ostrichemulators.semtool.ui.preferences.StoredMetadata;
import java.awt.Color;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import java.util.Random;
import java.util.Set;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDFS;

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
	private static final double DEFAULT_ICON_SIZE = 16;

	private final Random random = new Random();
	private final Map<URI, NamedShape> shapelkp = new HashMap<>();
	private final Map<URI, Color> colorlkp = new HashMap<>();
	private final Map<URI, URL> imglkp = new HashMap<>();
	private final List<GraphColorShapeRepositoryListener> listenees = new ArrayList<>();
	private boolean saveOnChange = false;
	private double iconsize = DEFAULT_ICON_SIZE;
	private StoredMetadata persistance;

	public DefaultColorShapeRepository() {
		shapelkp.put( RDFS.CLASS, NamedShape.URCHIN );
		colorlkp.put( RDFS.CLASS, Color.PINK );
		shapelkp.put( OWL.CLASS, NamedShape.URCHIN );
		colorlkp.put( OWL.CLASS, Color.PINK );
	}

	public void saveTo( URI database, StoredMetadata pers ) {
		persistance = pers;
		saveOnChange = ( null != persistance );
	}

	private void trysave( URI... uris ) {
		if ( saveOnChange ) {
			Set<URI> tosave = new HashSet<>( Arrays.asList( uris ) );
			if ( 0 == uris.length ) {
				tosave.addAll( imglkp.keySet() );
				tosave.addAll( shapelkp.keySet() );
				tosave.addAll( imglkp.keySet() );
			}

			for ( URI uri : tosave ) {
				try {
					if ( imglkp.containsKey( uri ) ) {
						persistance.set( null, uri, StoredMetadata.GRAPH_ICON,
								new URIImpl( imglkp.get( uri ).toExternalForm() ) );
					}
					else {
						try {
							if ( shapelkp.containsKey( uri ) ) {
								persistance.set( null, uri, shapelkp.get( uri ) );
							}
							if ( colorlkp.containsKey( uri ) ) {
								persistance.set( null, uri, colorlkp.get( uri ) );
							}
						}
						catch ( Exception e ) {
							log.warn( e );
						}
					}
				}
				catch ( Exception e ) {

				}
			}
		}

		for ( GraphColorShapeRepositoryListener l : listenees ) {
			l.dataChanged( null, null, null, null );
		}
	}

	@Override
	public void importFrom( GraphColorShapeRepository repo ) {
		shapelkp.putAll( repo.getShapes() );
		colorlkp.putAll( repo.getColors() );
		imglkp.putAll( repo.getIcons() );
		iconsize = repo.getIconSize();
		trysave();
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
		return getShape( ge.getType(), ge.getURI() );
	}

	@Override
	public NamedShape getShape( URI type, URI instance ) {
		if ( shapelkp.containsKey( instance ) ) {
			return shapelkp.get( instance );
		}

		return getShape( type );
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
	public void set( Collection<GraphElement> ges, Color color, NamedShape shape ) {
		List<URI> saves = new ArrayList<>();
		for ( GraphElement ge : ges ) {
			URI u = ge.getURI();
			saves.add( u );
			shapelkp.put( u, shape );
			colorlkp.put( u, color );
		}
		trysave( saves.toArray( new URI[0] ) );
	}

	@Override
	public void addListener( GraphColorShapeRepositoryListener l ) {
		listenees.add( l );
	}

	@Override
	public void removeListener( GraphColorShapeRepositoryListener l ) {
		listenees.remove( l );
	}

	@Override
	public double getIconSize() {
		return iconsize;
	}

	@Override
	public void setIconSize( double d ) {
		iconsize = d;
	}

	@Override
	public void set( URI ge, NamedShape s ) {
		shapelkp.put( ge, s );
	}

	@Override
	public void set( URI ge, Color c ) {
		colorlkp.put( ge, c );
	}

	@Override
	public Color getColor( URI type, URI instance ) {
		if ( colorlkp.containsKey( instance ) ) {
			return colorlkp.get( instance );
		}

		return getColor( type );
	}

	@Override
	public URL getUrl( URI ge ) {
		return imglkp.get( ge );
	}

	@Override
	public URL getUrl( URI type, URI instance ) {
		if ( imglkp.containsKey( type ) ) {
			return imglkp.get( type );
		}
		return imglkp.get( instance );
	}
}
