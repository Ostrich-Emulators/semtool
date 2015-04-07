/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.util;

import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.BigdataSailRepository;
import com.bigdata.rdf.sail.BigdataSailRepositoryConnection;
import gov.va.semoss.model.vocabulary.VAS;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFHandlerException;
import gov.va.semoss.poi.main.ImportData;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.api.MetadataConstants;
import gov.va.semoss.rdf.engine.api.ModificationExecutor;
import gov.va.semoss.rdf.engine.api.WriteableInsightManager;
import gov.va.semoss.rdf.engine.impl.AbstractEngine;
import gov.va.semoss.rdf.engine.impl.BigDataEngine;
import gov.va.semoss.rdf.engine.impl.InMemorySesameEngine;
import gov.va.semoss.rdf.engine.impl.InsightManagerImpl;
import gov.va.semoss.rdf.query.util.MetadataQuery;
import gov.va.semoss.rdf.query.util.ModificationExecutorAdapter;
import gov.va.semoss.ui.components.RepositoryList;
import gov.va.semoss.ui.components.RepositoryList.RepositoryListModel;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.Utility;
import gov.va.semoss.rdf.engine.util.EngineManagementException.ErrorCode;
import gov.va.semoss.rdf.query.util.QueryExecutorAdapter;
import gov.va.semoss.ui.main.SemossPreferences;
import gov.va.semoss.util.UriBuilder;
import info.aduna.iteration.Iterations;
import java.io.InputStream;
import java.util.prefs.Preferences;
import org.apache.commons.io.FilenameUtils;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

/**
 * A class to centralize Engine operations. This class is thread-safe, and if
 * started as a thread and used to mount repositories, those repositories can be
 * successfully unmounted later.
 *
 * @author ryan
 */
public class EngineUtil implements Runnable {

	private static final Logger log = Logger.getLogger( EngineUtil.class );

	private static EngineUtil instance;

	private final Map<File, Boolean> toopen = Collections.synchronizedMap( new HashMap<File, Boolean>() );
	private final List<IEngine> toclose = Collections.synchronizedList( new ArrayList<IEngine>() );
	private final List<EngineOperationListener> listeners = new ArrayList<>();
	private final Map<IEngine, InsightsImportConfig> insightqueue = new HashMap<>();
	private boolean stopping = false;

	private EngineUtil() {
	}

	public synchronized void addEngineOpListener( EngineOperationListener eol ) {
		listeners.add( eol );
	}

	public synchronized void removeEngineOpListener( EngineOperationListener eol ) {
		listeners.remove( eol );
	}

	public static EngineUtil getInstance() {
		if ( null == instance ) {
			instance = new EngineUtil();
		}
		return instance;
	}

