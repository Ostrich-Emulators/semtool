/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.util;

import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.BigdataSailRepository;
import gov.va.semoss.model.vocabulary.VAC;
import gov.va.semoss.model.vocabulary.VAS;
import gov.va.semoss.poi.main.ImportValidationException;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import gov.va.semoss.poi.main.ImportData;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.api.InsightManager;
import gov.va.semoss.rdf.engine.api.MetadataConstants;
import gov.va.semoss.rdf.engine.api.ModificationExecutor;
import gov.va.semoss.rdf.engine.api.ReificationStyle;
import gov.va.semoss.rdf.engine.impl.AbstractEngine;
import gov.va.semoss.rdf.engine.impl.InsightManagerImpl;
import gov.va.semoss.rdf.query.util.MetadataQuery;
import gov.va.semoss.rdf.query.util.ModificationExecutorAdapter;
import gov.va.semoss.ui.components.RepositoryList;
import gov.va.semoss.ui.components.RepositoryList.RepositoryListModel;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.GuiUtility;
import gov.va.semoss.rdf.engine.util.EngineManagementException.ErrorCode;
import gov.va.semoss.rdf.query.util.QueryExecutorAdapter;
import gov.va.semoss.user.LocalUserImpl;
import gov.va.semoss.user.User;
import gov.va.semoss.user.Security;
import gov.va.semoss.util.Utility;
import info.aduna.iteration.Iterations;
import java.io.InputStream;
import java.net.URL;
import org.apache.commons.io.FilenameUtils;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
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
	private final Map<File, User> openusers = Collections.synchronizedMap( new HashMap<File, User>() );
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
				Map<File, User> users;
				synchronized ( toopen ) { // we need an atomic copy and clear operation
					todo = new HashMap<>( toopen );
					toopen.clear();
				}
				synchronized ( openusers ) { // we need an atomic copy and clear operation
					users = new HashMap<>( openusers );
					openusers.clear();
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
							IEngine eng = GuiUtility.loadEngine( entry.getKey() );
							Security.getSecurity().associateUser( eng, users.get( entry.getKey() ) );

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
				InsightManager oldim = eng.getInsightManager();

				oldim.addAll( iic.im.getPerspectives(), iic.clearfirst );
				try {
					eng.updateInsights( oldim );
					List<EngineOperationListener> lls = new ArrayList<>( listeners );
					for ( EngineOperationListener eol : lls ) {
						eol.insightsModified( eng, oldim.getPerspectives() );
					}
				}
				catch ( EngineManagementException eme ) {
					List<EngineOperationListener> lls = new ArrayList<>( listeners );
					for ( EngineOperationListener eol : lls ) {
						eol.handleError( eng, eme );
					}
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
			from = GuiUtility.loadEngine( oldfile );
			fromWasOpened = true;
		}

		Map<URI, Value> metas = new HashMap<>();
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
					metas.get( RDFS.LABEL ) + sfx, true, true );

			EngineUtil.getInstance().clone( from, metadata, true );
		}

		finally {
			if ( fromWasOpened ) {
				unmount( from );
			}
		}
	}

	public void clone( IEngine from, DbCloneMetadata metadata, boolean addToRepoList )
			throws RepositoryException, IOException, EngineManagementException {

		MetadataQuery mq = new MetadataQuery();
		Map<URI, Value> oldmetadata = new HashMap<>();
		try {
			oldmetadata = from.query( mq );
		}
		catch ( QueryEvaluationException | MalformedQueryException meq ) {
			log.error( "no metadata to clone", meq );
		}

		URI reification = ( oldmetadata.containsKey( VAS.ReificationModel )
				? URI.class.cast( oldmetadata.get( VAS.ReificationModel ) )
				: VAS.VASEMOSS_Reification );

		EngineCreateBuilder ecb
				= new EngineCreateBuilder( metadata.getLocation(), metadata.getName() );
		ecb.setReificationModel( ReificationStyle.fromUri( reification ) );

		File newsmss = EngineUtil.createNew( ecb, null );
		IEngine neweng = GuiUtility.loadEngine( newsmss );

		merge( from, neweng, metadata.isData(), false );

		// copy insights
		if ( metadata.isInsights() ) {
			neweng.updateInsights( from.getInsightManager() );
		}

		EngineUtil.makeNewMetadata( from, neweng, metadata.getTitle() );
		GuiUtility.closeEngine( neweng );
		mount( newsmss, addToRepoList, true, new LocalUserImpl() );
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
			boolean noDupeEx, User user ) throws EngineManagementException {
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
		openusers.put( smssfile, user );
		notify();
	}

	public void mount( File smssfile, boolean updateRepoList )
			throws EngineManagementException {
		mount( smssfile, updateRepoList, new LocalUserImpl() );
	}

	public void mount( File smssfile, boolean updateRepoList, User user )
			throws EngineManagementException {
		mount( smssfile, updateRepoList, false, user );
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
					new InsightsImportConfig( from.getInsightManager(), false ) );
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
	 * @param ecb how the new database should be created
	 * @param conformanceErrors if not null, conformance will be checked, and
	 * errors will be placed here
	 *
	 * @return the newly-created smss file, or null if something goes wrong
	 *
	 * @throws java.io.IOException
	 * @throws gov.va.semoss.rdf.engine.util.EngineManagementException
	 */
	public static File createNew( EngineCreateBuilder ecb, ImportData conformanceErrors )
			throws IOException, EngineManagementException {

		File dbtopdir = ecb.getEngineDir();
		dbtopdir.mkdirs();

		User user = new LocalUserImpl();
		File smssfile = createEngine( ecb, user );

		IEngine bde = EngineUtil2.loadEngine( smssfile.getAbsoluteFile() );
		Security.getSecurity().associateUser( bde, user );

		if ( null != ecb.getQuestions() ) {
			InsightManagerImpl imi = new InsightManagerImpl();
			createInsightStatements( ecb.getQuestions(), imi );
			bde.updateInsights( imi );
		}

		EngineLoader el = new EngineLoader( ecb.isStageInMemory() );
		el.setDefaultBaseUri( ecb.getDefaultBaseUri(), ecb.isDefaultBaseOverridesFiles() );

		try {
			el.loadToEngine( ecb.getFiles(), bde, ecb.isDoMetamodel(), conformanceErrors );
			if ( ecb.isCalcInfers() ) {
				bde.calculateInferences();
			}
		}
		catch ( ImportValidationException | RepositoryException e ) {
			throw new EngineManagementException( e.getMessage(), e );
		}
		finally {
			el.release();
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
	 * @param vocabs list of vocabularies to import
	 *
	 * @throws IOException
	 * @throws EngineManagementException
	 */
	public synchronized void importInsights( IEngine engine, File insightsfile,
			boolean clearfirst, Collection<URL> vocabs ) throws IOException, EngineManagementException {
		InsightManagerImpl imi = new InsightManagerImpl();
		if ( null != insightsfile ) {
			createInsightStatements( insightsfile, imi );
		}

		insightqueue.put( engine, new InsightsImportConfig( imi, clearfirst ) );
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
	public synchronized boolean importInsights( IEngine engine, InsightManager wim ) {
		try {
			insightqueue.put( engine, new InsightsImportConfig( wim, true ) );
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

	private static void makeNewMetadata( final IEngine from, final IEngine to,
			String title ) throws RepositoryException {
		try {
			final ValueFactory vf = new ValueFactoryImpl();
			final Map<URI, Value> oldmetas = from.query( new MetadataQuery() );
			final URI newbase = to.getBaseUri();
			Date now = new Date();
			oldmetas.put( VAC.SOFTWARE_AGENT,
					vf.createLiteral( System.getProperty( "build.name", "unknown" ) ) );
			oldmetas.put( MetadataConstants.DCT_CREATED, vf.createLiteral( now ) );
			oldmetas.put( MetadataConstants.DCT_MODIFIED, vf.createLiteral( now ) );
			oldmetas.put( RDFS.LABEL, vf.createLiteral( title ) );
			oldmetas.remove( VAS.Database );

			to.execute( new ModificationExecutorAdapter( true ) {

				@Override
				public void exec( RepositoryConnection conn ) throws RepositoryException {
					for ( Map.Entry<URI, Value> en : oldmetas.entrySet() ) {
						Statement s = new StatementImpl( newbase, en.getKey(), en.getValue() );
						conn.add( EngineLoader.cleanStatement( s, vf ) );
					}
				}
			} );
		}
		catch ( MalformedQueryException | QueryEvaluationException e ) {
			log.error( e, e );
		}
	}

	private static File createEngine( EngineCreateBuilder ecb, User user )
			throws IOException, EngineManagementException {

		String dbname = ecb.getEngineName();
		File enginedir = ecb.getEngineDir();
		File modelmap = ecb.getMap();

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
		File modelsmss = ecb.getSmss();
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

			if ( ecb.isDoMetamodel() ) {
				rc.begin();
				for ( URL url : ecb.getVocabularies() ) {
					rc.add( getStatementsFromResource( url, RDFFormat.TURTLE ) );
				}
				rc.commit();
			}

			URI baseuri = AbstractEngine.getNewBaseUri();

			// add the metadata
			rc.begin();
			ValueFactory vf = rc.getValueFactory();
			rc.add( new StatementImpl( baseuri, RDF.TYPE, VAS.Database ) );
			Date today = new Date();
			rc.add( new StatementImpl( baseuri, MetadataConstants.DCT_CREATED,
					vf.createLiteral( QueryExecutorAdapter.getCal( today ) ) ) );
			rc.add( new StatementImpl( baseuri, MetadataConstants.DCT_MODIFIED,
					vf.createLiteral( QueryExecutorAdapter.getCal( today ) ) ) );

			rc.add( new StatementImpl( baseuri, VAS.ReificationModel,
					ecb.getReificationModel().uri ) );

			rc.add( new StatementImpl( baseuri, VAC.SOFTWARE_AGENT,
					vf.createLiteral( System.getProperty( "build.name", "unknown" ) ) ) );

			String username = user.getProperty( User.UserProperty.USER_FULLNAME );
			String email = user.getProperty( User.UserProperty.USER_EMAIL );
			String org = user.getProperty( User.UserProperty.USER_ORG );

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

			rc.close();
			repo.shutDown();
		}
		catch ( RepositoryException e ) {
			log.error( e, e );
			return null;
		}

		return jnl;
	}

	private static void createInsightStatements( File modelquestions,
			InsightManagerImpl imi ) throws IOException, EngineManagementException {

		if ( null == modelquestions || !modelquestions.exists() ) {
			return;
		}

		Map<String, RDFFormat> extfmt = new HashMap<>();

		extfmt.put( "ttl", RDFFormat.TURTLE );
		extfmt.put( "rdf", RDFFormat.RDFXML );
		extfmt.put( "n3", RDFFormat.N3 );
		extfmt.put( "nt", RDFFormat.NTRIPLES );

		// we need to check that we actually loaded SOME perspectives, so we'll load
		// a temporary InsightManager first
		InsightManagerImpl loader = new InsightManagerImpl();

		try {
			Repository repo = new SailRepository( new MemoryStore() );
			repo.initialize();
			if ( FilenameUtils.isExtension( modelquestions.toString(), extfmt.keySet() ) ) {
				RepositoryConnection rc = repo.getConnection();
				rc.add( modelquestions, "",
						extfmt.get( FilenameUtils.getExtension( modelquestions.toString() ) ) );
				loader.loadFromRepository( rc );
				rc.close();
				repo.shutDown();
			}
			else {
				Properties p = Utility.loadProp( modelquestions );
				loader.loadLegacyData( p );
			}
		}
		catch ( RepositoryException | RDFParseException e ) {
			throw new EngineManagementException( ErrorCode.FILE_ERROR,
					e );
		}

		boolean ok = !loader.getPerspectives().isEmpty();

		if ( !ok ) {
			throw new EngineManagementException( ErrorCode.MISSING_REQUIRED_TUPLE,
					modelquestions + " does not contain any Perspectives" );
		}

		imi.addAll( loader.getPerspectives(), false );
	}

	private static List<Statement> getStatementsFromResource( URL resource,
			RDFFormat fmt ) {
		List<Statement> stmts = new ArrayList<>();

		Repository repo = null;
		RepositoryConnection rc = null;

		try ( InputStream is = resource.openStream() ) {
			repo = new SailRepository( new MemoryStore() );
			repo.initialize();
			rc = repo.getConnection();

			rc.add( is, Constants.DEFAULT_SEMOSS_URI, fmt );
			rc.commit();
			stmts.addAll( Iterations.asList( rc.getStatements( null, null, null, false ) ) );
		}
		catch ( Exception e ) {
			log.warn( "could not open/parse model resource: " + resource, e );
		}
		finally {
			if ( null != rc ) {
				try {
					rc.close();
				}
				catch ( Exception e ) {
					log.warn( "could not remove temp rc", e );
				}
			}
			if ( null != repo ) {
				try {
					repo.shutDown();
				}
				catch ( Exception e ) {
					log.warn( "could not remove temp rc" , e );
				}
			}
		}

//		log.debug( "subjects from resource: " + resource );
//		Set<Resource> uris = new HashSet<>();
//		for ( Statement s : stmts ) {
//			uris.add( s.getSubject() );
//		}
//		for ( Resource u : uris ) {
//			log.debug( u );
//		}

		return stmts;
	}

	public static class DbCloneMetadata {

		private final File location;
		private final String name;
		private final String title;
		private final boolean config;
		private final boolean data;

		public DbCloneMetadata( File location, String dbname, String title,
				boolean config, boolean data ) {
			this.location = location;
			this.title = title;
			this.name = dbname;
			this.config = config;
			this.data = data;
		}

		public File getLocation() {
			return location;
		}

		public String getTitle() {
			return title;
		}

		public String getName() {
			return name;
		}

		public boolean isInsights() {
			return config;
		}

		public boolean isData() {
			return data;
		}
	}

	private static class InsightsImportConfig {

		public final InsightManager im;
		public final boolean clearfirst;

		public InsightsImportConfig( InsightManager im, boolean clear ) {
			this.im = im;
			clearfirst = clear;
		}
	}
}
