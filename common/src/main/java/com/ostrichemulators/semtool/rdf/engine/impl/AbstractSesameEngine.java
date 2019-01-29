/**
 * *****************************************************************************
 * Copyright 2013 SEMOSS.ORG
 *
 * This file is part of SEMOSS.
 *
 * SEMOSS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SEMOSS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SEMOSS. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package com.ostrichemulators.semtool.rdf.engine.impl;

import com.ostrichemulators.semtool.model.vocabulary.SEMTOOL;
import com.ostrichemulators.semtool.rdf.engine.api.Bindable;
import info.aduna.iteration.Iterations;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import com.ostrichemulators.semtool.util.Constants;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;
import com.ostrichemulators.semtool.rdf.engine.api.InsightManager;
import com.ostrichemulators.semtool.rdf.engine.api.MetadataConstants;
import com.ostrichemulators.semtool.rdf.engine.api.ModificationExecutor;
import com.ostrichemulators.semtool.rdf.engine.api.QueryExecutor;
import com.ostrichemulators.semtool.rdf.engine.api.UpdateExecutor;
import com.ostrichemulators.semtool.rdf.query.util.MetadataQuery;
import com.ostrichemulators.semtool.rdf.query.util.QueryExecutorAdapter;
import com.ostrichemulators.semtool.rdf.query.util.impl.VoidQueryAdapter;
import com.ostrichemulators.semtool.user.Security;
import com.ostrichemulators.semtool.user.User;
import com.ostrichemulators.semtool.util.UriBuilder;
import com.ostrichemulators.semtool.util.Utility;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;
import org.openrdf.model.Model;
import org.openrdf.model.Namespace;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.query.impl.DatasetImpl;

/**
 * An Abstract Engine that sets up the base constructs needed to create an
 * engine.
 */
public abstract class AbstractSesameEngine extends AbstractEngine {

	private static final Logger log = Logger.getLogger( AbstractSesameEngine.class );
	public static final String REPOSITORY_KEY = "repository";
	public static final String INSIGHTS_KEY = "insights";

	public AbstractSesameEngine() {
	}

	protected RepositoryConnection createOwlRc() throws RepositoryException {
		ForwardChainingRDFSInferencer inferencer
				= new ForwardChainingRDFSInferencer( new MemoryStore() );
		SailRepository owlRepo = new SailRepository( inferencer );
		owlRepo.initialize();
		return owlRepo.getConnection();
	}

	/**
	 * Initiates the loading process with the given properties. Subclasses will
	 * usually use this function to open their repositories before the rest of the
	 * loading process occurs. If overridden, subclasses should be sure to call
	 * their superclass's version of this function in addition to whatever other
	 * processing they do.
	 *
	 * @param props
	 * @throws RepositoryException
	 */
	@Override
	protected void startLoading( Properties props ) throws RepositoryException {
		createRc( props );
		super.startLoading( props );
	}

	/**
	 * An extension point for subclasses to create their RepositoryConnection
	 *
	 * @param props
	 * @throws RepositoryException
	 */
	protected abstract void createRc( Properties props ) throws RepositoryException;

	@Override
	protected URI setUris( String data, String schema ) throws RepositoryException {
		URI baseuri = null;
		if ( data.isEmpty() ) {
			// if the baseuri isn't already set, then query the kb for void:Dataset
			RepositoryResult<Statement> rr
					= getRawConnection().getStatements( null, RDF.TYPE, SEMTOOL.Database, false );
			List<Statement> stmts = Iterations.asList( rr );
			for ( Statement s : stmts ) {
				baseuri = URI.class.cast( s.getSubject() );
				break;
			}

			if ( null == baseuri ) {
				// not set yet, so make one (this is a silent upgrade)
				RepositoryConnection rc = getRawConnection();
				rc.begin();
				try {
					baseuri = silentlyUpgrade( rc );
					rc.commit();
				}
				catch ( RepositoryException e ) {
					log.error( e, e );
					rc.rollback();
				}
			}
		}
		else {
			baseuri = new URIImpl( data );
		}

		if ( null == baseuri ) {
			log.fatal( "no base uri set" );
		}

		setSchemaBuilder( UriBuilder.getBuilder( schema ) );
		setDataBuilder( UriBuilder.getBuilder( baseuri ) );
		return baseuri;
	}

