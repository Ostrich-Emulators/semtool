/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.util;

import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.query.util.impl.VoidQueryAdapter;
import com.ostrichemulators.semtool.util.MultiMap;
import java.io.Closeable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.Document;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.search.spell.StringDistance;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.BindingSet;

/**
 * Checks the values from a Loading Sheet against the values in an existing
 * engine. This is a string-based checking, based on a particular sound
 * algorithm
 *
 * @author ryan
 */
public class EngineConsistencyChecker {

	private static final Logger log = Logger.getLogger( EngineConsistencyChecker.class );

	public static enum Type {

		CONCEPT, RELATIONSHIP
	};

	private final IEngine engine;
	private final boolean across;
	private final StringDistance strdist;
	private final Map<IRI, String> labels = new HashMap<>();
	private final Map<IRI, IRI> uriToTypeLkp = new HashMap<>();
	private final MultiMap<IRI, IRI> typeToURILkp = new MultiMap<>();

	public EngineConsistencyChecker( IEngine eng, boolean across, StringDistance dist ) {
		this.engine = eng;
		this.across = across;
		this.strdist = dist;
	}

	public void release() {
		labels.clear();
		uriToTypeLkp.clear();
		typeToURILkp.clear();
	}

	/**
	 * Adds the given uris as the specified type
	 *
	 * @param uris A collection of concept classes (not instances)
	 * @param type
	 */
	public void add( Collection<IRI> uris, Type type ) {
		if ( Type.CONCEPT == type ) {
			for ( IRI uri : uris ) {
				makeConceptDocuments( uri );
			}
		}
		else {
			for ( IRI uri : uris ) {
				makeRelationDocuments( uri );
			}
		}
	}

	private void makeConceptDocuments( IRI concept ) {

		String query = "SELECT DISTINCT ?s ?slabel WHERE { ?s a ?concept ; rdfs:label ?slabel } ORDER BY ?s";
		VoidQueryAdapter vqa = new VoidQueryAdapter( query ) {
			IRI lastS = null;
			Document currentDoc = null;
			Set<String> seenLabels = new HashSet<>();

			@Override
			public void handleTuple( BindingSet set, ValueFactory fac ) {
				IRI s = IRI.class.cast( set.getValue( "s" ) );
				if ( s != lastS ) {
					seenLabels.clear();
					typeToURILkp.add( concept, s );
					uriToTypeLkp.put( s, concept );
					lastS = s;
				}

				String label = set.getValue( "slabel" ).stringValue();
				// don't add multiple copies of the same label
				if ( !seenLabels.contains( label ) ) {
					seenLabels.add( label );
					labels.put( s, label );
				}
			}
		};

		vqa.bind( "concept", concept );
		// log.debug( vqa.bindAndGetSparql() );
		engine.queryNoEx( vqa );
	}

	private void makeRelationDocuments( IRI superclass ) {
		// get all suclasses of superclass
		String query = "SELECT DISTINCT ?rel ?label WHERE {\n"
				+ " ?rel rdfs:subPropertyOf ?superclass ; rdfs:label ?label .\n"
				+ " FILTER( ?rel != ?superclass )\n"
				+ "} ORDER BY ?rel";
		VoidQueryAdapter vqa = new VoidQueryAdapter( query ) {
			IRI lastRel = null;
			Set<String> seenLabels = new HashSet<>();

			@Override
			public void handleTuple( BindingSet set, ValueFactory fac ) {
				IRI rel = IRI.class.cast( set.getValue( "rel" ) );
				if ( rel != lastRel ) {
					seenLabels.clear();

					// add the URI information now; seenLabels later (avoid saving too much data)
					typeToURILkp.add( superclass, rel );
					uriToTypeLkp.put( rel, superclass );
					lastRel = rel;
				}

				String label = set.getValue( "label" ).stringValue();
				// don't add multiple copies of the same label
				if ( !seenLabels.contains( label ) ) {
					seenLabels.add( label );
					labels.put( rel, label );
				}
			}
		};

		vqa.bind( "superclass", superclass );
		log.debug( vqa.bindAndGetSparql() );
		engine.queryNoEx( vqa );
	}

	public int getItemsForType( IRI uri ) {
		return typeToURILkp.getNN( uri ).size();
	}

