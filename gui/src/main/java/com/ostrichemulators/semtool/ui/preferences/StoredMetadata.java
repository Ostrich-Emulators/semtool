/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.preferences;

import com.ostrichemulators.semtool.om.GraphColorShapeRepository;
import com.ostrichemulators.semtool.om.NamedShape;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.engine.api.QueryExecutor;
import com.ostrichemulators.semtool.rdf.engine.impl.AbstractSesameEngine;
import com.ostrichemulators.semtool.rdf.query.util.impl.OneValueQueryAdapter;
import com.ostrichemulators.semtool.rdf.query.util.impl.OneVarListQueryAdapter;
import com.ostrichemulators.semtool.ui.helpers.DefaultColorShapeRepository;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.Utility;
import info.aduna.iteration.Iterations;
import java.awt.Color;
import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

/**
 * A class to store data about individual databases (things like graph icons,
 * colors, insight locations, reification data).
 *
 * @author ryan
 */
public class StoredMetadata {

	private static final Logger log = Logger.getLogger( StoredMetadata.class );
	private RepositoryConnection rc;
	private ValueFactory vf;
	private URI PIN_SUB = Utility.makeInternalUri( "pin-locs" );
	public static final URI PIN = Utility.makeInternalUri( "pinned" );
	public static final URI INSIGHT_LOC = Utility.makeInternalUri( "insight-loc" );
	public static final URI GRAPH_COLOR = Utility.makeInternalUri( "graph-color" );
	public static final URI GRAPH_SHAPE = Utility.makeInternalUri( "graph-shape" );
	public static final URI GRAPH_ICON = Utility.makeInternalUri( "graph-icon" );

	public StoredMetadata( File datadir ) {
		try {
			MemoryStore store = new MemoryStore( datadir );
			store.setSyncDelay( 2000 ); // every two seconds
			SailRepository repo = new SailRepository( store );
			repo.initialize();
			rc = repo.getConnection();
			vf = rc.getValueFactory();

			Runtime.getRuntime().addShutdownHook( new Thread( new Runnable() {

				@Override
				public void run() {
					try {
						rc.close();
					}
					catch ( Exception e ) {
						log.warn( e );
					}
					try {
						rc.getRepository().shutDown();
					}
					catch ( Exception e ) {
						log.warn( e );
					}
				}
			} ) );
		}
		catch ( Exception e ) {
			log.error( e, e );
		}
	}

	public void pin( IEngine engine, boolean pin ) {
		set( null, engine.getBaseUri(), PIN, pin );

		Set<String> pins = getPinnedLocations();
		String loc = engine.getProperty( Constants.SMSS_LOCATION );
		if ( pin ) {
			pins.add( loc );
		}
		else {
			pins.remove( loc );
		}

		set( null, PIN_SUB, RDFS.LABEL, pins.toArray( new String[0] ) );
	}

	public boolean isPinned( URI database ) {
		return getBool( null, database, PIN, false );
	}

	public Set<String> getPinnedLocations() {
		return new HashSet<>( getStrings( null, PIN_SUB, RDFS.LABEL ) );
	}

	public Set<String> getInsightLocations( URI database ) {
		return getStrings( null, database, INSIGHT_LOC );
	}

	public void setInsightLocations( URI database, String... locs ) {
		set( null, database, INSIGHT_LOC, locs );
	}

	public void set( URI database, URI uri, Color col ) {
		set( database, uri, GRAPH_COLOR, String.format( "%d,%d,%d",
				col.getRed(), col.getGreen(), col.getBlue() ) );
	}

	public Color get( URI database, URI uri, Color defaultval ) {
		String col = getString( database, uri, GRAPH_COLOR, "" );

		Color color = defaultval;
		Pattern PAT = Pattern.compile( "(\\d+),(\\d+),(\\d)" );
		Matcher m = PAT.matcher( col );
		if ( m.matches() ) {
			color = new Color( Integer.parseInt( m.group( 1 ) ),
					Integer.parseInt( m.group( 2 ) ), Integer.parseInt( m.group( 3 ) ) );
		}

		return color;
	}

	public void set( URI database, URI uri, NamedShape ns ) {
		set( database, uri, GRAPH_SHAPE, ns.toString() );
	}

	public NamedShape getShape( URI database, URI uri, NamedShape defaultval ) {
		String str = getString( database, uri, GRAPH_SHAPE, "" );
		return ( str.isEmpty() ? defaultval : NamedShape.valueOf( str ) );
	}