	protected URI silentlyUpgrade( RepositoryConnection rc ) throws RepositoryException {
		URI baseuri = getNewBaseUri();
		rc.add( baseuri, RDF.TYPE, SEMTOOL.Database );

		// see if we have some old metadata we can move over, too
		VoidQueryAdapter q = new VoidQueryAdapter( "SELECT ?pred ?val { ?uri a ?voidds . ?uri ?pred ?val}" ) {

			@Override
			public void handleTuple( BindingSet set, ValueFactory fac ) {
				URI pred = URI.class.cast( set.getValue( "pred" ) );
				if ( !( MetadataConstants.OWLIRI.equals( pred ) || RDF.TYPE.equals( pred )
						|| OWL.VERSIONINFO.equals( pred ) ) ) {

					try {
						rc.add( baseuri, pred, set.getValue( "val" ) );
					}
					catch ( RepositoryException re ) {
						log.warn( "Could not move metadata to new URI", re );
					}
				}
			}
		};
		q.bind( "voidds", MetadataConstants.VOID_DS );
		try {
			query( q );
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			log.error( e, e );
		}
		return baseuri;
	}

	@Override
	protected void finishLoading( Properties props ) throws RepositoryException {
		String realname = ( null == getEngineName()
				? props.getProperty( Constants.ENGINE_NAME,
						FilenameUtils.getBaseName( props.getProperty( Constants.SMSS_LOCATION ) ) )
				: getEngineName() );
		MetadataQuery mq = new MetadataQuery( RDFS.LABEL );
		queryNoEx( mq );
		String str = mq.getString();
		if ( null != str ) {
			realname = str;
		}
		setEngineName( realname );

		RepositoryConnection rc = getRawConnection();
		rc.begin();
		for ( Map.Entry<String, String> en : Utility.DEFAULTNAMESPACES.entrySet() ) {
			rc.setNamespace( en.getKey(), en.getValue() );
		}
		rc.commit();
	}

	@Override
	protected InsightManager createInsightManager() throws RepositoryException {
		log.debug( "creating default (in-memory) insight repository" );
		return new InsightManagerImpl();
	}

	@Override
	public void closeDB() {
		log.debug( "closing db: " + getEngineName() );

		if ( null != getRawConnection() ) {
			RepositoryConnection rc = getRawConnection();
			if ( null != rc ) {
				try {
					rc.close();
				}
				catch ( Exception e ) {
					log.warn( "could not close repo connection", e );
				}

				try {
					rc.getRepository().shutDown();
				}
				catch ( Exception e ) {
					log.warn( "could not close repo", e );
				}
			}
		}
	}

	@Override
	public boolean isConnected() {
		try {
			return getRawConnection().isOpen();
		}
		catch ( RepositoryException e ) {
			return false;
		}
	}

	public static String processNamespaces( String rawsparql,
			Map<String, String> customNamespaces ) {
		Map<String, String> namespaces = new HashMap<>( Utility.DEFAULTNAMESPACES );
		namespaces.putAll( customNamespaces );

		Set<String> existingNamespaces = new HashSet<>();
		if ( rawsparql.toUpperCase().contains( "PREFIX" ) ) {
			Pattern pat = Pattern.compile( "prefix[\\s]+([A-Za-z0-9_-]+)[\\s]*:",
					Pattern.CASE_INSENSITIVE );
			Matcher m = pat.matcher( rawsparql );
			while ( m.find() ) {
				existingNamespaces.add( m.group( 1 ) );
			}
		}

		StringBuilder sparql = new StringBuilder();
		for ( Map.Entry<String, String> en : namespaces.entrySet() ) {
			if ( !existingNamespaces.contains( en.getKey() ) ) {
				sparql.append( "PREFIX " ).append( en.getKey() );
				sparql.append( ": <" ).append( en.getValue() ).append( "> " );
			}
		}

		sparql.append( rawsparql );
		return sparql.toString();
	}

