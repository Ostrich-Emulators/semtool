/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.preferences;

import com.ostrichemulators.semtool.om.GraphColorShapeRepository;
import com.ostrichemulators.semtool.om.NamedShape;
import com.ostrichemulators.semtool.rdf.engine.api.InsightManager;
import com.ostrichemulators.semtool.rdf.engine.api.QueryExecutor;
import com.ostrichemulators.semtool.rdf.engine.impl.AbstractSesameEngine;
import com.ostrichemulators.semtool.rdf.engine.impl.InsightManagerImpl;
import com.ostrichemulators.semtool.rdf.query.util.impl.OneValueQueryAdapter;
import com.ostrichemulators.semtool.rdf.query.util.impl.OneVarListQueryAdapter;
import com.ostrichemulators.semtool.ui.helpers.DefaultColorShapeRepository;
import com.ostrichemulators.semtool.user.LocalUserImpl;
import com.ostrichemulators.semtool.util.Utility;
import info.aduna.iteration.Iterations;
import java.awt.Color;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.contextaware.ContextAwareConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

/**
 * A class to store data about individual databases (things like graph icons,
 * colors, insight locations, reification data). This class if different from
 * the standard Preferences object in that it is intended for per-database
 * settings instead of UI-specific user preferences.
 *
 * @author ryan
 */
public class StoredMetadata {

	private static final Logger log = Logger.getLogger( StoredMetadata.class );
	private RepositoryConnection rc;
	private ValueFactory vf;

	public static final URI INSIGHT_LOC = Utility.makeInternalUri( "insight-loc" );
	public static final URI GRAPH_COLOR = Utility.makeInternalUri( "graph-color" );
	public static final URI GRAPH_SHAPE = Utility.makeInternalUri( "graph-shape" );
	public static final URI GRAPH_ICON = Utility.makeInternalUri( "graph-icon" );

	public StoredMetadata( File datadir ) {
		try {
			MemoryStore store = new MemoryStore( datadir );
			store.setSyncDelay( 2000 ); // every two seconds (arbitrary)
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

	public Set<URI> getDatabases() {
		Set<URI> set = new HashSet<>();
		try {
			for ( Resource r : Iterations.asList( rc.getContextIDs() ) ) {
				set.add( URI.class.cast( r ) );
			}
		}
		catch ( RepositoryException e ) {
			log.error( e, e );
		}

		return set;
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
				catch ( MalformedURLException x ) {
					log.warn( x );
				}
			}
		}
		catch ( RepositoryException e ) {
			log.error( e, e );
		}

		return repo;
	}

	public void clearGraphSettings( URI database ) {
		try {
			rc.remove( rc.getStatements( null, GRAPH_SHAPE, null, false, database ) );
			rc.remove( rc.getStatements( null, GRAPH_COLOR, null, false, database ) );
			rc.remove( rc.getStatements( null, GRAPH_ICON, null, false, database ) );
		}
		catch ( RepositoryException re ) {
			log.error( re, re );
		}
	}

	public void set( URI database, GraphColorShapeRepository repo ) {
		clearGraphSettings( database );

		for ( Map.Entry<URI, NamedShape> en : repo.getShapes().entrySet() ) {
			set( database, en.getKey(), en.getValue() );
		}
		for ( Map.Entry<URI, Color> en : repo.getColors().entrySet() ) {
			set( database, en.getKey(), en.getValue() );
		}
		for ( Map.Entry<URI, URL> en : repo.getIcons().entrySet() ) {
			set( database, en.getKey(), GRAPH_ICON,
					new URIImpl( en.getValue().toExternalForm() ) );
		}
	}

	private void set( URI ctx, URI subj, URI pref, boolean bool ) {
		try {
			rc.begin();
			rc.remove( subj, pref, null, ctx );
			rc.add( new ContextStatementImpl( subj, pref, vf.createLiteral( bool ), ctx ) );
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

	public void set( URI ctx, URI subj, URI pref, URI... uris ) {
		try {
			rc.begin();
			rc.remove( subj, pref, null, ctx );

			if ( !( null == uris || 0 == uris.length ) ) {
				for ( URI u : uris ) {
					rc.add( new ContextStatementImpl( subj, pref, u, ctx ) );
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

	public void set( URI ctx, URI subj, URI pref, String... val ) {
		try {
			rc.begin();
			rc.remove( subj, pref, null, ctx );
			for ( String s : val ) {
				rc.add( new ContextStatementImpl( subj, pref, vf.createLiteral( s ), ctx ) );
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

	private <X> X get( URI ctx, URI subj, URI pref, QueryExecutor<X> qa ) {
		qa.bind( "db", subj );
		qa.bind( "pred", pref );
		qa.setContext( ctx );
		return AbstractSesameEngine.getSelectNoEx( qa, rc, true );
	}

	public boolean getBool( URI ctx, URI uri, URI pref, boolean defaultval ) {
		Boolean b = get( ctx, uri, pref,
				OneValueQueryAdapter.getBoolean( "SELECT ?val WHERE { ?db ?pred ?val }" ) );

		return ( null == b ? defaultval : b );
	}

	public String getString( URI ctx, URI uri, URI pref, String defaultval ) {
		String str = get( ctx, uri, pref,
				OneValueQueryAdapter.getString( "SELECT ?val WHERE { ?db ?pred ?val }" ) );
		return ( null == str ? defaultval : str );
	}

	public Set<String> getStrings( URI ctx, URI uri, URI pref ) {
		return new HashSet<>( get( ctx, uri, pref,
				OneVarListQueryAdapter.getStringList( "SELECT ?val WHERE { ?db ?pred ?val }" ) ) );
	}

	public Set<URI> getUris( URI ctx, URI uri, URI pref ) {
		return new HashSet<>( get( ctx, uri, pref,
				OneVarListQueryAdapter.getUriList( "SELECT ?val WHERE { ?db ?pred ?val }" ) ) );
	}

	public URI getUri( URI ctx, URI uri, URI pref ) {
		return get( ctx, uri, pref,
				OneValueQueryAdapter.getUri( "SELECT ?val WHERE { ?db ?pred ?val }" ) );
	}

	public InsightManager getLocalInsightManager( URI database ) {
		InsightManagerImpl imi = new InsightManagerImpl();
		ContextAwareConnection imirc = null;
		try {
			imirc = new ContextAwareConnection( rc );
			imirc.setReadContexts( getInsightContext( database ) );
			imi.loadFromRepository( imirc );
		}
		catch ( RepositoryException re ) {
			log.warn( re, re );
		}

		return imi;
	}

	public void setLocalInsights( URI database, InsightManager imi ) {
		Model model = InsightManagerImpl.getModel( imi, new LocalUserImpl() );

		ContextAwareConnection imirc = null;
		try {
			URI realctx = getInsightContext( database );
			imirc = new ContextAwareConnection( rc );
			imirc.setReadContexts( realctx );
			imirc.setRemoveContexts( realctx );
			imirc.setInsertContext( realctx );
			imirc.begin();
			imirc.clear();
			imirc.add( model );
			imirc.commit();
		}
		catch ( RepositoryException re ) {
			log.warn( re, re );
			if ( null != imirc ) {
				try {
					imirc.rollback();
				}
				catch ( Exception e ) {
					log.warn( e, e );
				}
			}
		}
	}

	private static URI getInsightContext( URI database ) {
		return new URIImpl( database.toString() + "_insights" );
	}
}
