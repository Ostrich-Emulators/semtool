/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.poi.main;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.api.MetadataConstants;
import gov.va.semoss.rdf.query.util.MetadataQuery;
import gov.va.semoss.util.UriBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public final class ImportMetadata {

	private static final Logger log = Logger.getLogger( ImportMetadata.class );
	private final Map<String, String> namespaces = new HashMap<>();
	private final Map<URI, String> extras = new HashMap<>();
	private final List<Statement> statements = new ArrayList<>();
	private URI base;
	private UriBuilder databuilder;
	private UriBuilder schemabuilder;
	private boolean autocreateModel = true;
	private boolean legacy = false;

	public ImportMetadata() {
	}

	public ImportMetadata( URI base, UriBuilder schema, UriBuilder data ) {
		this.base = base;
		this.databuilder = data;
		this.schemabuilder = schema;
	}

	public static ImportMetadata forEngine( IEngine eng ) {
		if ( null == eng ) {
			return new ImportMetadata();
		}

		ImportMetadata metas = new ImportMetadata( eng.getBaseUri(),
				eng.getSchemaBuilder(), eng.getDataBuilder() );
		metas.setNamespaces( eng.getNamespaces() );

		try {
			MetadataQuery mq = new MetadataQuery();
			metas.setExtras( eng.query( mq ) );
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			log.error( e, e );
		}

		return metas;
	}

	public void clear() {
		namespaces.clear();
		statements.clear();
		extras.clear();
	}

	public void setAll( ImportMetadata im ) {
		setNamespaces( im.getNamespaces() );
		setExtras( im.getExtras() );
		setStatements( im.getStatements() );
		base = im.getBase();
		databuilder = im.getDataBuilder();
		schemabuilder = im.getSchemaBuilder();
		autocreateModel = im.isAutocreateMetamodel();
	}

	public void setExtras( Map<URI, String> exs ) {
		extras.clear();
		for ( Map.Entry<URI, String> en : exs.entrySet() ) {
			// don't write the void#dataset value, because it's the same for every KB
			if ( !en.getKey().equals( MetadataConstants.VOID_DS ) ) {
				extras.put( en.getKey(), en.getValue() );
			}
		}
	}

	public boolean isLegacyMode() {
		return legacy;
	}

	public void setLegacyMode( boolean legacy ) {
		this.legacy = legacy;
	}

	public Map<URI, String> getExtras() {
		return extras;
	}

	public void addExtra( URI sub, String val ) {
		extras.put( sub, val );
	}

	public ImportMetadata setNamespaces( Map<String, String> ns ) {
		namespaces.clear();
		return addNamespaces( ns );
	}

	public ImportMetadata addNamespaces( Map<String, String> ns ) {
		for ( Map.Entry<String, String> en : ns.entrySet() ) {
			setNamespace( en.getKey(), en.getValue() );
		}
		return this;
	}

	public ImportMetadata setNamespace( String prefix, String uri ) {
		// if the prefix ends with a :, remove it
		namespaces.put( prefix.replaceAll( ":$", "" ), uri );
		return this;
	}

	public Map<String, String> getNamespaces() {
		return new HashMap<>( namespaces );
	}

	public void setAutocreateMetamodel( boolean b ) {
		autocreateModel = b;
	}

	public boolean isAutocreateMetamodel() {
		return autocreateModel;
	}

	public void setBase( URI b ) {
		base = b;
	}

	public void setDataBuilder( String uri ) {
		databuilder = ( null == uri ? null : UriBuilder.getBuilder( uri ) );
	}

	public void setSchemaBuilder( String uri ) {
		schemabuilder = ( null == uri ? null : UriBuilder.getBuilder( uri ) );
	}

	public URI getBase() {
		return base;
	}

	public UriBuilder getDataBuilder() {
		return ( null == databuilder ? null : databuilder.copy() );
	}

	public UriBuilder getSchemaBuilder() {
		return ( null == schemabuilder ? null : schemabuilder.copy() );
	}

	public Collection<Statement> getStatements() {
		return statements;
	}

	public ImportMetadata setStatements( Collection<Statement> stmts ) {
		statements.clear();
		return addAll( stmts );
	}

	public void add( Statement s ) {
		statements.add( s );
	}

	public ImportMetadata addAll( Collection<Statement> stmts ) {
		statements.addAll( stmts );
		return this;
	}
}
