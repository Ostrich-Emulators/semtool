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
import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

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

	public static final IRI INSIGHT_LOC = Utility.makeInternalIRI( "insight-loc" );
	public static final IRI GRAPH_COLOR = Utility.makeInternalIRI( "graph-color" );
	public static final IRI GRAPH_SHAPE = Utility.makeInternalIRI( "graph-shape" );
	public static final IRI GRAPH_ICON = Utility.makeInternalIRI( "graph-icon" );

	public StoredMetadata( File datadir ) {
		try {
			MemoryStore store = new MemoryStore( datadir );
			store.setSyncDelay( 2000 ); // every two seconds (arbitrary)
			SailRepository repo = new SailRepository( store );
			repo.init();
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

	public Set<IRI> getDatabases() {
		Set<IRI> set = new HashSet<>();
		try {
			for ( Resource r : Iterations.asList( rc.getContextIDs() ) ) {
				set.add( IRI.class.cast( r ) );
			}
		}
		catch ( RepositoryException e ) {
			log.error( e, e );
		}

		return set;
	}

	public Set<String> getInsightLocations( IRI database ) {
		return getStrings( null, database, INSIGHT_LOC );
	}

	public void setInsightLocations( IRI database, String... locs ) {
		set( null, database, INSIGHT_LOC, locs );
	}

	public void set( IRI database, IRI IRI, Color col ) {
		set( database, IRI, GRAPH_COLOR, String.format( "%d,%d,%d",
				col.getRed(), col.getGreen(), col.getBlue() ) );
	}

	public Color get( IRI database, IRI IRI, Color defaultval ) {
		String col = getString( database, IRI, GRAPH_COLOR, "" );

		Color color = defaultval;
		Pattern PAT = Pattern.compile( "(\\d+),(\\d+),(\\d)" );
		Matcher m = PAT.matcher( col );
		if ( m.matches() ) {
			color = new Color( Integer.parseInt( m.group( 1 ) ),
					Integer.parseInt( m.group( 2 ) ), Integer.parseInt( m.group( 3 ) ) );
		}

		return color;
	}

	public void set( IRI database, IRI IRI, NamedShape ns ) {
		set( database, IRI, GRAPH_SHAPE, ns.toString() );
	}

	public NamedShape getShape( IRI database, IRI IRI, NamedShape defaultval ) {
		String str = getString( database, IRI, GRAPH_SHAPE, "" );
		return ( str.isEmpty() ? defaultval : NamedShape.valueOf( str ) );
	}

	public GraphColorShapeRepository getCSRepo( IRI database ) {
		DefaultColorShapeRepository repo = new DefaultColorShapeRepository();
		try {
			List<Statement> stmts = Iterations.asList( rc.getStatements( null,
					GRAPH_SHAPE, null, false, database ) );
			for ( Statement s : stmts ) {
				repo.set( IRI.class.cast( s.getSubject() ),
						NamedShape.valueOf( s.getObject().stringValue() ) );
			}

			stmts = Iterations.asList( rc.getStatements( null,
					GRAPH_COLOR, null, false, database ) );
			for ( Statement s : stmts ) {
				String vals[] = s.getObject().stringValue().split( "," );
				Color color = new Color( Integer.parseInt( vals[0] ),
						Integer.parseInt( vals[1] ), Integer.parseInt( vals[2] ) );
				repo.set( IRI.class.cast( s.getSubject() ), color );
			}

			stmts = Iterations.asList( rc.getStatements( null,
					GRAPH_ICON, null, false, database ) );
			for ( Statement s : stmts ) {
				try {
					repo.set( IRI.class.cast( s.getSubject() ),
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

	public void clearGraphSettings( IRI database ) {
		try {
			rc.remove( rc.getStatements( null, GRAPH_SHAPE, null, false, database ) );
			rc.remove( rc.getStatements( null, GRAPH_COLOR, null, false, database ) );
			rc.remove( rc.getStatements( null, GRAPH_ICON, null, false, database ) );
		}
		catch ( RepositoryException re ) {
			log.error( re, re );
		}
	}

	public void set( IRI database, GraphColorShapeRepository repo ) {
		clearGraphSettings( database );

		for ( Map.Entry<IRI, NamedShape> en : repo.getShapes().entrySet() ) {
			set( database, en.getKey(), en.getValue() );
		}
		for ( Map.Entry<IRI, Color> en : repo.getColors().entrySet() ) {
			set( database, en.getKey(), en.getValue() );
		}
		for ( Map.Entry<IRI, URL> en : repo.getIcons().entrySet() ) {
			set( database, en.getKey(), GRAPH_ICON,
					SimpleValueFactory.getInstance().createIRI( en.getValue().toExternalForm() ) );
		}
	}

	private void set( IRI ctx, IRI subj, IRI pref, boolean bool ) {
		try {
			rc.begin();
			rc.remove( subj, pref, null, ctx );
			rc.add( rc.getValueFactory().createStatement( subj, pref, vf.createLiteral( bool ), ctx ) );
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

	public void set( IRI ctx, IRI subj, IRI pref, IRI... uris ) {
		try {
			rc.begin();
			rc.remove( subj, pref, null, ctx );

			if ( !( null == uris || 0 == uris.length ) ) {
				for ( IRI u : uris ) {
					rc.add( rc.getValueFactory().createStatement( subj, pref, u, ctx ) );
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

	public void set( IRI ctx, IRI subj, IRI pref, String... val ) {
		try {
			rc.begin();
			rc.remove( subj, pref, null, ctx );
			for ( String s : val ) {
				rc.add( rc.getValueFactory().createStatement( subj, pref, vf.createLiteral( s ), ctx ) );
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

	private <X> X get( IRI ctx, IRI subj, IRI pref, QueryExecutor<X> qa ) {
		qa.bind( "db", subj );
		qa.bind( "pred", pref );
		qa.setContext( ctx );
		return AbstractSesameEngine.getSelectNoEx( qa, rc, true );
	}

	public boolean getBool( IRI ctx, IRI IRI, IRI pref, boolean defaultval ) {
		Boolean b = get( ctx, IRI, pref,
				OneValueQueryAdapter.getBoolean( "SELECT ?val WHERE { ?db ?pred ?val }" ) );

		return ( null == b ? defaultval : b );
	}

	public String getString( IRI ctx, IRI IRI, IRI pref, String defaultval ) {
		String str = get( ctx, IRI, pref,
				OneValueQueryAdapter.getString( "SELECT ?val WHERE { ?db ?pred ?val }" ) );
		return ( null == str ? defaultval : str );
	}

	public Set<String> getStrings( IRI ctx, IRI IRI, IRI pref ) {
		return new HashSet<>( get( ctx, IRI, pref,
				OneVarListQueryAdapter.getStringList( "SELECT ?val WHERE { ?db ?pred ?val }" ) ) );
	}

	public Set<IRI> getUris( IRI ctx, IRI IRI, IRI pref ) {
		return new HashSet<>( get( ctx, IRI, pref,
				OneVarListQueryAdapter.getIriList( "SELECT ?val WHERE { ?db ?pred ?val }" ) ) );
	}

	public IRI getUri( IRI ctx, IRI IRI, IRI pref ) {
		return get( ctx, IRI, pref,
				OneValueQueryAdapter.getUri( "SELECT ?val WHERE { ?db ?pred ?val }" ) );
	}

	public InsightManager getLocalInsightManager( IRI database ) {
		InsightManagerImpl imi = new InsightManagerImpl();
		try {
			Model imirc = QueryResults.asModel( rc.getStatements( null, null, null, getInsightContext( database ) ) );
			imi.loadFromModel( imirc );
		}
		catch ( RepositoryException re ) {
			log.warn( re, re );
		}

		return imi;
	}

	public void setLocalInsights( IRI database, InsightManager imi ) {
		Model model = InsightManagerImpl.getModel( imi, new LocalUserImpl() );

		try {
			IRI realctx = getInsightContext( database );
			rc.begin();
			rc.clear( realctx );
			rc.add( model );
			rc.commit();
		}
		catch ( RepositoryException re ) {
			log.warn( re, re );
		}
	}

	private static IRI getInsightContext( IRI database ) {
		return SimpleValueFactory.getInstance().createIRI( database.toString() + "_insights" );
	}
}
