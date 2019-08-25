/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.util;

import com.ostrichemulators.semtool.poi.main.ImportData;
import com.ostrichemulators.semtool.poi.main.LoadingSheetData;
import com.ostrichemulators.semtool.poi.main.LoadingSheetData.DataIterator;
import com.ostrichemulators.semtool.poi.main.LoadingSheetData.LoadingNodeAndPropertyValues;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.engine.api.ReificationStyle;
import static com.ostrichemulators.semtool.rdf.engine.util.EngineLoader.cleanValue;
import com.ostrichemulators.semtool.rdf.query.util.impl.ListQueryAdapter;
import com.ostrichemulators.semtool.rdf.query.util.impl.VoidQueryAdapter;
import com.ostrichemulators.semtool.util.MultiMap;
import com.ostrichemulators.semtool.util.UriBuilder;
import com.ostrichemulators.semtool.util.Utility;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.RepositoryException;

/**
 * A class to check for QA errors given a Loading Sheet and an Engine.
 * Basically, this class is just a set of caches with accessor methods. It was
 * lifted from the EngineLoader class to simplify that class.
 *
 * @author ryan
 */
public class QaChecker {

	private static final Logger log = Logger.getLogger( QaChecker.class );
	private final Map<ConceptInstanceCacheKey, IRI> dataNodes;
	private final Map<String, IRI> instanceClassCache;
	private final Map<String, IRI> relationBaseClassCache;
	private final Map<RelationCacheKey, IRI> relationCache;
	private final Map<String, IRI> propertyClassCache;
	private final File backingfile;
	private final DB db;

	public static enum CacheType {

		CONCEPTCLASS, PROPERTYCLASS, RELATIONCLASS
	};

	public QaChecker() {
		File f = null;
		try {
			f = File.createTempFile( "qachecker-", ".maps" );
		}
		catch ( IOException ioe ) {
			log.error( "cannot make backing store...will use in-memory caches", ioe );
		}
		if ( null == f ) {
			db = null;
			backingfile = null;
			dataNodes = new HashMap<>();
			relationCache = new HashMap<>();
			instanceClassCache = new HashMap<>();
			relationBaseClassCache = new HashMap<>();
			propertyClassCache = new HashMap<>();
		}
		else {
			backingfile = f;
			log.debug( "QA backing file is: " + backingfile );
			db = DBMaker.fileDB( f ).
					deleteFilesAfterClose().
					fileMmapEnable().
					transactionDisable().
					asyncWriteEnable().
					make();
			dataNodes = db.treeMapCreate( "datanodes" ).counterEnable().make();
			relationCache = db.treeMapCreate( "relations" ).counterEnable().make();
			instanceClassCache = db.treeMapCreate( "instances" ).keySerializer( Serializer.STRING ).counterEnable().make();
			relationBaseClassCache = db.treeMapCreate( "relationclasses" ).keySerializer( Serializer.STRING ).counterEnable().make();
			propertyClassCache = db.treeMapCreate( "propclasses" ).keySerializer( Serializer.STRING ).counterEnable().make();
		}
	}

	public QaChecker( IEngine eng ) {
		this();
		loadCaches( eng );
	}

	public void release() {
		if ( null != db ) {
			db.close();
			FileUtils.deleteQuietly( backingfile );
		}
	}

	public Set<IRI> getKnownUris() {
		Set<IRI> set = new HashSet<>( instanceClassCache.size()
				+ relationBaseClassCache.size() + relationCache.size()
				+ propertyClassCache.size() + dataNodes.size() );
		for ( Map<?, IRI> map : Arrays.asList( instanceClassCache, relationBaseClassCache,
				relationCache, propertyClassCache, dataNodes ) ) {
			set.addAll( map.values() );
		}

		return set;
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
					for ( LoadingSheetData.LoadingNodeAndPropertyValues nap : errs ) {
						errvals.add( nap );
						errdata.add( nap );
					}

					d.removeAll( errvals );
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
			data.setRelationIsError( !hasCachedRelationClass( data.getRelname() ) );
		}

		for ( Map.Entry<String, IRI> en : data.getPropertiesAndDataTypes().entrySet() ) {
			data.setPropertyIsError( en.getKey(), !propertyClassCache.containsKey( en.getKey() ) );
		}