	@Override
	public void run() {
		log.debug( "starting EngineUtil thread" );
		while ( !stopping ) {
			synchronized ( this ) {
				try {
					log.debug( "going to sleep" );
					wait();
				}
				catch ( InterruptedException ie ) {
					log.error( "interrupted", ie );
				}
			}

			log.debug( "awaken, looking for operations" );

			// check closings first, then openings
			ListIterator<IEngine> closeit = toclose.listIterator();
			while ( closeit.hasNext() ) {
				IEngine eng = closeit.next();
				eng.closeDB();
				closeit.remove();

				for ( EngineOperationListener eol : listeners ) {
					eol.engineClosed( eng );
				}
			}

			// check for engines to open
			boolean firstcheck = true;
			while ( !toopen.isEmpty() ) {
				if ( log.isDebugEnabled() ) {
					if ( !firstcheck ) {
						log.debug( "engines added to open during openings" );
					}
					firstcheck = false;
				}

				// avoid concurrent mod exception
				Map<File, Boolean> todo;
				synchronized ( toopen ) { // we need an atomic copy and clear operation
					todo = new HashMap<>( toopen );
					toopen.clear();
				}

				for ( Map.Entry<File, Boolean> entry : todo.entrySet() ) {
					try {
						boolean domount = true;
						for ( IEngine eng : DIHelper.getInstance().getEngineMap().values() ) {
							File mountedsmss = new File( eng.getProperty( Constants.SMSS_LOCATION ) );
							if ( entry.getKey().getAbsolutePath().equals( mountedsmss.getAbsolutePath() ) ) {
								log.debug( "db already mounted, skipping:" + entry.getKey() );
								domount = false;
							}
						}

						if ( domount ) {
							IEngine eng = Utility.loadEngine( entry.getKey() );
							// avoid a possible ConcurrentModificationException
							List<EngineOperationListener> lls = new ArrayList<>( listeners );
							for ( EngineOperationListener eol : lls ) {
								eol.engineOpened( eng );
							}

							// update the repository list?
							if ( entry.getValue() && eng.isConnected() ) {
								EngineUtil.addRepositoryToList( eng );
							}
						}
					}
					catch ( IOException ioe ) {
						log.error( ioe );
					}
				}
				todo.clear();
			}

			// check for insights to load
			Map<IEngine, InsightsImportConfig> iq = new HashMap<>( insightqueue );
			for ( Map.Entry<IEngine, InsightsImportConfig> imps : iq.entrySet() ) {
				IEngine eng = imps.getKey();
				InsightsImportConfig iic = imps.getValue();

				WriteableInsightManager wim = eng.getWriteableInsightManager();

				if ( iic.clearfirst ) {
					wim.clear();
				}

				try {
					Model m = new LinkedHashModel( iic.stmts );
					List<URI> persps = new ArrayList<>();
					List<URI> ins = new ArrayList<>();
					for ( Resource r : m.filter( null, RDF.TYPE, VAS.Perspective ).subjects() ) {
						persps.add( URI.class.cast( r ) );
					}
					for ( Value v : m.filter( null, VAS.insight, null ).objects() ) {
						ins.add( URI.class.cast( v ) );
					}

					wim.addRawStatements( iic.stmts );
					wim.commit();

					List<EngineOperationListener> lls = new ArrayList<>( listeners );

					for ( EngineOperationListener eol : lls ) {
						eol.insightsModified( eng, persps, ins );
					}
				}
				catch ( RepositoryException e ) {
					log.error( e, e );
				}
				finally {
					wim.release();
				}
			}
			insightqueue.clear();
		}
	}

	public synchronized void stop() {
		stopping = true;
		notify();
	}

	public void clone( String oldsmss ) throws IOException, RepositoryException,
			EngineManagementException {
		IEngine from = null;
		boolean fromWasOpened = false;
		// see if our copied db is still open
		RepositoryList rl = DIHelper.getInstance().getRepoList();

		for ( IEngine eng : rl.getRepositoryModel().getElements() ) {
			if ( oldsmss.equals( eng.getProperty( Constants.SMSS_LOCATION ) ) ) {
				from = eng;
				break;
			}
		}

		File oldfile = new File( oldsmss );
		// not opened, but maybe still exists
		if ( null == from ) {
			from = Utility.loadEngine( oldfile );
			fromWasOpened = true;
		}

		Map<URI, String> metas = new HashMap<>();
		try {
			metas.putAll( from.query( new MetadataQuery() ) );
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			log.warn( "no metadata found in engine: " + from, e );
		}

		// see if our SMSS file is old-school or new-school
		Properties props = new Properties();
		try ( FileReader rdr = new FileReader( oldfile ) ) {
			props.load( rdr );
		}

		String propver = props.getProperty( Constants.SMSS_VERSION_KEY, "0.0" );
		Double dbl = Double.parseDouble( propver );
		File dbdir = oldfile.getParentFile();
		if ( dbl >= 1.0d ) {
			// we're in the new style, so we want to put our new database in the
			// parent directory from where the smss file is
			dbdir = dbdir.getParentFile();
		}

		// figure out what number copy we will be
		int lastcopy = -1;
		Pattern pat = Pattern.compile( ".* Copy([\\s]?[0-9]+)?" );

		for ( File f : dbdir.listFiles() ) {
			String filename = f.getName();
			log.debug( "filename to check: " + filename );
			Matcher m = pat.matcher( filename );
			if ( m.matches() ) {
				String number = m.group( 1 );

				if ( null == number ) {
					lastcopy = 0;
				}
				else {
					int num = Integer.parseInt( number.trim() );
					if ( num > lastcopy ) {
						lastcopy = num;
					}
				}
			}
		}

		String sfx = " Copy";
		if ( lastcopy >= 0 ) {
			sfx += " " + Integer.toString( lastcopy + 1 );
		}
		String newname = from.getEngineName() + sfx;

		try {
			DbCloneMetadata metadata = new DbCloneMetadata( dbdir, newname,
					metas.get( RDFS.LABEL ) + sfx, from.getBaseUri() );

			EngineUtil.getInstance().clone( from, metadata, true, true );
		}

		finally {
			if ( fromWasOpened ) {
				unmount( from );
			}
		}
	}