	public GraphColorShapeRepository getCSRepo( URI database ) {
		DefaultColorShapeRepository repo = new DefaultColorShapeRepository();
		try {
			List<Statement> stmts = Iterations.asList( rc.getStatements( null,
					GRAPH_SHAPE, null, false, database ) );
			for ( Statement s : stmts ) {
				repo.set( URI.class.cast( s.getSubject() ),
						NamedShape.valueOf( s.getObject().stringValue() ) );
			}

			stmts = Iterations.asList( rc.getStatements( null,
					GRAPH_COLOR, null, false, database ) );
			for ( Statement s : stmts ) {
				String vals[] = s.getObject().stringValue().split( "," );
				Color color = new Color( Integer.parseInt( vals[0] ),
						Integer.parseInt( vals[1] ), Integer.parseInt( vals[2] ) );
				repo.set( URI.class.cast( s.getSubject() ), color );
			}

			stmts = Iterations.asList( rc.getStatements( null,
					GRAPH_ICON, null, false, database ) );
			for ( Statement s : stmts ) {
				try {
					repo.set( URI.class.cast( s.getSubject() ),
							new URL( s.getObject().stringValue() ) );
				}
				catch ( Exception x ) {
					log.warn( x );
				}
			}

		}
		catch ( RepositoryException e ) {
			log.error( e, e );
		}

		return repo;
	}

	private void set( URI ctx, URI dbid, URI pref, boolean bool ) {
		try {
			rc.begin();
			rc.remove( dbid, pref, null );
			rc.add( new ContextStatementImpl( dbid, pref, vf.createLiteral( bool ), ctx ) );
			rc.commit();
		}
		catch ( Exception e ) {
			try {
				rc.rollback();
			}
			catch ( Exception ee ) {
				log.warn( ee, ee );
			}
			log.error( e, e );
		}
	}

	public void set( URI ctx, URI dbid, URI pref, URI... uris ) {
		try {
			rc.begin();
			rc.remove( dbid, pref, null );

			if ( !( null == uris || 0 == uris.length ) ) {
				for ( URI u : uris ) {
					rc.add( new ContextStatementImpl( dbid, pref, u, ctx ) );
				}
			}
			rc.commit();
		}
		catch ( Exception e ) {
			try {
				rc.rollback();
			}
			catch ( Exception ee ) {
				log.warn( ee, ee );
			}
			log.error( e, e );
		}
	}

	public void set( URI ctx, URI dbid, URI pref, String... val ) {
		try {
			rc.begin();
			rc.remove( dbid, pref, null );
			for ( String s : val ) {
				rc.add( new ContextStatementImpl( dbid, pref, vf.createLiteral( s ), ctx ) );
			}
			rc.commit();
		}
		catch ( Exception e ) {
			try {
				rc.rollback();
			}
			catch ( Exception ee ) {
				log.warn( ee, ee );
			}
			log.error( e, e );
		}
	}

	private <X> X get( URI ctx, URI dbid, URI pref, QueryExecutor<X> qa ) {
		qa.bind( "db", dbid );
		qa.bind( "pred", pref );
		qa.setContext( ctx );
		return AbstractSesameEngine.getSelectNoEx( qa, rc, true );
	}

	public boolean getBool( URI ctx, URI dbid, URI pref, boolean defaultval ) {
		Boolean b = get( ctx, dbid, pref,
				OneValueQueryAdapter.getBoolean( "SELECT ?val WHERE { ?db ?pred ?val }" ) );

		return ( null == b ? defaultval : b );
	}

	public String getString( URI ctx, URI dbid, URI pref, String defaultval ) {
		String str = get( ctx, dbid, pref,
				OneValueQueryAdapter.getString( "SELECT ?val WHERE { ?db ?pred ?val }" ) );
		return ( null == str ? defaultval : str );
	}

	public Set<String> getStrings( URI ctx, URI dbid, URI pref ) {
		return new HashSet<>( get( ctx, dbid, pref,
				OneVarListQueryAdapter.getStringList( "SELECT ?val WHERE { ?db ?pred ?val }" ) ) );
	}

	public Set<URI> getUris( URI ctx, URI dbid, URI pref ) {
		return new HashSet<>( get( ctx, dbid, pref,
				OneVarListQueryAdapter.getUriList( "SELECT ?val WHERE { ?db ?pred ?val }" ) ) );
	}

	public URI getUri( URI ctx, URI dbid, URI pref ) {
		return get( ctx, dbid, pref,
				OneValueQueryAdapter.getUri( "SELECT ?val WHERE { ?db ?pred ?val }" ) );
	}
}