		return data;
	}

	public void loadCaches( IEngine engine ) {
		if ( null == engine.getSchemaBuilder() || null == engine.getDataBuilder() ) {
			log.error( "this engine does not have a schema or data URI defined" );
		}

		if ( ReificationStyle.LEGACY == EngineUtil2.getReificationStyle( engine ) ) {
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

		DataIterator di = data.iterator();
		while ( di.hasNext() ) {
			LoadingNodeAndPropertyValues nap = di.next();
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

	public void cacheUris( CacheType type, Map<String, IRI> newtocache ) {
		if ( null == type ) {
			throw new IllegalArgumentException( "cache type cannot be null" );
		}
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

	public Map<String, IRI> getCache( CacheType type ) {
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

	public void cacheConceptInstances( Map<String, IRI> instances, String typelabel ) {
		for ( Map.Entry<String, IRI> en : instances.entrySet() ) {
			String l = en.getKey();
			IRI uri = en.getValue();

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
	 * @param propertyClassCache
	 */
	public void setCaches( Map<String, IRI> schemaNodes,
			Map<ConceptInstanceCacheKey, IRI> dataNodes,
			Map<String, IRI> relationClassCache,
			Map<RelationCacheKey, IRI> relationCache, Map<String, IRI> propertyClassCache ) {
		clear();
		this.instanceClassCache.putAll( schemaNodes );
		this.dataNodes.putAll( dataNodes );
		//this.propertiedRelationClassCache.putAll( relationClassCache );

		this.relationBaseClassCache.putAll( relationClassCache );
		this.relationCache.putAll( relationCache );
		this.propertyClassCache.putAll( propertyClassCache );
	}

	public IRI getCachedRelationClass( String key ) {
		return relationBaseClassCache.get( key );
		//return propertiedRelationClassCache.get( key );
	}

	public IRI getCachedPropertyClass( String name ) {
		return propertyClassCache.get( name );
	}

	public IRI getCachedRelation( RelationCacheKey key ) {
		return relationCache.get( key );
	}

	public IRI getCachedInstance( String typename, String rawlabel ) {
		return dataNodes.get( new ConceptInstanceCacheKey( typename, rawlabel ) );
	}

	public IRI getCachedInstanceClass( String name ) {
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

	public void cachePropertyClass( IRI uri, String name ) {
		propertyClassCache.put( name, uri );
	}

	public void cacheInstanceClass( IRI uri, String label ) {
		instanceClassCache.put( label, uri );
	}

	public void cacheRelationNode( IRI uri, String stype, String otype,
			String relname, String slabel, String olabel ) {
		cacheRelationNode( uri, new RelationCacheKey( stype, otype, relname, slabel,
				olabel ) );
	}

	public void cacheRelationNode( IRI uri, RelationCacheKey key ) {
		relationCache.put( key, uri );
	}

	public void cacheRelationClass( IRI uri, String key ) {
		relationBaseClassCache.put( key, uri );
	}

	public void cacheInstance( IRI uri, String typelabel, String rawlabel ) {
		dataNodes.put( new ConceptInstanceCacheKey( typelabel, rawlabel ), uri );
	}

	private void loadLegacy( IEngine engine ) {
		final Map<String, IRI> map = new HashMap<>();
		String subpropq = "SELECT ?uri ?label WHERE { ?uri rdfs:label ?label . ?uri ?isa ?type }";
		VoidQueryAdapter vqa = new VoidQueryAdapter( subpropq ) {

			@Override
			public void handleTuple( BindingSet set, ValueFactory fac ) {
				map.put( set.getValue( "label" ).stringValue(),
						IRI.class.cast( cleanValue( set.getValue( "uri" ), fac ) ) );
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
			IRI type = owlb.getRelationIri().build();
			vqa.bind( "type", type );
			vqa.bind( "isa", RDFS.SUBPROPERTYOF );
			engine.query( vqa );

			Map<String, IRI> props = new HashMap<>();
			for ( Map.Entry<String, IRI> en : map.entrySet() ) {
				props.put( en.getKey(), en.getValue() );
			}

			cacheUris( CacheType.PROPERTYCLASS, props );

			vqa.bind( "isa", RDFS.SUBCLASSOF );
			type = owlb.getConceptIri().build();
			vqa.bind( "type", type );
			engine.query( vqa );
			cacheUris( CacheType.CONCEPTCLASS, map );

			vqa.bind( "isa", RDF.TYPE );
			Map<String, IRI> concepts = new HashMap<>( map );
			for ( Map.Entry<String, IRI> en : concepts.entrySet() ) {
				vqa.bind( "type", en.getValue() );

				engine.query( vqa );
				cacheConceptInstances( map, en.getKey() );
			}

			Set<IRI> needlabels = new HashSet<>();
			String relq = "SELECT DISTINCT * WHERE {"
					+ " ?left a ?lefttype ."
					+ " ?lefttype a owl:Class ."
					+ " ?right a ?righttype ."
					+ " ?righttype a owl:Class ."
					+ " ?left ?specrel ?right ."
					+ " ?specrel rdfs:subPropertyOf ?reltype ."
					+ "}";

			ListQueryAdapter<IRI[]> vqa2 = new ListQueryAdapter<IRI[]>( relq ) {

				@Override
				public void handleTuple( BindingSet set, ValueFactory fac ) {
					IRI reltype = IRI.class.cast( set.getValue( "reltype" ) );
					IRI lefttype = IRI.class.cast( set.getValue( "lefttype" ) );
					IRI righttype = IRI.class.cast( set.getValue( "righttype" ) );
					IRI left = IRI.class.cast( set.getValue( "left" ) );
					IRI right = IRI.class.cast( set.getValue( "right" ) );
					IRI specrel = IRI.class.cast( set.getValue( "specrel" ) );

					IRI[] uris = new IRI[]{ lefttype, righttype, reltype, left, right, specrel };
					needlabels.addAll( Arrays.asList( uris ) );
					add( uris );
				}

			};

			vqa2.useInferred( false );
			List<IRI[]> data = engine.query( vqa2 );
			Map<IRI, String> labels = Utility.getInstanceLabels( needlabels, engine );
			for ( IRI[] uris : data ) {
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
		StructureManager sm = StructureManagerFactory.getStructureManager( engine );
		Model model = sm.rebuild( false );

		Set<IRI> datatypeProps = new HashSet<>();
		for ( Statement s : model.filter( null, OWL.DATATYPEPROPERTY, null ) ) {
			datatypeProps.add( IRI.class.cast( s.getObject() ) );
		}
		Map<IRI, String> dtlabels = Utility.getInstanceLabels( datatypeProps, engine );
		cacheUris( CacheType.PROPERTYCLASS, MultiMap.lossyflip( dtlabels ) );

		Set<IRI> rels = new HashSet<>();
		for ( Value v : model.filter( null, RDF.PREDICATE, null ).objects() ) {
			rels.add( IRI.class.cast( v ) );
		}
		Map<IRI, String> rellabels = Utility.getInstanceLabels( rels, engine );
		cacheUris( CacheType.RELATIONCLASS, MultiMap.lossyflip( rellabels ) );

		Map<IRI, String> cpclabels = Utility.getInstanceLabels( sm.getTopLevelConcepts(), engine );
		cacheUris( CacheType.CONCEPTCLASS, MultiMap.lossyflip( cpclabels ) );

		// cache concept instances
		for ( Map.Entry<IRI, String> en : rellabels.entrySet() ) {
			List<IRI> instances
					= NodeDerivationTools.createInstanceList( en.getKey(), engine );
			Map<IRI, String> names = Utility.getInstanceLabels( instances, engine );
			for ( Map.Entry<IRI, String> en2 : names.entrySet() ) {
				cacheInstance( en2.getKey(), en.getValue(), en2.getValue() );
			}
		}

		// cache relation instances
		Model preds = model.filter( null, RDF.PREDICATE, null );
		for ( Statement s : preds ) {
			IRI pred = IRI.class.cast( s.getObject() );
			String relname = rellabels.get( pred );

			// get the subject from the RDFS.DOMAIN statements
			// and the object from the RDFS.RANGE statements
			for ( Statement t : model.filter( s.getSubject(), RDFS.DOMAIN, null ) ) {
				IRI stype = IRI.class.cast( t.getObject() );
				String stypelabel = cpclabels.get( stype );

				for ( Statement u : model.filter( s.getSubject(), RDFS.RANGE, null ) ) {
					IRI otype = IRI.class.cast( u.getObject() );
					String otypelabel = cpclabels.get( otype );

					Model instancemodel = NodeDerivationTools.getInstances( stype, pred,
							otype, null, engine );
					List<Resource> objs = new ArrayList<>();
					instancemodel.objects().forEach( new Consumer<Value>() {

						@Override
						public void accept( Value t ) {
							objs.add( Resource.class.cast( t ) );
						}
					} );
					Map<Resource, String> lkp
							= Utility.getInstanceLabels( instancemodel.subjects(), engine );
					lkp.putAll( Utility.getInstanceLabels( objs, engine ) );

					for ( Statement instance : instancemodel ) {
						RelationCacheKey rck = new RelationCacheKey(
								stypelabel,
								otypelabel,
								relname,
								lkp.get( instance.getSubject() ),
								lkp.get( Resource.class.cast( instance.getObject() ) ) );
						cacheRelationNode( instance.getPredicate(), rck );
					}
				}
			}
		}
	}

	public static class ConceptInstanceCacheKey implements Serializable,
			Comparable<ConceptInstanceCacheKey> {

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

		@Override
		public int compareTo( ConceptInstanceCacheKey o ) {
			int diff = typelabel.compareTo( o.typelabel );
			if ( 0 == diff ) {
				return rawlabel.compareTo( o.rawlabel );
			}
			return diff;
		}
	}

	public static class RelationCacheKey implements Serializable,
			Comparable<RelationCacheKey> {

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

		@Override
		public String toString() {
			return String.format( "%s%s%s%s%s", stype, otype, s, o, relname );
		}

		@Override
		public int compareTo( RelationCacheKey other ) {
			return toString().compareTo( other.toString() );
		}
	}
}