	public void clone( IEngine from, DbCloneMetadata metadata, boolean copydata,
			boolean addToRepoList ) throws RepositoryException, IOException, EngineManagementException {
		// cloning is done in 4 steps:
		// 1) Copy the RDF statements from one engine to the other
		// 2) Copy the OWL, Ontology, and DREAMER files to the new location
		// 
		// Note: We're removing a lot of keys in this function so we can transition
		// to using the convention of all db files going in the db directory
		// instead of scattered about on the filesystem.

		log.debug( "cloning " + from.getEngineName() + " to " + metadata.getTitle() );
		Properties props = from.getProperties();
		String newName = metadata.getName();

		// figure out if we're the "old style" with the smss file above the 
		// dbdir, or the "new style" where it's inside the db dir
		// the easiest way is to see if we have a version key, and if we do,
		// assume we must be in the "new style" if it's above 1.
		props.setProperty( Constants.SMSS_VERSION_KEY, "1.0" );

		// get all the required files in the dbdir
		File dbdir = new File( metadata.getLocation(), metadata.getName() );
		dbdir.mkdirs();

		// FIXME: we're assuming we always want a BigData clone
		if ( null == props.getProperty( BigdataSail.Options.FILE ) ) {
			log.warn( "cloning db as BigData store" );

		}
		props.setProperty( Constants.ENGINE_IMPL, BigDataEngine.class.getCanonicalName() );
		props.setProperty( Constants.SEMOSS_URI, from.getSchemaBuilder().toUri().stringValue() );

		Properties rws = Utility.copyProperties( props );
		File jnl = new File( dbdir, newName + ".jnl" );
		String jnlpath = jnl.getCanonicalPath();

		rws.setProperty( BigdataSail.Options.FILE, jnlpath );

		fillRepo( from, rws, copydata, metadata );
		// we don't need to set the FILE property because it follows the convention
		// we just needed it for the fillRepo call

		rws.remove( BigdataSail.Options.FILE );

		File rwsfile = new File( dbdir, "RWStore.properties" );
		try ( FileWriter fw = new FileWriter( rwsfile ) ) {
			SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss'Z'" );
			sdf.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
			rws.store( fw, "cloned from " + from.getEngineName() + " on "
					+ sdf.format( new Date() ) );
		}

		// no need to specify the file, because it follows the convention
		props.remove( Constants.SMSS_RWSTORE_KEY );

		String owlloc = props.getProperty( Constants.OWLFILE );
		if ( null != owlloc ) {
			File oldowl = new File( owlloc );
			FileUtils.copyFile( oldowl, new File( dbdir,
					AbstractEngine.getDefaultName( Constants.OWLFILE, newName ) ) );
			// no need to specify the file, because it follows the convention
		}

		props.remove( Constants.OWLFILE );

		String dreamloc = props.getProperty( Constants.DREAMER );
		if ( null != dreamloc ) {
			File olddreamer = new File( dreamloc );
			File newdreamer = new File( dbdir,
					AbstractEngine.getDefaultName( Constants.DREAMER, newName ) );
			FileUtils.copyFile( olddreamer, newdreamer );
		}

		// no need to specify the file, because it follows the convention
		props.remove( Constants.DREAMER );

		String ontoloc = props.getProperty( Constants.ONTOLOGY );
		if ( null != ontoloc ) {
			File oldonto = new File( ontoloc );
			File newonto = new File( dbdir,
					AbstractEngine.getDefaultName( Constants.ONTOLOGY, newName ) );
			FileUtils.copyFile( oldonto, newonto );
		}

		// no need to specify the file, because it follows the convention
		props.remove( Constants.ONTOLOGY );
		props.remove( Constants.ENGINE_NAME );
		props.remove( Constants.SMSS_LOCATION );

		// this is in every props file, but looks like it is a programming mistake
		props.remove( "Base" );
		props.remove( Constants.PIN_KEY ); // don't copy the pinning, if it's there

		props.setProperty( Constants.SMSS_VERSION_KEY, "1.0" );
		props.setProperty( Constants.DEFAULTUI_KEY, "veera.ui" );

		File smss = new File( dbdir, newName + ".smss" );
		try ( FileWriter fw = new FileWriter( smss ) ) {
			log.debug( "writing new smss to: " + smss.getAbsolutePath() );
			props.store( fw, newName );
		}

		mount( smss, addToRepoList,
				true );
	}

