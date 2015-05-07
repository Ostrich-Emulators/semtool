/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.util;

import gov.va.semoss.poi.main.ImportData;
import gov.va.semoss.poi.main.LoadingSheetData;
import gov.va.semoss.poi.main.LoadingSheetData.LoadingNodeAndPropertyValues;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.api.ReificationStyle;
import static gov.va.semoss.rdf.engine.util.EngineLoader.cleanValue;
import gov.va.semoss.rdf.query.util.MetadataQuery;
import gov.va.semoss.rdf.query.util.impl.VoidQueryAdapter;
import gov.va.semoss.util.UriBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

/**
 * A class to check for QA errors given a Loading Sheet and an Engine.
 * Basically, this class is just a set of caches with accessor methods. It was
 * lifted from the EngineLoader class to simplify that class.
 *
 * @author ryan
 */
public class QaChecker {

	private static final Logger log = Logger.getLogger( QaChecker.class );
	private final Map<ConceptInstanceCacheKey, URI> dataNodes = new HashMap<>();
	private final Map<String, URI> instanceClassCache = new HashMap<>();
	private final Map<RelationClassCacheKey, URI> relationClassCache = new HashMap<>();
	private final Map<String, URI> relationCache = new HashMap<>();
	private final Map<String, URI> propertyClassCache = new HashMap<>();

	public static enum CacheType {

		CONCEPTCLASS, RELATION, PROPERTYCLASS
	};

	public QaChecker() {
	}

	public QaChecker( IEngine eng ) {
		loadCaches( eng );
	}

	/**
	 * Separates any non-conforming data from the loading data. This removes the
	 * offending data from <code>data</code> and puts them in <code>errors</code>
	 *
	 * @param data the data to check for errors
	 * @param errors where to put non-conforming data. If null, this function does
	 * nothing
	 * @param engine the engine to check against
	 */
	public void separateConformanceErrors( ImportData data, ImportData errors,
			IEngine engine ) {
		if ( null != errors ) {
			for ( LoadingSheetData d : data.getSheets() ) {
				List<LoadingSheetData.LoadingNodeAndPropertyValues> errs
						= checkConformance( d, engine, false );

				if ( !errs.isEmpty() ) {
					LoadingSheetData errdata = LoadingSheetData.copyHeadersOf( d );
					errdata.setProperties( d.getPropertiesAndDataTypes() );
					errors.add( errdata );

					Set<LoadingSheetData.LoadingNodeAndPropertyValues> errvals = new HashSet<>();
					List<LoadingSheetData.LoadingNodeAndPropertyValues> reldata = d.getData();

					for ( LoadingSheetData.LoadingNodeAndPropertyValues nap : errs ) {
						errvals.add( nap );
						errdata.add( nap );
					}

					reldata.removeAll( errvals );
				}
			}
		}
	}

	/**
	 * Checks that the Loading Sheet's {@link LoadingSheetData#subjectType},
	 * {@link LoadingSheetData#objectType}, and
	 * {@link LoadingSheetData#getProperties()} exist in the given engine
	 *
	 * @param data the data to check
	 * @return the same loading sheet as the <code>data</code> arg
	 */
	public LoadingSheetData checkModelConformance( LoadingSheetData data ) {
		data.setSubjectTypeIsError( !instanceClassCache.containsKey( data.getSubjectType() ) );

		if ( data.isRel() ) {
			data.setObjectTypeIsError( !instanceClassCache.containsKey( data.getObjectType() ) );

			data.setRelationIsError( hasCachedRelationClass( data.getSubjectType(),
					data.getObjectType(), data.getRelname() ) );
		}

		for ( Map.Entry<String, URI> en : data.getPropertiesAndDataTypes().entrySet() ) {
			data.setPropertyIsError( en.getKey(), !propertyClassCache.containsKey( en.getKey() ) );
		}

		return data;
	}

	public void loadCaches( IEngine engine ) {
		if ( ReificationStyle.LEGACY == MetadataQuery.getReificationStyle( engine ) ) {
			loadLegacy( engine );
		}
		else {
			load( engine );
		}
	}

