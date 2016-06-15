/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.impl;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.ostrichemulators.semtool.rdf.engine.api.ModificationExecutor;
import com.ostrichemulators.semtool.util.Constants;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.nativerdf.NativeStore;

/**
 * A class to handle reading TDB files. Instead of maintaining an internal jena
 * dataset, this class loads everything from the TDB into a separate OpenRDF
 * repository (in-mem or on disk) on startup, and copies it back (if necessary)
 * on close. Unfortunately, this means startup and shutdown can take a long
 * time, but at least all the query interfaces are consistent across all
 * engines.
 *
 * @author ryan
 */
public class JenaEngine extends AbstractSesameEngine {

	private static final Logger log = Logger.getLogger( JenaEngine.class );
	public static final String FILE_PROP = "file";
	public static final String INMEM_PROP = "inmem";
	private RepositoryConnection rc;
	private File datadir;
	private File tdbdir;
	private boolean needsSave = false;

	public JenaEngine() {
	}

	public JenaEngine( Dataset dataset ) throws RepositoryException {
		openDB( new Properties() );
		tdbdir = null;
		try {
			copyFromTdb( dataset );
		}
		catch ( Exception e ) {
			log.fatal( e, e );
		}
	}

	@Override
	protected void createRc( Properties props ) throws RepositoryException {
		boolean inmem = Boolean.parseBoolean( props.getProperty( INMEM_PROP,
				Boolean.toString( true ) ) );
		needsSave = false;
		SailRepository repo;
		if ( inmem ) {
			repo = new SailRepository( new MemoryStore() );
		}
		else {
			try {
				datadir = File.createTempFile( "jena-temp-", ".rdf" );
				datadir.delete();
				datadir.mkdirs();
				datadir.deleteOnExit();
				repo = new SailRepository( new NativeStore( datadir ) );
			}
			catch ( IOException ioe ) {
				log.error( "could not create on-disk data store...using memory", ioe );
				repo = new SailRepository( new MemoryStore() );
			}
		}

		try {
			repo.initialize();
			rc = repo.getConnection();
		}
		catch ( Exception e ) {
			try {
				repo.shutDown();
			}
			catch ( Exception ex ) {
				log.error( ex, ex );
			}
		}

		if ( props.containsKey( FILE_PROP ) ) {
			copyFromTdb( props.getProperty( FILE_PROP ) );
		}
	}

	private void copyFromTdb( Dataset dataset ) throws RepositoryException {
		ValueFactory vf = rc.getValueFactory();

		if ( dataset.supportsTransactions() ) {
			dataset.begin( ReadWrite.READ );
		}

		// Get model inside the transaction
		Model model = dataset.getDefaultModel();
		StmtIterator si = model.listStatements();

		try {
			rc.begin();
			while ( si.hasNext() ) {
				Statement stmt = si.next();
				com.hp.hpl.jena.rdf.model.Resource rsr = stmt.getSubject();
				Property pred = stmt.getPredicate();
				RDFNode val = stmt.getObject();
				Node valnode = val.asNode();
				
				Resource sub;
				try {
					sub = ( rsr.isAnon()
							? vf.createBNode( valnode.getBlankNodeLabel() )
							: vf.createURI( rsr.toString() ) );
				}
				catch ( UnsupportedOperationException uoo ) {
					log.warn( uoo, uoo );
					continue;
				}
				
				URI pred2 = vf.createURI( pred.toString() );
				Value val2;

				if ( val.isLiteral() ) {
					Literal lit = val.asLiteral();
					String dtstr = lit.getDatatypeURI();
					URI dt = ( null == dtstr ? null : vf.createURI( dtstr ) );
					String langstr = lit.getLanguage();

					if ( null == dt ) {
						if ( langstr.isEmpty() ) {
							val2 = vf.createLiteral( lit.toString() );
						}
						else {
							val2 = vf.createLiteral( lit.toString(), langstr );
						}
					}
					else {
						val2 = vf.createLiteral( lit.toString(), dt );
					}
				}
				else {
					if ( val.isAnon() ) {
						val2 = vf.createBNode( valnode.getBlankNodeLabel() );
					}
					else {
						val2 = vf.createURI( val.toString() );
					}
				}
				rc.add( sub, pred2, val2 );
			}
			rc.commit();
		}
		catch ( RepositoryException re ) {
			rc.rollback();
			throw re;
		}
		finally {
			if ( dataset.supportsTransactions() ) {
				dataset.end();
			}
		}
	}