	/**
	 * Mounts a repository from its database directory, or its SMSS file. Most
	 * callers will want to follow a call to mount with one to
   * {@link #addRepositoryToList(gov.va.semoss.rdf.engine.api.IEngine) }
	 *
	 * @param smssfile either the SMSS file, or the database containing the SMSS
	 * file
	 * @param updateRepoList should the repository list be updated once the
	 * repository is loaded?
	 * @param noDupeEx if the given SMSS is already loaded, do nothing instead of
	 * throwing the usual DuplicateName exception
	 *
	 * @throws EngineManagementException
	 */
	public synchronized void mount( File smssfile, boolean updateRepoList,
			boolean noDupeEx ) throws EngineManagementException {
		// at this point, we don't know if smssfile is a directory containing the 
		// smss file, or the file itself, so figure out what we're looking at    

		if ( smssfile.isDirectory() ) {
			// if this is a directory, we should have an smss file either in the
			// directory, or at the same level as it
			FilenameFilter fnf = new FilenameFilter() {

				@Override
				public boolean accept( File dir, String name ) {
					return ( name.endsWith( ".smss" ) || name.endsWith( ".jnl" ) );
				}
			};

			// check inside this directory for an smss file
			// (assume we'll only have one smss inside)
			for ( File check : smssfile.listFiles( fnf ) ) {
				smssfile = check;
			}
		}
		// else we've been given the smss file itself

		log.debug( "mounting db from smss:" + smssfile );

		for ( IEngine engo : DIHelper.getInstance().getEngineMap().values() ) {
			try {
				File othersmss = new File( engo.getProperty( Constants.SMSS_LOCATION ) );
				if ( othersmss.getCanonicalPath().equals( smssfile.getCanonicalPath() ) ) {
					if ( noDupeEx ) {
						log.debug( "ignoring already-mounted smss:" + smssfile );
						return; // silently ignoring
					}
					else {
						throw new EngineManagementException( ErrorCode.DUPLICATE_NAME,
								"This database is already mounted" );
					}
				}
			}
			catch ( IOException ioe ) {
				throw new EngineManagementException( ErrorCode.UNREADABLE_SMSS,
						"Could not load properties file: " + smssfile, ioe );
			}
		}

		toopen.put( smssfile, updateRepoList );
		notify();
	}

	public void mount( File smssfile, boolean updateRepoList )
			throws EngineManagementException {
		mount( smssfile, updateRepoList, false );
	}

	public synchronized void unmount( final IEngine engineToClose ) {
		final RepositoryList list = DIHelper.getInstance().getRepoList();
		final IEngine selected = DIHelper.getInstance().getRdfEngine();

		toclose.add( engineToClose );
		notify();

		DIHelper.getInstance().unregisterEngine( engineToClose );

		SwingUtilities.invokeLater( new Runnable() {

			@Override
			public void run() {
				RepositoryListModel model = list.getRepositoryModel();
				model.remove( engineToClose );

				if ( selected.equals( engineToClose ) && !model.isEmpty() ) {
					// we're closing our selected engine, so select something else
					list.setSelectedIndex( 0 );
				}
			}
		} );
	}

	/**
	 * Copies all the data from the "from" repository to the "to" repository
	 *
	 * @param from the source of statements
	 * @param to the destination of the statements
	 * @param doData copy the data
	 * @param doInsights copy the insights
	 *
	 * @throws RepositoryException
	 */
	public synchronized void merge( final IEngine from, final IEngine to,
			boolean doData, boolean doInsights ) throws RepositoryException {
		// this is a little complicated because we don't have a handle to either
		// repositoryconnection. 

		if ( doData ) {
			ModificationExecutor fromcopier = new ModificationExecutorAdapter() {

				@Override
				public void exec( final RepositoryConnection fromconn ) throws RepositoryException {

					final ModificationExecutor tocopier = new ModificationExecutorAdapter() {

						@Override
						public void exec( RepositoryConnection toconn ) throws RepositoryException {
							try {
								final RepositoryCopier copier = new RepositoryCopier( toconn );
								fromconn.export( copier );
							}
							catch ( RDFHandlerException e ) {
								log.error( e );
							}
						}
					};

					to.execute( tocopier );
				}

			};

			from.execute( fromcopier );
		}

		if ( doInsights ) {
			insightqueue.put( to,
					new InsightsImportConfig( from.getInsightManager().getStatements(),
							false ) );
			notify();
		}

		// RPB: I don't think we need to do this anymore
		// we don't have a "reload" function, so just close and re-open
//    to.closeDB();
//    to.openDB( to.getProperties() ); // not sure this really works
	}