	/**
	 * Resolves "near" matches from the elements of the given type. If
	 * {@link #across} is <code>true</code>, each element will be compared to all
	 * elements of all types.
	 *
	 * @param uri the concept/relation class (not instance) to resolve
	 * @param minDistance the minimum allowable similarity
	 * @return map of uri-to-hits
	 */
	public MultiMap<IRI, Hit> check( IRI uri, final float minDistance ) {
		MultiMap<IRI, Hit> hits = new MultiMap<>();

		// get our universe of possible hits
		Map<IRI, String> possibles = getHitUniverse( uri );
		MultiMap<String, IRI> revpos = MultiMap.flip( possibles );

		Directory ramdir = new RAMDirectory();
		StandardAnalyzer analyzer = null;
		SpellChecker speller = null;

		List<IRI> errors = new ArrayList<>();
		try {
			analyzer = new StandardAnalyzer();
			IndexWriterConfig config = new IndexWriterConfig( analyzer );
			speller = new SpellChecker( ramdir, strdist );

			StringBuilder names = new StringBuilder();
			for ( String s : possibles.values() ) {
				names.append( s ).append( "\n" );
			}
			PlainTextDictionary ptd = new PlainTextDictionary( new StringReader( names.toString() ) );
			speller.indexDictionary( ptd, config, true );

			List<IRI> needles = typeToURILkp.get( uri );
			for ( IRI needle : needles ) {
				String needlelabel = labels.get( needle );
				try {
					String[] suggestions = speller.suggestSimilar( needlelabel, 20, minDistance );
					for ( String s : suggestions ) {
						// found a match, so figure out what we actually matched
						float distance = strdist.getDistance( needlelabel, s );

						for ( IRI match : revpos.get( s ) ) {
							hits.add( needle,
									new Hit( match, s, uriToTypeLkp.get( match ), distance ) );
						}
					}
				}
				catch ( Exception e ) {
					// our fallback resolution always works; it's just a ton slower
					errors.add( needle );
				}
			}
		}
		catch ( Exception e ) {
			log.error( e, e );
		}
		finally {
			for ( Closeable c : new Closeable[]{ analyzer, ramdir, speller } ) {
				if ( null != c ) {
					try {
						c.close();
					}
					catch ( Exception e ) {
						log.warn( e, e );
					}
				}
			}
		}

		if ( !errors.isEmpty() ) {
			fallbackResolve( errors, possibles, hits, strdist, minDistance );
		}

		return hits;
	}

	/**
	 * Resolves terms that could not be resolved with the lucene approach. This
	 * brute-force function is significantly slower, but always works
	 *
	 * @param needles the URIs that produced errors in lucene
	 * @param possibles the set of all possible solutions
	 * @param hits populate this multimap with matches
	 * @param levy the string distance object to use to measure hits
	 * @param minDistance the minimum similarity measure
	 */
	private void fallbackResolve( Collection<IRI> needles, Map<IRI, String> possibles,
			MultiMap<IRI, Hit> hits, StringDistance levy, float minDistance ) {
		log.debug( "falling back to resolve " + needles.size() + " items" );

		for ( IRI needle : needles ) {
			String needlelabel = labels.get( needle );

			for ( Map.Entry<IRI, String> en : possibles.entrySet() ) {
				IRI match = en.getKey();
				String matchlabel = en.getValue();

				float distance = levy.getDistance( needlelabel, matchlabel );
				if ( distance >= minDistance && !match.equals( needle ) ) {
					hits.add( needle,
							new Hit( match, matchlabel, uriToTypeLkp.get( match ), distance ) );
				}
			}
		}
	}

	private Map<IRI, String> getHitUniverse( IRI type ) {
		Map<IRI, String> possibles = new HashMap<>();
		if ( across ) {
			possibles.putAll( labels );
		}
		else {
			for ( IRI key : typeToURILkp.getNN( type ) ) {
				possibles.put( key, labels.get( key ) );
			}
		}

		return possibles;
	}

	public class Hit {

		private final IRI match;
		private final String matchLabel;
		private final IRI matchType;
		private final float score;

		public Hit( IRI match, String matchLabel, IRI matchType, float score ) {
			this.match = match;
			this.matchLabel = matchLabel;
			this.matchType = matchType;
			this.score = score;
		}

		public IRI getMatch() {
			return match;
		}

		public String getMatchLabel() {
			return matchLabel;
		}

		public IRI getMatchType() {
			return matchType;
		}

		public float getScore() {
			return score;
		}

		@Override
		public String toString() {
			return "Hit (" + score + ": " + matchLabel + "," + match + " ->" + matchType + ")";
		}
	}
}