	private void copyFromTdb( String file ) throws RepositoryException {
		tdbdir = new File( file );

		Dataset dataset = TDBFactory.createDataset( file );
		try {
			copyFromTdb( dataset );
		}
		finally {
			dataset.close();
		}
	}

	private void copyToTdb() throws RepositoryException {
		if ( !needsSave || null == tdbdir ) {
			return;
		}

		final Dataset dataset = TDBFactory.createDataset( tdbdir.getAbsolutePath() );

		try {
			rc.export( new TdbExporter( dataset ) );
		}
		catch ( RepositoryException | RDFHandlerException e ) {
			log.error( "Problem exporting data to TDB", e );
			dataset.abort();
		}
		finally {
			dataset.close();
		}
	}

	@Override
	protected RepositoryConnection getRawConnection() {
		return rc;
	}

	public File getShadowFile() {
		return datadir;
	}

	/**
	 * Closes the data base associated with the engine. This will prevent further
	 * changes from being made in the data store and safely ends the active
	 * transactions and closes the engine.
	 */
	@Override
	public void closeDB() {
		try {
			copyToTdb();
		}
		catch ( Exception e ) {
			log.error( "Could not copy data back to TDB file", e );
		}

		try {
			rc.close();
		}
		catch ( Exception e ) {
			log.error( e, e );
		}

		Repository repo = rc.getRepository();
		try {
			repo.shutDown();
		}
		catch ( Exception e ) {
			log.error( e, e );
		}

		FileUtils.deleteQuietly( datadir );
	}

	@Override
	protected void updateLastModifiedDate() {
		super.updateLastModifiedDate();
		needsSave = true;
	}

	@Override
	public void execute( ModificationExecutor exe ) throws RepositoryException {
		super.execute( exe );
		needsSave = true;
	}

	public static Properties generateProperties( File tdb ) {
		Properties props = new Properties();
		props.setProperty( FILE_PROP, tdb.toString() );
		props.setProperty( INMEM_PROP, Boolean.toString( true ) );
		props.setProperty( Constants.SMSS_LOCATION, tdb.getAbsolutePath() );
		props.setProperty( Constants.ENGINE_IMPL, JenaEngine.class.getCanonicalName() );
		props.setProperty( Constants.SMSS_VERSION_KEY, "1.0" );

		return props;
	}

	private class TdbExporter implements RDFHandler {

		private final Dataset dataset;
		private Model model;

		public TdbExporter( Dataset ds ) {
			dataset = ds;
		}

		@Override
		public void startRDF() throws RDFHandlerException {
			dataset.begin( ReadWrite.WRITE );
			model = dataset.getDefaultModel();
			model.removeAll();
		}

		@Override
		public void endRDF() throws RDFHandlerException {
			model.commit();
			dataset.commit();
			model.close();
		}

		@Override
		public void handleNamespace( String string, String string1 ) throws RDFHandlerException {
			model.setNsPrefix( string, string1 );
		}

		@Override
		public void handleStatement( org.openrdf.model.Statement stmt ) throws RDFHandlerException {
			Resource rsr = stmt.getSubject();
			com.hp.hpl.jena.rdf.model.Resource sub;
			if ( rsr instanceof URI ) {
				sub = model.createResource( rsr.stringValue() );
			}
			else {
				BNode node = BNode.class.cast( rsr );
				sub = model.createResource( new AnonId( node.getID() ) );
			}

			Property pred = model.createProperty( stmt.getPredicate().toString() );

			Value val = stmt.getObject();
			RDFNode obj;
			if ( val instanceof URI ) {
				obj = model.createResource( val.stringValue() );
			}
			else if ( val instanceof BNode ) {
				BNode node = BNode.class.cast( val );
				obj = model.createResource( new AnonId( node.getID() ) );
			}
			else if ( val instanceof org.openrdf.model.Literal ) {
				org.openrdf.model.Literal lit = org.openrdf.model.Literal.class.cast( val );
				if ( null == lit.getDatatype() ) {
					if ( null == lit.getLanguage() ) {
						obj = model.createLiteral( val.stringValue() );
					}
					else {
						obj = model.createLiteral( val.stringValue(), lit.getLanguage() );
					}
				}
				else {
					obj = model.createTypedLiteral( lit.toString(),
							lit.getDatatype().stringValue() );
				}
			}
			else {
				throw new RDFHandlerException( "unhandled value type:" + val );
			}

			model.add( sub, pred, obj );
		}

		@Override
		public void handleComment( String string ) throws RDFHandlerException {
		}
	}
}