	/**
	 * Creates an empty database by copying data from the db/Default directory
	 *
	 * @param dbtopdir directory about where the engine will be placed.
	 * @param engine the engine name
	 * @param baseuri the base uri of the new engine
	 * @param smss the custom smss file. If null or empty, will use the sample
	 * file from the Defaults directory
	 * @param map the custom ontology properties file. Can be null null or empty
	 * @param questions the custom questions file. If null or empty, will use the
	 * sample file from the Defaults directory
	 * @param toload the files to load. An {@link IllegalArgumentException} will
	 * be thrown if this list is empty
	 * @param stageInMemory load intermediate data to memory; if false, write it
	 * to disk
	 * @param calcInfers should the engine calculate inferences after the load?
	 * @param dometamodel should the loader create the metamodel from the load?
	 * @param conformanceErrors if not null, conformance will be checked, and
	 * errors will be placed here
	 *
	 * @return the newly-created smss file, or null if something goes wrong
	 *
	 * @throws java.io.IOException
	 * @throws gov.va.semoss.rdf.engine.util.EngineManagementException
	 */
	public static File createNew( File dbtopdir, String engine, String baseuri,
			String smss, String map, String questions, Collection<File> toload,
			boolean stageInMemory, boolean calcInfers, boolean dometamodel,
			ImportData conformanceErrors ) throws IOException, EngineManagementException {

		dbtopdir.mkdirs();

		File modelmap = ( null == map || map.isEmpty() ? null : new File( map ) );
		File modelsmss = ( null == smss || smss.isEmpty() ? null : new File( smss ) );

		File modelquestions;
		if ( null == questions || questions.isEmpty() ) {
			String ddreamer = "/defaultdb/"
					+ AbstractEngine.getDefaultName( Constants.DREAMER, "Default" );
			File f = File.createTempFile( "semoss-tmp-dreamer-", "prop" );
			FileUtils.copyInputStreamToFile( EngineUtil.class
					.getResourceAsStream( ddreamer ), f );
			modelquestions = f;

			f.deleteOnExit();
		}
		else {
			modelquestions = new File( questions );
		}

		List<Statement> insights
				= getStatementsFromResource( "/models/va-semoss.ttl", RDFFormat.TURTLE );
		insights.addAll( createInsightStatements( modelquestions ) );

		File smssfile = createEngine( dbtopdir, engine, new URIImpl( baseuri ),
				modelsmss, modelmap );

		IEngine bde = Utility.loadEngine( smssfile.getAbsoluteFile() );

		try {
			WriteableInsightManager wim = bde.getWriteableInsightManager();

			wim.addRawStatements( insights );
			wim.commit();
			wim.release();
		}
		catch ( RepositoryException re ) {
			throw new EngineManagementException( EngineManagementException.ErrorCode.UNKNOWN,
					re );
		}

		try {
			EngineLoader el = new EngineLoader( stageInMemory );
			el.loadToEngine( toload, bde, dometamodel, conformanceErrors );
			el.release();
			if ( calcInfers ) {
				bde.calculateInferences();
			}
		}
		catch ( RepositoryException e ) {
			throw new EngineManagementException( e );
		}
		finally {
			DIHelper.getInstance().unregisterEngine( bde );
			bde.closeDB();
		}

		return smssfile;
	}

	/**
	 * Adds Insights from the given file to the given engine. This function is
	 * always safer than doing it yourself, because this function is guaranteed to
	 * run on the same thread that opened the KB in the first place.
	 *
	 * @param engine the engine
	 * @param insightsfile the file with the statements to add (could be a legacy
	 * "properties" file). If null, this function will clear all insights from the
	 * engine without adding anything
	 * @param clearfirst remove the current insights before adding the new ones?
	 *
	 * @throws IOException
	 * @throws EngineManagementException
	 */
	public synchronized void importInsights( IEngine engine, File insightsfile,
			boolean clearfirst ) throws IOException, EngineManagementException {
		List<Statement> stmts = new ArrayList<>();
		if ( null != insightsfile ) {
			stmts.addAll( createInsightStatements( insightsfile ) );
		}

		// need to re-seed the initial model statements, like during KB creation
		stmts.addAll( getStatementsFromResource( "/models/va-semoss.ttl", RDFFormat.TURTLE ) );

		insightqueue.put( engine, new InsightsImportConfig( stmts, clearfirst ) );
		notify();
	}

