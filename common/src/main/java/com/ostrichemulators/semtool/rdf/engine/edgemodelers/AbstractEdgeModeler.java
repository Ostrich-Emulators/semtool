/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.edgemodelers;

import com.ostrichemulators.semtool.model.vocabulary.SEMONTO;
import com.ostrichemulators.semtool.poi.main.ImportData;
import com.ostrichemulators.semtool.poi.main.ImportMetadata;
import com.ostrichemulators.semtool.poi.main.ImportValidationException;
import com.ostrichemulators.semtool.poi.main.ImportValidationException.ErrorType;
import com.ostrichemulators.semtool.poi.main.LoadingSheetData;
import com.ostrichemulators.semtool.rdf.engine.util.QaChecker;
import com.ostrichemulators.semtool.rdf.engine.util.QaChecker.RelationCacheKey;
import com.ostrichemulators.semtool.rdf.engine.util.SemtoolStructureManagerImpl;
import static com.ostrichemulators.semtool.util.RDFDatatypeTools.IRISTARTPATTERN;
import static com.ostrichemulators.semtool.util.RDFDatatypeTools.getIriFromRawString;
import static com.ostrichemulators.semtool.util.RDFDatatypeTools.getRDFStringValue;
import com.ostrichemulators.semtool.util.UriBuilder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public abstract class AbstractEdgeModeler implements EdgeModeler {

	private static final Logger log = Logger.getLogger( AbstractEdgeModeler.class );
	private final Set<IRI> duplicates;
	private QaChecker qaer;

	public AbstractEdgeModeler( QaChecker qa ) {
		qaer = qa;
		duplicates = qaer.getKnownUris();
	}

	public static boolean isUri( String raw, Map<String, String> namespaces ) {
		if ( raw.startsWith( "<" ) && raw.endsWith( ">" ) ) {
			raw = raw.substring( 1, raw.length() - 1 );
		}

		Matcher m = IRISTARTPATTERN.matcher( raw );
		if ( m.matches() ) {
			return true;
		}

		if ( raw.contains( ":" ) ) {
			String[] pieces = raw.split( ":" );
			if ( 2 == pieces.length ) {
				String namespace = namespaces.get( pieces[0] );
				if ( !( null == namespace || namespace.trim().isEmpty() ) ) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks that the given ImportMetadata is valid for importing data
	 * (basically, does it have a {@link ImportMetadata#databuilder} set).
	 *
	 * @param metas the data to check
	 * @return true, if the ImportMetadata can be used for importing data
	 */
	public static boolean isValidMetadata( ImportMetadata metas ) {
		return ( null != metas.getDataBuilder() );
	}

	/**
	 * Same as
	 * {@link #isValidMetadata(com.ostrichemulators.semtool.poi.main.ImportMetadata)},
	 * but throw an exception if
	 * {@link #isValidMetadata(gov.va.semoss.poi.main.ImportMetadata)} returns
	 * <code>false</code>
	 *
	 * @param metas the data to check
	 */
	public static void isValidMetadataEx( ImportMetadata metas ) throws ImportValidationException {
		if ( !isValidMetadata( metas ) ) {
			throw new ImportValidationException( ErrorType.MISSING_DATA,
					"Invalid metadata" );
		}
	}

	/**
	 * Adds just a node to the dataset (no properties, nothing else)
	 *
	 * @param typename
	 * @param rawlabel
	 * @param namespaces
	 * @param metas
	 * @param myrc
	 * @param checkCacheFirst
	 * @return
	 * @throws org.eclipse.rdf4j.repository.RepositoryException
	 */
	protected IRI addSimpleNode( String typename, String rawlabel, Map<String, String> namespaces,
			ImportMetadata metas, RepositoryConnection myrc, boolean checkCacheFirst )
			throws RepositoryException {

		boolean nodeIsAlreadyUri = isUri( rawlabel, namespaces );

		if ( nodeIsAlreadyUri ) {
			IRI subject = getIriFromRawString( rawlabel, namespaces );
			cacheInstance( subject, typename, rawlabel );
		}
		else {
			if ( ( checkCacheFirst && !hasCachedInstance( typename, rawlabel ) )
					|| !checkCacheFirst ) {
				IRI subject = ( nodeIsAlreadyUri
						? getIriFromRawString( rawlabel, namespaces )
						: metas.getDataBuilder().add( rawlabel ).build() );
				subject = ensureUnique( subject );
				cacheInstance( subject, typename, rawlabel );
			}
		}

		IRI subject = getCachedInstance( typename, rawlabel );
		myrc.add( subject, RDF.TYPE, qaer.getCachedInstanceClass( typename ) );
		return subject;
	}

	protected IRI ensureUnique( IRI uri ) {
		if ( duplicates.contains( uri ) ) {
			UriBuilder dupefixer = UriBuilder.getBuilder( uri.getNamespace() );
			uri = dupefixer.uniqueIri();
			duplicates.add( uri );
		}
		return uri;
	}

	@Override
	public IRI addNode( LoadingSheetData.LoadingNodeAndPropertyValues nap,
			Map<String, String> namespaces, LoadingSheetData sheet, ImportMetadata metas,
			RepositoryConnection myrc ) throws RepositoryException {

		String typename = nap.getSubjectType();
		String rawlabel = nap.getSubject();

		IRI subject = addSimpleNode( typename, rawlabel, namespaces, metas, myrc, true );

		ValueFactory vf = myrc.getValueFactory();
		boolean savelabel = metas.isAutocreateMetamodel();
		if ( rawlabel.contains( ":" ) ) {
			// we have something with a colon in it, so we need to figure out if it's
			// a namespace-prefixed string, or just a string with a colon in it

			Value val = getRDFStringValue( rawlabel, namespaces, vf );
			// check if we have a prefixed URI
			IRI u = getIriFromRawString( rawlabel, namespaces );
			savelabel = ( savelabel && null == u );
			rawlabel = val.stringValue();
		}

		// if we have a label property, skip this label-making
		// (it'll get handled in the addProperties function later)
		if ( savelabel && !nap.hasProperty( RDFS.LABEL, namespaces ) ) {
			myrc.add( subject, RDFS.LABEL, vf.createLiteral( rawlabel ) );
		}

		addProperties( subject, nap, namespaces, sheet, metas, myrc );

		return subject;
	}

	@Override
	public void addProperties( IRI subject, Map<String, Value> properties,
			Map<String, String> namespaces, LoadingSheetData sheet,
			ImportMetadata metas, RepositoryConnection myrc )
			throws RepositoryException {

		for ( Map.Entry<String, Value> entry : properties.entrySet() ) {
			String propname = entry.getKey();
			IRI predicate = getCachedPropertyClass( propname );

			Value value = entry.getValue();
			if ( sheet.isLink( propname ) ) {
				// our "value" is really the label of another node, so find that node
				value = addSimpleNode( propname, value.stringValue(), namespaces,
						metas, myrc, true );
				predicate = getCachedRelationClass( sheet.getSubjectType()
						+ sheet.getObjectType() + propname );
			}

			myrc.add( subject, predicate, value );
		}
	}

	@Override
	public Model createMetamodel( ImportData alldata, Map<String, String> namespaces,
			ValueFactory vf ) throws RepositoryException {
		if ( null == vf ) {
			vf = SimpleValueFactory.getInstance();
		}

		Model model = new TreeModel();

		ImportMetadata metas = alldata.getMetadata();
		UriBuilder schema = metas.getSchemaBuilder();

		Map<String, IRI> structurelkp = new HashMap<>();

		for ( LoadingSheetData sheet : alldata.getSheets() ) {
			String stype = sheet.getSubjectType();
			if ( !hasCachedInstanceClass( stype ) ) {
				boolean nodeAlreadyMade = isUri( stype, namespaces );

				IRI subtype = ( nodeAlreadyMade
						? getIriFromRawString( stype, namespaces )
						: schema.build( stype ) );
				cacheInstanceClass( subtype, stype );

				if ( !nodeAlreadyMade ) {
					model.add( subtype, RDF.TYPE, OWL.CLASS );
					model.add( subtype, RDFS.LABEL, vf.createLiteral( stype ) );
					model.add( subtype, RDFS.SUBCLASSOF, schema.getConceptIri().build() );
				}
			}

			if ( sheet.isRel() ) {
				String otype = sheet.getObjectType();
				if ( !hasCachedInstanceClass( otype ) ) {
					boolean nodeAlreadyMade = isUri( otype, namespaces );

					IRI objtype = ( nodeAlreadyMade
							? getIriFromRawString( otype, namespaces )
							: schema.build( otype ) );

					cacheInstanceClass( objtype, otype );

					if ( !nodeAlreadyMade ) {
						model.add( objtype, RDF.TYPE, OWL.CLASS );
						model.add( objtype, RDFS.LABEL, vf.createLiteral( otype ) );
						model.add( objtype, RDFS.SUBCLASSOF, schema.getConceptIri().build() );
					}
				}

				String rellabel = sheet.getRelname();

				if ( !hasCachedRelationClass( rellabel ) ) {
					boolean relationAlreadyMade = isUri( rellabel, namespaces );

					IRI reltype = ( relationAlreadyMade
							? getIriFromRawString( rellabel, namespaces )
							: schema.build( rellabel ) );
					cacheRelationClass( reltype, rellabel );

					if ( !relationAlreadyMade ) {
						model.add( reltype, RDFS.LABEL, vf.createLiteral( rellabel ) );
						model.add( reltype, RDF.TYPE, OWL.OBJECTPROPERTY );
						model.add( reltype, RDFS.SUBPROPERTYOF, schema.getRelationIri().build() );
					}
				}

				// save the structure data
				IRI subtype = getCachedInstanceClass( stype );
				if ( sheet.isRel() ) {
					IRI objtype = getCachedInstanceClass( sheet.getObjectType() );
					IRI edgetype = getCachedRelationClass( sheet.getRelname() );

					Collection<Statement> structures
							= SemtoolStructureManagerImpl.getEdgeStructure( edgetype,
									subtype, objtype, schema, structurelkp,
									stype + "_" + sheet.getRelname() + "_" + sheet.getObjectType() );
					model.addAll( structures );
				}
			}
		}

		for ( LoadingSheetData sheet : alldata.getSheets() ) {
			IRI subtype = getCachedInstanceClass( sheet.getSubjectType() );
			IRI edgetype = ( sheet.isRel()
					? getCachedRelationClass( sheet.getRelname() )
					: null );

			for ( String propname : sheet.getProperties() ) {
				// check to see if we're actually a link to some
				// other node (and not really a new property
				if ( sheet.isLink( propname ) || hasCachedInstanceClass( propname ) ) {
					log.debug( "linking " + propname + " as a " + SEMONTO.has
							+ " relationship to " + getCachedInstanceClass( propname ) );

					cacheRelationClass( SEMONTO.has,
							sheet.getSubjectType() + sheet.getObjectType() + propname );

					// keep the ontology info for posterity
					Collection<Statement> structures;
					if ( sheet.isRel() ) {
						structures = SemtoolStructureManagerImpl.getEdgeStructure( SEMONTO.has,
								edgetype, getCachedInstanceClass( propname ),
								schema, structurelkp,
								sheet.getRelname() + "_has_" + propname );
					}
					else {
						structures = SemtoolStructureManagerImpl.getEdgeStructure( SEMONTO.has,
								subtype, getCachedInstanceClass( propname ),
								schema, structurelkp,
								sheet.getSubjectType() + "_has_" + propname );
					}
					model.addAll( structures );

					continue;
				}

				boolean alreadyMadeProp = isUri( propname, namespaces );

				if ( !hasCachedPropertyClass( propname ) ) {
					IRI predicate = ( alreadyMadeProp
							? getIriFromRawString( propname, namespaces )
							: schema.build( propname ) );
					cachePropertyClass( predicate, propname );
				}

				IRI predicate = getCachedPropertyClass( propname );

				// save the ontology info for querying db structure
				Collection<Statement> stmts;
				if ( sheet.isRel() ) {
					stmts = SemtoolStructureManagerImpl.getPropStructure( edgetype, subtype,
							getCachedInstanceClass( sheet.getObjectType() ), predicate,
							schema, structurelkp,
							sheet.getSubjectType() + "_" + sheet.getRelname()
							+ "_" + sheet.getObjectType() );
				}
				else {
					stmts = SemtoolStructureManagerImpl.getPropStructure( predicate, subtype,
							schema, structurelkp, sheet.getSubjectType() + "_" + propname );
				}

				model.addAll( stmts );

				if ( !alreadyMadeProp ) {
					model.add( predicate, RDFS.LABEL, vf.createLiteral( propname ) );
					model.add( predicate, RDF.TYPE, OWL.DATATYPEPROPERTY );
				}
			}
		}

		return model;
	}

	@Override
	public void setQaChecker( QaChecker q ) {
		qaer = q;
	}

	public IRI getCachedRelation( RelationCacheKey key ) {
		return qaer.getCachedRelation( key );
	}

	public IRI getCachedInstance( String typename, String rawlabel ) {
		return qaer.getCachedInstance( typename, rawlabel );
	}

	public IRI getCachedInstanceClass( String name ) {
		return qaer.getCachedInstanceClass( name );
	}

	public IRI getCachedRelationClass( String rel ) {
		return qaer.getCachedRelationClass( rel );
	}

	public IRI getCachedPropertyClass( String name ) {
		return qaer.getCachedPropertyClass( name );
	}

	public boolean hasCachedPropertyClass( String name ) {
		return qaer.hasCachedPropertyClass( name );
	}

	public boolean hasCachedRelationClass( String rel ) {
		return qaer.hasCachedRelationClass( rel );
	}

	public boolean hasCachedRelation( String stype, String otype, String relname,
			String slabel, String olabel ) {
		return qaer.hasCachedRelation( stype, otype, relname, slabel, olabel );
	}

	public boolean hasCachedRelation( RelationCacheKey key ) {
		return qaer.hasCachedRelation( key );
	}

	public boolean hasCachedInstance( String typename, String rawlabel ) {
		return qaer.hasCachedInstance( typename, rawlabel );
	}

	public boolean hasCachedInstanceClass( String name ) {
		return qaer.hasCachedInstanceClass( name );
	}

	public void cacheInstanceClass( IRI uri, String label ) {
		qaer.cacheInstanceClass( uri, label );
		duplicates.add( uri );
	}

	public void cacheRelationNode( IRI uri, String stype, String otype,
			String relname, String slabel, String olabel ) {
		qaer.cacheRelationNode( uri, stype, otype, relname, slabel, olabel );
		duplicates.add( uri );
	}

	public void cacheRelationNode( IRI uri, RelationCacheKey key ) {
		qaer.cacheRelationNode( uri, key );
		duplicates.add( uri );
	}

	public void cacheRelationClass( IRI uri, String rel ) {
		qaer.cacheRelationClass( uri, rel );
		duplicates.add( uri );
	}

	public void cacheInstance( IRI uri, String typelabel, String rawlabel ) {
		qaer.cacheInstance( uri, typelabel, rawlabel );
		duplicates.add( uri );
	}

	public void cachePropertyClass( IRI uri, String name ) {
		qaer.cachePropertyClass( uri, name );
		duplicates.add( uri );
	}
}
