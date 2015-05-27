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
import gov.va.semoss.rdf.query.util.impl.ListQueryAdapter;
import gov.va.semoss.rdf.query.util.impl.VoidQueryAdapter;
import gov.va.semoss.util.UriBuilder;
import gov.va.semoss.util.Utility;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.Binding;
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
	//private final Map<RelationClassCacheKey, URI> propertiedRelationClassCache = new HashMap<>();
	private final Map<String, URI> relationBaseClassCache = new HashMap<>();
	private final Map<RelationCacheKey, URI> relationCache = new HashMap<>();
	private final Map<String, URI> propertyClassCache = new HashMap<>();

	public static enum CacheType {

		CONCEPTCLASS, PROPERTYCLASS, RELATIONCLASS
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
			data.setRelationIsError( hasCachedRelationClass( data.getRelname() ) );
		}

		for ( Map.Entry<String, URI> en : data.getPropertiesAndDataTypes().entrySet() ) {
			data.setPropertyIsError( en.getKey(), !propertyClassCache.containsKey( en.getKey() ) );
		}

		return data;
	}

	public void loadCaches( IEngine engine ) {
		if ( null == engine.getSchemaBuilder() || null == engine.getDataBuilder() ) {
			log.error( "this engine does not have a schema or data URI defined" );
		}

		if ( ReificationStyle.LEGACY == MetadataQuery.getReificationStyle( engine ) ) {
			loadLegacy( engine );
		}
		else {
			load( engine );
		}
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
		switch ( type ) {
			case CONCEPTCLASS:
				instanceClassCache.putAll( newtocache );
				break;
			case PROPERTYCLASS:
				propertyClassCache.putAll( newtocache );
				break;
			case RELATIONCLASS:
				relationBaseClassCache.putAll( newtocache );
				break;
			default:
				throw new IllegalArgumentException( "unhandled cache type: " + type );
		}
	}

	public Map<String, URI> getCache( CacheType type ) {
		switch ( type ) {
			case CONCEPTCLASS:
				return new HashMap<>( instanceClassCache );
			case PROPERTYCLASS:
				return new HashMap<>( propertyClassCache );
			case RELATIONCLASS:
				return new HashMap<>( relationBaseClassCache );
			default:
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

	/**
	 * Clears the caches
	 */
	public void clear() {
		instanceClassCache.clear();
		dataNodes.clear();
		relationBaseClassCache.clear();
		// propertiedRelationClassCache.clear();
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
			Map<String, URI> relationClassCache,
			Map<RelationCacheKey, URI> relationCache, Map<String, URI> propertyClassCache ) {
		clear();
		this.instanceClassCache.putAll( schemaNodes );
		this.dataNodes.putAll( dataNodes );
		//this.propertiedRelationClassCache.putAll( relationClassCache );

		this.relationBaseClassCache.putAll( relationClassCache );
		this.relationCache.putAll( relationCache );
		this.propertyClassCache.putAll( propertyClassCache );
	}

	public URI getCachedRelationClass( String key ) {
		return relationBaseClassCache.get( key );
		//return propertiedRelationClassCache.get( key );
	}

	public URI getCachedPropertyClass( String name ) {
		return propertyClassCache.get( name );
	}

	public URI getCachedRelation( RelationCacheKey key ) {
		return relationCache.get( key );
	}

	public URI getCachedInstance( String typename, String rawlabel ) {
		return dataNodes.get( new ConceptInstanceCacheKey( typename, rawlabel ) );
	}

	public URI getCachedInstanceClass( String name ) {
		return instanceClassCache.get( name );
	}

	public boolean hasCachedRelationClass( String key ) {
		return relationBaseClassCache.containsKey( key );
	}

	public boolean hasCachedPropertyClass( String name ) {
		return propertyClassCache.containsKey( name );
	}

	public boolean hasCachedRelation( String stype, String otype, String relname,
			String slabel, String olabel ) {
		return hasCachedRelation( new RelationCacheKey( stype, otype, relname,
				slabel, olabel ) );
	}

	public boolean hasCachedRelation( RelationCacheKey key ) {
		return relationCache.containsKey( key );
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

	public void cacheRelationNode( URI uri, String stype, String otype,
			String relname, String slabel, String olabel ) {
		cacheRelationNode( uri, new RelationCacheKey( stype, otype, relname, slabel,
				olabel ) );
	}

	public void cacheRelationNode( URI uri, RelationCacheKey key ) {
		relationCache.put( key, uri );
	}

	public void cacheRelationClass( URI uri, String key ) {
		relationBaseClassCache.put( key, uri );
	}

	public void cacheInstance( URI uri, String typelabel, String rawlabel ) {
		dataNodes.put( new ConceptInstanceCacheKey( typelabel, rawlabel ), uri );
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

		try {
			URI type = owlb.getRelationUri().build();
			vqa.bind( "type", type );
			vqa.bind( "isa", RDFS.SUBPROPERTYOF );
			engine.query( vqa );

			Map<String, URI> props = new HashMap<>();
			for ( Map.Entry<String, URI> en : map.entrySet() ) {
				props.put( en.getKey(), en.getValue() );
			}

			cacheUris( CacheType.PROPERTYCLASS, props );

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

			Set<URI> needlabels = new HashSet<>();
			String relq = "SELECT DISTINCT * WHERE {"
					+ " ?left a ?lefttype ."
					+ " ?lefttype a owl:Class ."
					+ " ?right a ?righttype ."
					+ " ?righttype a owl:Class ."
					+ " ?left ?specrel ?right ."
					+ " ?specrel rdfs:subPropertyOf ?reltype ."
					+ "}";

			ListQueryAdapter<URI[]> vqa2 = new ListQueryAdapter<URI[]>( relq ) {

				@Override
				public void handleTuple( BindingSet set, ValueFactory fac ) {
					URI reltype = URI.class.cast( set.getValue( "reltype" ) );
					URI lefttype = URI.class.cast( set.getValue( "lefttype" ) );
					URI righttype = URI.class.cast( set.getValue( "righttype" ) );
					URI left = URI.class.cast( set.getValue( "left" ) );
					URI right = URI.class.cast( set.getValue( "right" ) );
					URI specrel = URI.class.cast( set.getValue( "specrel" ) );

					URI[] uris = new URI[]{ lefttype, righttype, reltype, left, right, specrel };
					needlabels.addAll( Arrays.asList( uris ) );
					add( uris );
				}

			};

			vqa2.useInferred( false );
			List<URI[]> data = engine.query( vqa2 );
			Map<URI, String> labels = Utility.getInstanceLabels( needlabels, engine );
			for ( URI[] uris : data ) {
				cacheRelationNode( uris[5], labels.get( uris[0] ),
						labels.get( uris[1] ), labels.get( uris[2] ),
						labels.get( uris[3] ), labels.get( uris[4] ) );
			}

		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			log.warn( e, e );
		}
	}

	private void load( IEngine engine ) {
		final Map<String, URI> map = new HashMap<>();
		String subpropq = "SELECT ?uri ?label WHERE { ?uri rdfs:label ?label . ?uri a ?type }";
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

		try {
			vqa.bind( "type", OWL.DATATYPEPROPERTY );
			engine.query( vqa );
			cacheUris( CacheType.PROPERTYCLASS, map );

			vqa.bind( "type", OWL.OBJECTPROPERTY );
			engine.query( vqa );
			cacheUris( CacheType.RELATIONCLASS, map );

			String subpropq2 = "SELECT ?uri ?label WHERE { ?uri rdfs:label ?label . "
					+ "?uri rdfs:subClassOf+ owl:Thing }";
			vqa.setSparql( subpropq2 );
			engine.query( vqa );
			cacheUris( CacheType.CONCEPTCLASS, map );

//			// getting relationships to cache is harder, because we need the labels
//			// of all the parts involved.
//			String relq = "SELECT DISTINCT ?reltype ?stypelabel ?otypelabel ?relname WHERE {"
//					+ "?sub ?reltype ?obj ."
//					+ "?reltype a owl:ObjectProperty ."
//					//+ "?sub rdfs:label ?slabel ."
//					//+ "?obj rdfs:label ?olabel ."
//					+ "?reltype rdfs:label ?relname ."
//					+ "?sub a ?subtype ."
//					+ "?subtype rdfs:subClassOf ?concept ."
//					+ "?subtype rdfs:label ?stypelabel ."
//					+ "?obj a ?objtype ."
//					+ "?objtype rdfs:subClassOf ?concept ."
//					+ "?objtype rdfs:label ?otypelabel"
//					+ "}";
//			VoidQueryAdapter vqa2 = new VoidQueryAdapter( relq ) {
//
//				@Override
//				public void handleTuple( BindingSet set, ValueFactory fac ) {
//					QaChecker.this.cacheRelationClass(
//							URI.class.cast( set.getValue( "reltype" ) ),
//							set.getValue( "stypelabel" ).stringValue(),
//							set.getValue( "otypelabel" ).stringValue(),
//							set.getValue( "relname" ).stringValue() );
//				}
//
//			};
//			vqa2.useInferred( true );
//			vqa2.bind( "concept", owlb.getConceptUri().build() );
//			engine.query( vqa2 );
			String instq = "SELECT DISTINCT ?sub ?rawlabel ?typelabel WHERE {"
					+ "?sub a ?type ."
					+ "?sub rdfs:label ?rawlabel ."
					+ "?type a owl:Class ."
					+ "?type rdfs:subClassOf ?concept ."
					+ "?type rdfs:label ?typelabel"
					+ "}";
			VoidQueryAdapter vqa3 = new VoidQueryAdapter( instq ) {

				@Override
				public void handleTuple( BindingSet set, ValueFactory fac ) {
					QaChecker.this.cacheInstance(
							URI.class.cast( set.getValue( "reltype" ) ),
							set.getValue( "typelabel" ).stringValue(),
							set.getValue( "rawlabel" ).stringValue() );
				}

			};
			vqa3.useInferred( true );
			vqa3.bind( "concept", owlb.getConceptUri().build() );
			engine.query( vqa3 );

			String relq2 = "SELECT DISTINCT ?specrel ?stypelabel ?otypelabel ?slabel ?olabel ?relname "
					+ "WHERE {"
					+ "  ?specrel a ?semossrel ."
					+ "  ?specrel rdf:predicate ?superrel ."
					+ "  ?sub ?specrel ?obj ."
					+ "  ?sub a ?stype ."
					+ "  ?obj a ?otype ."
					+ "  ?sub rdfs:label ?slabel ."
					+ "  ?obj rdfs:label ?olabel ."
					+ "  ?stype rdfs:label ?stypelabel ."
					+ "  ?otype rdfs:label ?otypelabel ."
					+ "  ?superrel rdfs:label ?relname"
					+ "}";
			VoidQueryAdapter vqa4 = new VoidQueryAdapter( relq2 ) {

				@Override
				public void handleTuple( BindingSet set, ValueFactory fac ) {				
					RelationCacheKey rck = new RelationCacheKey(
							set.getValue( "stypelabel" ).stringValue(),
							set.getValue( "otypelabel" ).stringValue(),
							set.getValue( "relname" ).stringValue(),
							set.getValue( "slabel" ).stringValue(),
							set.getValue( "olabel" ).stringValue() );

					QaChecker.this.cacheRelationNode(
							URI.class.cast( set.getValue( "specrel" ) ), rck );
				}

			};
			vqa4.useInferred( true );
			vqa4.bind( "semossrel", owlb.getRelationUri().build() );
			engine.query( vqa4 );

		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			log.warn( e, e );
		}

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

	public static class RelationCacheKey {

		private final String s;
		private final String o;
		private final String relname;
		private final String stype;
		private final String otype;

		public RelationCacheKey( String stype, String otype, String relname,
				String s, String o ) {
			this.s = s;
			this.o = o;
			this.relname = relname;
			this.stype = stype;
			this.otype = otype;
		}

		@Override
		public int hashCode() {
			int hash = 3;
			hash = 97 * hash + Objects.hashCode( this.s );
			hash = 97 * hash + Objects.hashCode( this.o );
			hash = 97 * hash + Objects.hashCode( this.relname );
			hash = 97 * hash + Objects.hashCode( this.stype );
			hash = 97 * hash + Objects.hashCode( this.otype );
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
			final RelationCacheKey other = (RelationCacheKey) obj;
			if ( !Objects.equals( this.s, other.s ) ) {
				return false;
			}
			if ( !Objects.equals( this.o, other.o ) ) {
				return false;
			}
			if ( !Objects.equals( this.relname, other.relname ) ) {
				return false;
			}
			if ( !Objects.equals( this.stype, other.stype ) ) {
				return false;
			}
			if ( !Objects.equals( this.otype, other.otype ) ) {
				return false;
			}
			return true;
		}

	}
}