	/**
	 * Imports Insights from a list into the active repository. Reports upon
	 * success.
	 *
	 * @param statements -- The RDF Statemsnts to be imported.
	 *
	 * @return -- (boolean) Whether the import succeeded.
	 */
	public synchronized boolean importInsightsFromList( RepositoryResult<Statement> statements ) {
		try {
			IEngine engine = DIHelper.getInstance().getRdfEngine();
			List<Statement> stmts = new ArrayList<>();
			stmts.addAll( Iterations.asList( statements ) );
			insightqueue.put( engine, new InsightsImportConfig( stmts, true ) );
			notify();
			return true;
		}
		catch ( Exception e ) {
			log.error( e, e );
			return false;
		}
	}

	public static void addRepositoryToList( final IEngine eng ) {
		SwingUtilities.invokeLater( new Runnable() {

			@Override
			public void run() {
				RepositoryList list = DIHelper.getInstance().getRepoList();
				list.getRepositoryModel().addElement( eng );
				list.setSelectedValue( eng, true );
			}
		} );
	}

	/**
	 * Creates a new repository by copying the given one, and optionally copies
	 * the data as well
	 *
	 * @param from the engine to copy from
	 * @param props properties for the new engine
	 * @param copydata if true, copy the data as well as the configuration
	 *
	 * @throws RepositoryException
	 */
	private static void fillRepo( final IEngine from, Properties props, boolean copydata,
			final DbCloneMetadata metadata ) throws RepositoryException {
		BigdataSail bdSail = new BigdataSail( props );
		BigdataSailRepository repo = new BigdataSailRepository( bdSail );
		BigdataSailRepositoryConnection rc = null;
		//final TurtleWriter writer;
		final RepositoryCopier rdfhandler;
		try {
			repo.initialize();
			rc = repo.getConnection();
			rc.begin();

			if ( copydata ) {

				rdfhandler = new RepositoryCopier( rc );

				ModificationExecutor copier = new ModificationExecutor() {

					@Override
					public void exec( RepositoryConnection conn ) throws RepositoryException {
						try {
							conn.export( rdfhandler );
						}
						catch ( RepositoryException | RDFHandlerException e ) {
							log.error( e );
						}
					}

					@Override
					public boolean execInTransaction() {
						return false;
					}
				};

				from.execute( copier );
				rc.flush();
			}
			makeNewMetadata( from, metadata, rc );
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException | IOException e ) {
			log.error( "Could not create cloned repository", e );
		}
		finally {
			if ( null != rc ) {
				try {
					rc.flush();
					rc.commit();
					rc.close();
				}
				catch ( Exception e ) {
					log.warn( e );
				}
			}
			try {
				bdSail.shutDown();
			}
			catch ( Exception e ) {
				log.warn( "unable to shutdown clone connection", e );
			}
		}
	}

	private static void makeNewMetadata( final IEngine from, final DbCloneMetadata metadata,
			final RepositoryConnection conn ) throws RepositoryException, MalformedQueryException,
			QueryEvaluationException, IOException {
		ValueFactory vf = conn.getValueFactory();

		// delete any metadata that might exist from the repository copy
		Collection<Statement> stmts = Iterations.asList( conn.getStatements( null,
				MetadataConstants.VOID_DS, null, false ) );
		for ( Statement s : stmts ) {
			conn.remove( s.getSubject(), null, null );
		}

		EngineUtil.add( conn, metadata.getBaseUri(), RDFS.LABEL,
				metadata.getTitle(), vf );
		conn.add( new StatementImpl( metadata.getBaseUri(), RDF.TYPE,
				MetadataConstants.VOID_DS ) );
		EngineUtil.add( conn, metadata.getBaseUri(), MetadataConstants.DCT_DESC,
				"Cloned from " + from.getEngineName(), vf );
		Date now = new Date();
		EngineUtil.add( conn, metadata.getBaseUri(), MetadataConstants.DCT_CREATED,
				now, vf );
		EngineUtil.add( conn, metadata.getBaseUri(), MetadataConstants.DCT_MODIFIED,
				now, vf );

		URI agenturi
				= from.getDataBuilder().getCoreUri( Constants.SOFTWARE_AGENT_LOCALNAME );

		EngineUtil.add( conn, metadata.getBaseUri(), agenturi,
				System.getProperty( "build.name", "unknown" ), vf );

		MetadataQuery mq = new MetadataQuery();

		// FIXME: we can't tell from these queries if we're retrieving the "right"
		// metadata from the source KB. We don't have any way of identifying the 
		// DB's base URI (and there could be many).
		Map<URI, String> oldmetadata = from.query( mq );
		if ( oldmetadata.containsKey( MetadataConstants.DCT_CONTRIB ) ) {
			EngineUtil.add( conn, metadata.getBaseUri(), MetadataConstants.DCT_CONTRIB,
					oldmetadata.get( MetadataConstants.DCT_CONTRIB ), vf );
		}
		if ( oldmetadata.containsKey( MetadataConstants.DCT_PUBLISHER ) ) {
			EngineUtil.add( conn, metadata.getBaseUri(), MetadataConstants.DCT_PUBLISHER,
					oldmetadata.get( MetadataConstants.DCT_PUBLISHER ), vf );
		}
	}

