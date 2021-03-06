package com.ostrichemulators.semtool.util;

import com.ostrichemulators.semtool.om.GraphElement;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.engine.api.ModificationExecutor;
import com.ostrichemulators.semtool.rdf.query.util.ModificationExecutorAdapter;
import com.ostrichemulators.semtool.rdf.query.util.impl.StatementAddingExecutor;
import com.ostrichemulators.semtool.ui.components.OperationsProgress;
import com.ostrichemulators.semtool.ui.main.PlayPane;
import com.ostrichemulators.semtool.ui.components.ProgressTask;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

public class StatementPersistenceUtility {

	private static final Logger log = Logger.getLogger( StatementPersistenceUtility.class );

	public static void deleteStatement( IEngine engine, Statement statement ) {
		Collection<Statement> statements = new HashSet<>();
		statements.add( statement );

		deleteStatements( engine, statements );
	}

	public static void deleteStatements( IEngine engine, Collection<Statement> statements ) {
		ProgressTask pt = new ProgressTask( "Deleting Statements from the Knowledge Base",
				new Runnable() {
					@Override
					public void run() {
						try {
							ModificationExecutor mea = new ModificationExecutorAdapter() {
								@Override
								public void exec( RepositoryConnection conn ) throws RepositoryException {
									conn.begin();
									conn.remove( statements );
								}
							};

							engine.execute( mea );
							engine.commit();
						}
						catch ( RepositoryException re ) {
							log.error( "RepositoryException trying to delete Statements: " + re, re );
						}
					}
				} );
		OperationsProgress.getInstance( PlayPane.UIPROGRESS ).add( pt );
	}

	public static void saveStatement( IEngine engine, Statement statement ) {
		Collection<Statement> statements = new HashSet<>();
		statements.add( statement );

		saveStatements( engine, statements );
	}

	public static void saveStatements( IEngine engine, Collection<Statement> statements ) {
		ProgressTask pt = new ProgressTask( "Saving Statements to the Knowledge Base",
				new Runnable() {
					@Override
					public void run() {
						try {
							engine.execute( new StatementAddingExecutor( statements ) );
						}
						catch ( RepositoryException re ) {
							log.error( "RepositoryException trying to save Statements: " + re, re );
						}
					}
				} );
		OperationsProgress.getInstance( PlayPane.UIPROGRESS ).add( pt );
	}

	public static void deleteNodeOrEdgeProperty( IEngine engine, GraphElement nodeOrEdge,
			IRI name, Value value ) {
		Value subject = nodeOrEdge.getIRI();
		if ( !( subject instanceof Resource ) ) {
			log.warn( "Trying to delete property with name: " + name + " and value: " + value
					+ ", but subject: " + subject + " is not an instanceof Resource." );
			return;
		}

		deleteStatement( engine, SimpleValueFactory.getInstance().createStatement( (Resource) subject, name, value ) );
	}

	public static void saveNodeOrEdgeProperty( IEngine engine, GraphElement nodeOrEdge,
			IRI name, Value value ) {
		Value subject = nodeOrEdge.getIRI();
		if ( !( subject instanceof Resource ) ) {
			log.warn( "Trying to save property with name: " + name + " and value: "
					+ value + ", but subject: " + subject + " is not an instanceof Resource." );
			return;
		}

		saveStatement( engine, SimpleValueFactory.getInstance().createStatement( (Resource) subject, name, value ) );
	}

	public static void updateNodeOrEdgePropertyValue( IEngine engine, 
			GraphElement nodeOrEdge, IRI name, Value oldValue, Value newValue ) {
		deleteNodeOrEdgeProperty( engine, nodeOrEdge, name, oldValue );
		saveNodeOrEdgeProperty( engine, nodeOrEdge, name, newValue );
	}
}