	private void loadLegacy( IEngine engine ) {
		final Map<String, URI> map = new HashMap<>();
		String subpropq = "SELECT ?uri ?label WHERE { ?uri rdfs:label ?label . ?uri ?isa ?type }";
		VoidQueryAdapter vqa = new VoidQueryAdapter( subpropq ) {

			@Override
			public void handleTuple( BindingSet set, ValueFactory fac ) {
				map.put( set.getValue( "label" ).stringValue(),
						URI.class.cast( cleanValue( set.getValue( "uri" ), fac ) ) );
			}

			@Override
			public void start( List<String> bnames ) {
				super.start( bnames );
				map.clear();
			}
		};
		vqa.useInferred( true );
		UriBuilder owlb = engine.getSchemaBuilder();
		UriBuilder datab = engine.getDataBuilder();

		try {
			URI type = owlb.getRelationUri().build();
			vqa.bind( "type", type );
			vqa.bind( "isa", RDFS.SUBPROPERTYOF );
			engine.query( vqa );

			Map<String, URI> props = new HashMap<>();
			Map<String, URI> relations = new HashMap<>();
			for ( Map.Entry<String, URI> en : map.entrySet() ) {
				if ( datab.contains( en.getValue() ) ) {
					relations.put( en.getKey(), en.getValue() );
				}
				else {
					props.put( en.getKey(), en.getValue() );
				}
			}

			cacheUris( CacheType.PROPERTYCLASS, props );
			cacheUris( CacheType.RELATION, relations );

			vqa.bind( "isa", RDFS.SUBCLASSOF );
			type = owlb.getConceptUri().build();
			vqa.bind( "type", type );
			engine.query( vqa );
			cacheUris( CacheType.CONCEPTCLASS, map );

			vqa.bind( "isa", RDF.TYPE );
			Map<String, URI> concepts = new HashMap<>( map );
			for ( Map.Entry<String, URI> en : concepts.entrySet() ) {
				vqa.bind( "type", en.getValue() );

				engine.query( vqa );
				cacheConceptInstances( map, en.getKey() );
			}
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			log.warn( e, e );
		}
	}

	private void load( IEngine engine ) {

	}

	/**
	 * Checks conformance of the given data. The <code>data</code> argument will
	 * be updated when errors are found. Only relationship data can be
	 * non-conforming.
	 *
	 * @param data the data to check
	 * @param eng the engine to check against. Can be null if
	 * <code>loadcaches</code> is false
	 * @param loadcaches call
	 * {@link #loadCaches(gov.va.semoss.rdf.engine.api.IEngine)} first
	 * @return a list of all {@link LoadingNodeAndPropertyValues} that fail the
	 * check
	 */
	public List<LoadingNodeAndPropertyValues> checkConformance( LoadingSheetData data,
			IEngine eng, boolean loadcaches ) {
		List<LoadingNodeAndPropertyValues> failures = new ArrayList<>();

		if ( loadcaches ) {
			loadCaches( eng );
		}

		String stype = data.getSubjectType();
		String otype = data.getObjectType();

		for ( LoadingNodeAndPropertyValues nap : data.getData() ) {
			// check that the subject and object are in our instance cache
			ConceptInstanceCacheKey skey
					= new ConceptInstanceCacheKey( stype, nap.getSubject() );
			nap.setSubjectIsError( !dataNodes.containsKey( skey ) );

			if ( data.isRel() ) {
				ConceptInstanceCacheKey okey
						= new ConceptInstanceCacheKey( otype, nap.getObject() );
				nap.setObjectIsError( !dataNodes.containsKey( okey ) );
			}

			if ( nap.hasError() ) {
				failures.add( nap );
			}
		}

		return failures;
	}

	/**
	 * Checks for an instance of the given type and label.
	 * {@link #loadCaches(gov.va.semoss.rdf.engine.api.IEngine)} MUST be called
	 * prior to this function to have any hope at a true result
	 *
	 * @param type
	 * @param label
	 * @return true, if the type/label matches a cached value
	 */
	public boolean instanceExists( String type, String label ) {
		return dataNodes.containsKey( new ConceptInstanceCacheKey( type, label ) );
	}

	public void cacheUris( CacheType type, Map<String, URI> newtocache ) {
		if ( CacheType.CONCEPTCLASS == type ) {
			instanceClassCache.putAll( newtocache );
		}
		else if ( CacheType.RELATION == type ) {
			relationCache.putAll( newtocache );
		}
		else if ( CacheType.PROPERTYCLASS == type ) {
			propertyClassCache.putAll( newtocache );
		}
		else {
			throw new IllegalArgumentException( "unhandled cache type: " + type );
		}
	}

	public void cacheConceptInstances( Map<String, URI> instances, String typelabel ) {
		for ( Map.Entry<String, URI> en : instances.entrySet() ) {
			String l = en.getKey();
			URI uri = en.getValue();

			ConceptInstanceCacheKey key = new ConceptInstanceCacheKey( typelabel, l );
			//log.debug( "conceptinstances : " + key + " -> " + en.getValue() );
			dataNodes.put( key, uri );
		}
	}

	public void cacheRelationClasses( Map<String, URI> instances, String typelabel ) {
		for ( Map.Entry<String, URI> en : instances.entrySet() ) {
			String l = en.getKey();
			URI uri = en.getValue();

			RelationClassCacheKey key = new RelationClassCacheKey( typelabel, l, "" );
			//log.debug( "conceptinstances : " + key + " -> " + en.getValue() );
			relationClassCache.put( key, uri );
		}
	}

	/**
	 * Clears the caches
	 */
	public void clear() {
		instanceClassCache.clear();
		dataNodes.clear();
		relationClassCache.clear();
		relationCache.clear();
		propertyClassCache.clear();
	}