	private static void add( RepositoryConnection conn, URI dburi, URI pred,
			String val, ValueFactory fac ) throws RepositoryException {
		conn.add( new StatementImpl( dburi, pred, fac.createLiteral( val ) ) );
	}

	private static void add( RepositoryConnection conn, URI dburi, URI pred,
			Date val, ValueFactory fac ) throws RepositoryException {
		conn.add( new StatementImpl( dburi, pred,
				fac.createLiteral( QueryExecutorAdapter.getCal( val ) ) ) );

	}

	public static File createEngine( File enginedir, String dbname, URI baseuri,
			File modelsmss, File modelmap ) throws IOException, EngineManagementException {

		if ( null != modelmap && modelmap.exists() ) {
			try {
				FileUtils.copyFile( modelmap, new File( enginedir,
						AbstractEngine.getDefaultName( Constants.ONTOLOGY, dbname ) ) );
			}
			catch ( IOException e ) {
				log.error( e, e );
				return null;
			}
		}

		Properties smssprops = new Properties();
		if ( null == modelsmss || !modelsmss.exists() ) {
			String dprop = "/defaultdb/Default.properties";
			smssprops.load( EngineUtil.class.getResourceAsStream( dprop ) );
		}
		else {
			try ( FileReader rdr = new FileReader( modelsmss ) ) {
				smssprops.load( rdr );
			}
			catch ( IOException e ) {
				log.error( e, e );
				return null;
			}
		}

		// make the big data journal, and then write out the (empty) OWL file
		File jnl = new File( enginedir, dbname + ".jnl" );

		if ( jnl.exists() ) {
			throw new IOException( "KB journal already exists" );
		}

		smssprops.setProperty( BigdataSail.Options.FILE, jnl.getAbsolutePath() );

		BigdataSail sail = new BigdataSail( smssprops );
		BigdataSailRepository repo = new BigdataSailRepository( sail );
		try {
			repo.initialize();
			RepositoryConnection rc = repo.getConnection();

			// add the metadata
			if ( null != baseuri ) {
				rc.begin();
				ValueFactory vf = rc.getValueFactory();
				rc.add( new StatementImpl( baseuri, RDF.TYPE,
						MetadataConstants.VOID_DS ) );
				rc.add( new StatementImpl( baseuri, MetadataConstants.DCT_CREATED,
						vf.createLiteral( QueryExecutorAdapter.getCal( new Date() ) ) ) );

				URI agenturi = UriBuilder.getBuilder( baseuri ).
						getCoreUri( Constants.SOFTWARE_AGENT_LOCALNAME );

				rc.add( new StatementImpl( baseuri, agenturi,
						vf.createLiteral( System.getProperty( "build.name", "unknown" ) ) ) );

				Preferences prefs = Preferences.userNodeForPackage( SemossPreferences.class );
				String username = prefs.get( Constants.USERPREF_NAME, "" );
				String email = prefs.get( Constants.USERPREF_EMAIL, "" );
				String org = prefs.get( Constants.USERPREF_ORG, "" );

				if ( !( username.isEmpty() && email.isEmpty() ) ) {
					StringBuilder poc = new StringBuilder();
					if ( username.isEmpty() ) {
						poc.append( email );
					}
					else {
						poc.append( username );
					}
					if ( !email.isEmpty() ) {
						poc.append( " <" ).append( email ).append( ">" );
					}

					rc.add( new StatementImpl( baseuri, MetadataConstants.DCT_PUBLISHER,
							vf.createLiteral( poc.toString() ) ) );
				}
				if ( !org.isEmpty() ) {
					rc.add( new StatementImpl( baseuri, MetadataConstants.DCT_CREATOR,
							vf.createLiteral( org ) ) );
				}

				rc.commit();
			}
			rc.close();
			repo.shutDown();
		}
		catch ( RepositoryException e ) {
			log.error( e, e );
			return null;
		}

		return jnl;
	}

