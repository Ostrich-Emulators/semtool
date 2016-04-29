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
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;

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
	private final Map<URI, String> labels = new HashMap<>();
	private final Map<URI, URI> uriToTypeLkp = new HashMap<>();
	private final MultiMap<URI, URI> typeLkp = new MultiMap<>();

	public EngineConsistencyChecker( IEngine eng, boolean across, StringDistance dist ) {
		this.engine = eng;
		this.across = across;
		this.strdist = dist;
	}

	public void release() {
		labels.clear();
		uriToTypeLkp.clear();
		typeLkp.clear();
	}

	/**
	 * Adds the given uris as the specified type
	 *
	 * @param uris A collection of concept classes (not instances)
	 * @param type
	 */
	public void add( Collection<URI> uris, Type type ) {
		if ( Type.CONCEPT == type ) {
			for ( URI uri : uris ) {
				makeConceptDocuments( uri );
			}
		}
		else {
			for ( URI uri : uris ) {
				makeRelationDocuments( uri );
			}
		}
	}

	private void makeConceptDocuments( URI concept ) {

		String query = "SELECT ?s ?type ?slabel WHERE {"
				+ " ?s ?rdftype ?concept . "
				+ " ?s ?label ?slabel . "
				+ " ?s ?rdftype ?type . "
				+ " FILTER ( ?type != ?rsr ) } ORDER BY ?s";
		VoidQueryAdapter vqa = new VoidQueryAdapter( query ) {
			URI lastS = null;
			Document currentDoc = null;
			Set<String> seenLabels = new HashSet<>();

			@Override
			public void handleTuple( BindingSet set, ValueFactory fac ) {
				URI s = URI.class.cast( set.getValue( "s" ) );
				if ( s != lastS ) {
					seenLabels.clear();
					typeLkp.add( concept, s );
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

		vqa.bind( "rdftype", RDF.TYPE );
		vqa.bind( "concept", concept );
		vqa.bind( "label", RDFS.LABEL );
		vqa.bind( "rsr", RDFS.RESOURCE );
		engine.queryNoEx( vqa );
	}

	private void makeRelationDocuments( URI rel ) {
		String query = "SELECT ?s ?slabel WHERE {"
				+ " ?s rdf:predicate ?rel . "
				+ " ?s rdfs:label ?slabel . "
				+ " FILTER ( ?rel != rdfs:resource ) . FILTER ( ?s != ?rel ) "
				+ "} ORDER BY ?s";
		VoidQueryAdapter vqa = new VoidQueryAdapter( query ) {
			URI lastS = null;
			Set<String> seenLabels = new HashSet<>();

			@Override
			public void handleTuple( BindingSet set, ValueFactory fac ) {
				URI s = URI.class.cast( set.getValue( "s" ) );
				if ( s != lastS ) {
					seenLabels.clear();

					// add the URI information now; seenLabels later (avoid saving too much data)
					typeLkp.add( rel, s );
					uriToTypeLkp.put( s, rel );
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

		vqa.bind( "concept", rel );
		engine.queryNoEx( vqa );
	}

	public int getItemsForType( URI uri ) {
		return typeLkp.getNN( uri ).size();
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
	public MultiMap<URI, Hit> check( URI uri, final float minDistance ) {
		MultiMap<URI, Hit> hits = new MultiMap<>();

		// get our universe of possible hits
		Map<URI, String> possibles = getHitUniverse( uri );
		MultiMap<String, URI> revpos = MultiMap.flip( possibles );

		Directory ramdir = new RAMDirectory();
		StandardAnalyzer analyzer = null;
		SpellChecker speller = null;

		List<URI> errors = new ArrayList<>();
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

			List<URI> needles = typeLkp.get( uri );
			for ( URI needle : needles ) {
				String needlelabel = labels.get( needle );
				try {
					String[] suggestions = speller.suggestSimilar( needlelabel, 20, minDistance );
					for ( String s : suggestions ) {
						// found a match, so figure out what we actually matched
						float distance = strdist.getDistance( needlelabel, s );

						for ( URI match : revpos.get( s ) ) {
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
	private void fallbackResolve( Collection<URI> needles, Map<URI, String> possibles,
			MultiMap<URI, Hit> hits, StringDistance levy, float minDistance ) {
		log.debug( "falling back to resolve " + needles.size() + " items" );

		for ( URI needle : needles ) {
			String needlelabel = labels.get( needle );

			for ( Map.Entry<URI, String> en : possibles.entrySet() ) {
				URI match = en.getKey();
				String matchlabel = en.getValue();

				float distance = levy.getDistance( needlelabel, matchlabel );
				if ( distance >= minDistance && !match.equals( needle ) ) {
					hits.add( needle,
							new Hit( match, matchlabel, uriToTypeLkp.get( match ), distance ) );
				}
			}
		}
	}

	private Map<URI, String> getHitUniverse( URI type ) {
		Map<URI, String> possibles = new HashMap<>();
		if ( across ) {
			possibles.putAll( labels );
		}
		else {
			for ( URI key : typeLkp.getNN( type ) ) {
				possibles.put( key, labels.get( key ) );
			}
		}

		return possibles;
	}

	public class Hit {

		private final URI match;
		private final String matchLabel;
		private final URI matchType;
		private final float score;

		public Hit( URI match, String matchLabel, URI matchType, float score ) {
			this.match = match;
			this.matchLabel = matchLabel;
			this.matchType = matchType;
			this.score = score;
		}

		public URI getMatch() {
			return match;
		}

		public String getMatchLabel() {
			return matchLabel;
		}

		public URI getMatchType() {
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