	/**
	 * Resets the caches to these calues
	 *
	 * @param schemaNodes
	 * @param dataNodes
	 * @param relationClassCache
	 * @param relationCache
	 */
	public void setCaches( Map<String, URI> schemaNodes,
			Map<ConceptInstanceCacheKey, URI> dataNodes,
			Map<RelationClassCacheKey, URI> relationClassCache,
			Map<String, URI> relationCache, Map<String, URI> propertyClassCache ) {
		clear();
		this.instanceClassCache.putAll( schemaNodes );
		this.dataNodes.putAll( dataNodes );
		this.relationClassCache.putAll( relationClassCache );
		this.relationCache.putAll( relationCache );
		this.propertyClassCache.putAll( propertyClassCache );
	}

	public URI getCachedRelationClass( String sub, String obj, String rel ) {
		return getCachedRelationClass( new RelationClassCacheKey( sub, obj, rel ) );
	}

	public URI getCachedRelationClass( RelationClassCacheKey key ) {
		return relationClassCache.get( key );
	}

	public URI getCachedPropertyClass( String name ) {
		return propertyClassCache.get( name );
	}

	public URI getCachedRelation( String name ) {
		return relationCache.get( name );
	}

	public URI getCachedInstance( String typename, String rawlabel ) {
		return dataNodes.get( new ConceptInstanceCacheKey( typename, rawlabel ) );
	}

	public URI getCachedInstanceClass( String name ) {
		return instanceClassCache.get( name );
	}

	public boolean hasCachedRelationClass( String s, String o, String p ) {
		return relationClassCache.containsKey( new RelationClassCacheKey( s, o, p ) );
	}

	public boolean hasCachedPropertyClass( String name ) {
		return propertyClassCache.containsKey( name );
	}

	public boolean hasCachedRelation( String name ) {
		return relationCache.containsKey( name );
	}

	public boolean hasCachedInstance( String typename, String rawlabel ) {
		return hasCachedInstance( new ConceptInstanceCacheKey( typename, rawlabel ) );
	}

	public boolean hasCachedInstance( ConceptInstanceCacheKey key ) {
		return dataNodes.containsKey( key );
	}

	public boolean hasCachedInstanceClass( String name ) {
		return instanceClassCache.containsKey( name );
	}

	public void cachePropertyClass( URI uri, String name ) {
		propertyClassCache.put( name, uri );
	}

	public void cacheInstanceClass( URI uri, String label ) {
		instanceClassCache.put( label, uri );
	}

	public void cacheRelationNode( URI uri, String label ) {
		relationCache.put( label, uri );
	}

	public void cacheRelationClass( URI uri, RelationClassCacheKey key ) {
		relationClassCache.put( key, uri );
	}

	public void cacheRelationClass( URI uri, String subtype, String objtype,
			String relname ) {
		cacheRelationClass( uri,
				new RelationClassCacheKey( subtype, objtype, relname ) );
	}

	public void cacheInstance( URI uri, String typelabel, String rawlabel ) {
		dataNodes.put( new ConceptInstanceCacheKey( typelabel, rawlabel ), uri );
	}

	public static class ConceptInstanceCacheKey {

		private final String typelabel;
		private final String rawlabel;

		public ConceptInstanceCacheKey( String typelabel, String conceptlabel ) {
			this.typelabel = typelabel;
			this.rawlabel = conceptlabel;
		}

		public String getTypeLabel() {
			return typelabel;
		}

		public String getConceptLabel() {
			return rawlabel;
		}

		@Override
		public String toString() {
			return "instance " + typelabel + "<->" + rawlabel;
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 89 * hash + Objects.hashCode( this.typelabel );
			hash = 89 * hash + Objects.hashCode( this.rawlabel );
			return hash;
		}

		@Override
		public boolean equals( Object obj ) {
			if ( obj == null ) {
				return false;
			}
			if ( getClass() != obj.getClass() ) {
				return false;
			}
			final ConceptInstanceCacheKey other = (ConceptInstanceCacheKey) obj;
			if ( !Objects.equals( this.typelabel, other.typelabel ) ) {
				return false;
			}
			return ( Objects.equals( this.rawlabel, other.rawlabel ) );
		}
	}

	public static class RelationClassCacheKey {

		private final String s;
		private final String p;
		private final String o;

		public RelationClassCacheKey( String subtype, String objtype, String relname ) {
			s = subtype;
			p = relname;
			o = objtype;
		}

		@Override
		public String toString() {
			return "rel " + s + "<->" + p + "<->" + o;
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 53 * hash + Objects.hashCode( this.s );
			hash = 53 * hash + Objects.hashCode( this.p );
			hash = 53 * hash + Objects.hashCode( this.o );
			return hash;
		}

		@Override
		public boolean equals( Object obj ) {
			if ( obj == null ) {
				return false;
			}
			if ( getClass() != obj.getClass() ) {
				return false;
			}
			final RelationClassCacheKey other = (RelationClassCacheKey) obj;
			if ( !Objects.equals( this.s, other.s ) ) {
				return false;
			}
			if ( !Objects.equals( this.p, other.p ) ) {
				return false;
			}
			if ( !Objects.equals( this.o, other.o ) ) {
				return false;
			}
			return true;
		}
	}
}