	public static Collection<Statement> createInsightStatements( File modelquestions )
			throws IOException, EngineManagementException {
		LinkedHashModel model = new LinkedHashModel();
		if ( null == modelquestions || !modelquestions.exists() ) {
			return model;
		}

		Map<String, RDFFormat> extfmt = new HashMap<>();

		extfmt.put( "ttl", RDFFormat.TURTLE );
		extfmt.put( "rdf", RDFFormat.RDFXML );
		extfmt.put( "n3", RDFFormat.N3 );
		extfmt.put( "nt", RDFFormat.NTRIPLES );

		List<Statement> stmts = new ArrayList<>();
		Repository repo = new SailRepository( new MemoryStore() );

		try {
			repo.initialize();
			if ( FilenameUtils.isExtension( modelquestions.toString(), extfmt.keySet() ) ) {
				RepositoryConnection rc = repo.getConnection();
				rc.add( modelquestions, "",
						extfmt.get( FilenameUtils.getExtension( modelquestions.toString() ) ) );
				stmts.addAll( Iterations.asList( rc.getStatements( null, null, null, false ) ) );
				rc.close();
				repo.shutDown();
			}
			else {
				InsightManagerImpl iei = new InsightManagerImpl( repo );
				Properties p = Utility.loadProp( modelquestions );
				iei.loadAllPerspectives( p );
				stmts.addAll( iei.getStatements() );
				iei.release(); // also shuts down repo
			}
		}
		catch ( RepositoryException | RDFParseException e ) {
			throw new EngineManagementException( EngineManagementException.ErrorCode.FILE_ERROR,
					e );
		}

		model.addAll( stmts );
		boolean ok = model.contains( null, RDF.TYPE, VAS.Perspective );

		if ( !ok ) {
			model.clear();
			throw new EngineManagementException( EngineManagementException.ErrorCode.MISSING_REQUIRED_TUPLE,
					modelquestions + " does not contain any Perspectives" );
		}

		return stmts;
	}

	public static void clear( IEngine engine ) throws RepositoryException {
		try {
			final Map<URI, String> metas = engine.query( new MetadataQuery() );
			metas.remove( MetadataConstants.VOID_DS );

			engine.execute( new ModificationExecutorAdapter() {

				@Override
				public void exec( RepositoryConnection conn ) throws RepositoryException {
					conn.remove( (Resource) null, null, null );

					ValueFactory vf = conn.getValueFactory();
					// re-add the metadata
					for ( Map.Entry<URI, String> en : metas.entrySet() ) {
						conn.add( engine.getBaseUri(), en.getKey(),
								vf.createLiteral( en.getValue() ) );
					}
				}
			} );
		}
		catch ( MalformedQueryException | QueryEvaluationException e ) {
			log.error( e, e );
		}
	}

	private static List<Statement> getStatementsFromResource( String resource,
			RDFFormat fmt ) {
		List<Statement> stmts = new ArrayList<>();

		InMemorySesameEngine mem = new InMemorySesameEngine();
		try ( InputStream is = EngineUtil.class.getResourceAsStream( resource ) ) {
			RepositoryConnection rc = mem.getRepositoryConnection();
			rc.add( is, Constants.DEFAULT_SEMOSS_URI, fmt );
			rc.commit();

			for ( Statement s : Iterations.asList( rc.getStatements( null, null, null, false ) ) ) {
				stmts.add( s );
			}
		}
		catch ( Exception e ) {
			log.warn( "could not open/parse model resource: " + resource, e );
		}

		mem.closeDB();

		return stmts;
	}

	public static class DbCloneMetadata {

		private final File location;
		private final String name;
		private final String title;
		private final URI baseuri;

		public DbCloneMetadata( File location, String dbname, String title, URI baseuri ) {
			this.location = location;
			this.title = title;
			this.baseuri = baseuri;
			this.name = dbname;
		}

		public File getLocation() {
			return location;
		}

		public String getTitle() {
			return title;
		}

		public URI getBaseUri() {
			return baseuri;
		}

		public String getName() {
			return name;
		}
	}

	private static class InsightsImportConfig {

		final Collection<Statement> stmts;
		final boolean clearfirst;

		public InsightsImportConfig( Collection<Statement> s, boolean clear ) {
			stmts = s;
			clearfirst = clear;
		}
	}
}