	public static final void doUpdate( UpdateExecutor query,
			RepositoryConnection rc, boolean dobindings ) throws RepositoryException,
			MalformedQueryException, UpdateExecutionException {

		String sparql = processNamespaces( dobindings ? query.getSparql()
				: query.bindAndGetSparql(), query.getNamespaces() );

		ValueFactory vfac = new ValueFactoryImpl();
		Update upd = rc.prepareUpdate( QueryLanguage.SPARQL, sparql );

		if ( dobindings ) {
			upd.setIncludeInferred( query.usesInferred() );
			query.setBindings( upd, vfac );
		}

		upd.execute();
		query.done();
	}

	public static final <T> T getSelect( QueryExecutor<T> query,
			RepositoryConnection rc, boolean dobindings ) throws RepositoryException,
			MalformedQueryException, QueryEvaluationException {

		String sparql = processNamespaces( dobindings ? query.getSparql()
				: query.bindAndGetSparql(), query.getNamespaces() );

		ValueFactory vfac = new ValueFactoryImpl();
		TupleQuery tq = rc.prepareTupleQuery( QueryLanguage.SPARQL, sparql );

		if ( null != query.getContext() ) {
			DatasetImpl dataset = new DatasetImpl();
			dataset.addDefaultGraph( query.getContext() );
			tq.setDataset( dataset );
		}

		if ( dobindings ) {
			tq.setIncludeInferred( query.usesInferred() );
			query.setBindings( tq, vfac );
		}

		TupleQueryResult rslt = tq.evaluate();
		query.start( rslt.getBindingNames() );
		while ( rslt.hasNext() ) {
			query.handleTuple( rslt.next(), vfac );
		}
		query.done();
		rslt.close();
		return query.getResults();
	}

	protected abstract RepositoryConnection getRawConnection();

	public static final <T> T getSelectNoEx( QueryExecutor<T> query,
			RepositoryConnection rc, boolean dobindings ) {
		try {
			return getSelect( query, rc, dobindings );
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			log.error( "could not execute select: " + query.getSparql(), e );
			return null;
		}
	}

	public static Model getConstruct( QueryExecutor<Model> query,
			RepositoryConnection rc, boolean dobindings )
			throws RepositoryException, MalformedQueryException, QueryEvaluationException {

		String sparql = processNamespaces( dobindings ? query.getSparql()
				: query.bindAndGetSparql(), query.getNamespaces() );

		GraphQuery tq = rc.prepareGraphQuery( QueryLanguage.SPARQL, sparql );
		tq.setIncludeInferred( query.usesInferred() );
		if ( dobindings ) {
			query.setBindings( tq, rc.getValueFactory() );
		}

		GraphQueryResult gqr = tq.evaluate();
		while ( gqr.hasNext() ) {
			query.getResults().add( gqr.next() );
		}
		gqr.close();

		return query.getResults();
	}

	private void addUserNamespaces( Bindable ab ) {
		User user = Security.getSecurity().getAssociatedUser( this );
		ab.addNamespaces( user.getNamespaces() );
	}

	@Override
	public <T> T query( QueryExecutor<T> exe )
			throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		if ( isConnected() ) {
			addUserNamespaces( exe );
			RepositoryConnection rc = getRawConnection();
			return getSelect( exe, rc, supportsSparqlBindings() );
		}

		throw new RepositoryException( "The engine is not connected" );
	}

	@Override
	public <T> T queryNoEx( QueryExecutor<T> exe ) {
		if ( isConnected() ) {
			addUserNamespaces( exe );
			RepositoryConnection rc = getRawConnection();
			return getSelectNoEx( exe, rc, supportsSparqlBindings() );
		}

		return null;
	}

	@Override
	public void update( UpdateExecutor ue ) throws RepositoryException,
			MalformedQueryException, UpdateExecutionException {
		if ( isConnected() ) {
			addUserNamespaces( ue );
			RepositoryConnection rc = getRawConnection();
			doUpdate( ue, rc, supportsSparqlBindings() );
			updateLastModifiedDate( rc, getBaseIri() );
			logProvenance( ue );
		}
	}

	@Override
	public Model construct( QueryExecutor<Model> q ) throws RepositoryException,
			MalformedQueryException, QueryEvaluationException {
		addUserNamespaces( q );
		return getConstruct( q, getRawConnection(), supportsSparqlBindings() );
	}

	@Override
	public Model constructNoEx( QueryExecutor<Model> q ) {
		addUserNamespaces( q );
		try {
			return getConstruct( q, getRawConnection(), supportsSparqlBindings() );
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			log.error( "could not execute construct: " + q.getSparql(), e );
			return null;
		}
	}

	@Override
	public void execute( ModificationExecutor exe ) throws RepositoryException {
		RepositoryConnection rc = getRawConnection();

		try {
			if ( exe.execInTransaction() ) {
				rc.begin();
			}

			exe.exec( rc );

			if ( exe.execInTransaction() ) {
				rc.commit();
			}
		}
		catch ( RepositoryException e ) {
			if ( exe.execInTransaction() ) {
				rc.rollback();
			}

			throw e;
		}
	}

	/**
	 * Does this engine support binding variables within the Sparql execution?
	 *
	 * @return true, if the engine supports sparql variable binding
	 */
	@Override
	public boolean supportsSparqlBindings() {
		return true;
	}

	public static void updateLastModifiedDate( RepositoryConnection rc,
			Resource baseuri ) {
		// updates the base uri's last modified key
		// 1) if we don't know it already, figure out what our base uri is
		// 2) remove any last modified value
		// 3) add the new last modified value

		ValueFactory vf = rc.getValueFactory();
		try {
			if ( null == baseuri ) {
				RepositoryResult<Statement> rr = rc.getStatements( null, RDF.TYPE,
						SEMTOOL.Database, false );
				List<Statement> stmts = Iterations.asList( rr );
				for ( Statement s : stmts ) {
					baseuri = s.getSubject();
				}
			}

			if ( null == baseuri ) {
				log.warn( "cannot update last modified date when no base uri is set" );
			}
			else {
				rc.remove( baseuri, MetadataConstants.DCT_MODIFIED, null );

				rc.add( new StatementImpl( baseuri, MetadataConstants.DCT_MODIFIED,
						vf.createLiteral( QueryExecutorAdapter.getCal( new Date() ) ) ) );
			}
		}
		catch ( RepositoryException e ) {
			log.warn( "could not update last modified date", e );
		}
	}

	@Override
	public void commit() {
		try {
			RepositoryConnection rc = getRawConnection();
			// updateLastModifiedDate();
			rc.commit();
		}
		catch ( Exception e ) {
			log.error( e, e );
		}
	}

	@Override
	public Map<String, String> getNamespaces() {
		Map<String, String> ret = new HashMap<>();
		try {
			RepositoryConnection rc = getRawConnection();

			for ( Namespace ns : Iterations.asList( rc.getNamespaces() ) ) {
				ret.put( ns.getPrefix(), ns.getName() );
			}
		}
		catch ( RepositoryException re ) {
			log.warn( "could not retrieve namespaces", re );
		}
		return ret;
	}

	@Override
	protected void updateLastModifiedDate() {
		RepositoryConnection rc = getRawConnection();
		updateLastModifiedDate( rc, getBaseIri() );
	}
}
