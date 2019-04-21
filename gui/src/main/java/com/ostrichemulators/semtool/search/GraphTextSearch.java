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
package com.ostrichemulators.semtool.search;

import com.ostrichemulators.semtool.om.GraphElement;
import edu.uci.ics.jung.graph.Graph;
import com.ostrichemulators.semtool.om.SEMOSSEdge;
import com.ostrichemulators.semtool.om.SEMOSSVertex;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;

import com.ostrichemulators.semtool.util.RetrievingLabelCache;
import com.ostrichemulators.semtool.util.Utility;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

/**
 */
public class GraphTextSearch {

	private static final Logger log = Logger.getLogger( GraphTextSearch.class );
	private static final String ALL_TEXT = "alltext";
	private static final String IRI_FIELD = "IRI";

	private final StandardAnalyzer analyzer = new StandardAnalyzer();
	private final Directory ramdir = new RAMDirectory();
	private DirectoryReader reader;
	private IndexSearcher searcher;
	private final Map<IRI, GraphElement> vertStore = new HashMap<>();
	private boolean indexing = false;
	private final Map<IRI, Float> boosts = new HashMap<>();
  private static final ValueFactory vf = SimpleValueFactory.getInstance();

	/**
	 * Creates a new index using {@link RDFS#LABEL}, {@link RDF#TYPE}, and
	 * {@link DCTERMS#DESCRIPTION}, with boosts of 8,4, and 2, respectively.
	 */
	public GraphTextSearch() {
		boosts.put( RDFS.LABEL, 8f );
		boosts.put( RDF.TYPE, 4f );
		boosts.put( DCTERMS.DESCRIPTION, 2f );
	}

	public GraphTextSearch( Map<IRI, Float> boosts ) {
		this.boosts.putAll( boosts );
	}

	/**
	 * A convenience function to 
	 * {@link #search(org.apache.lucene.search.Query, java.util.Set, java.util.Set) }
	 * that uses some internal logic to formulate the search from the given string
	 *
	 * @param query
	 * @param nodes
	 * @param edges
	 */
	public void search( String query, Set<SEMOSSVertex> nodes, Set<SEMOSSEdge> edges ) {
		try {
			StringBuilder sb = new StringBuilder();
			Pattern pat = Pattern.compile( "^(\\S*):(.*)" );
			Matcher m = pat.matcher( query );
			String field = ALL_TEXT;

			if ( m.matches() ) {
				field = m.group( 1 );
				sb.append( m.group( 2 ) );
			}
			else {
				sb.append( "label: " ).append( query ).append( "*" );
				sb.append( " description: " ).append( query );
				sb.append( " type: " ).append( query );
			}

			if ( 0 == sb.length() ) {
				return;
			}

			QueryParser alltextqp = new QueryParser( field, analyzer );
			Query q = alltextqp.parse( sb.toString() );
			log.debug( q );
			search( q, nodes, edges );
		}
		catch ( ParseException e ) {
			log.warn( e );
		}
		catch ( Exception e ) {
			log.error( e, e );
		}
	}

	/**
	 * A convenience function to
	 * {@link #search(java.lang.String, java.util.Set, java.util.Set)}.
	 *
	 * @param searchString
	 * @return
	 */
	public List<? extends GraphElement> search( String searchString ) {

		Set<SEMOSSVertex> vs = new HashSet<>();
		Set<SEMOSSEdge> es = new HashSet<>();
		search( searchString, vs, es );

		List<GraphElement> graphhits = new ArrayList<>();
		graphhits.addAll( vs );
		graphhits.addAll( es );
		return graphhits;
	}

	public void search( Query q, Set<SEMOSSVertex> nodes, Set<SEMOSSEdge> edges ) {
		try {
			TopDocs hits = searcher.search( q, 500 );

			for ( ScoreDoc sd : hits.scoreDocs ) {
				Document doc = searcher.doc( sd.doc );
				IRI IRI = vf.createIRI( doc.get( IRI_FIELD ) );

				if ( vertStore.containsKey( IRI ) ) {
					GraphElement v = vertStore.get( IRI );
					if ( v.isNode() ) {
						nodes.add( SEMOSSVertex.class.cast( v ) );
					}
					else {
						edges.add( SEMOSSEdge.class.cast( v ) );
					}
				}
			}
		}
		catch ( IOException e ) {
			log.error( e, e );
		}
	}

	public boolean isIndexing() {
		return indexing;
	}

	/**
	 * Replaces the current index with the data from the given graph
	 *
	 * @param graph
	 * @param engine
	 * @throws java.io.IOException
	 */
	public void index( Graph<SEMOSSVertex, SEMOSSEdge> graph, IEngine engine ) throws IOException {
		log.trace( "asking to update search index" );

		if ( indexing ) {
			log.debug( "already indexing" );
		}

		log.debug( "indexing graph for searchbar" );
		indexing = true;
		Date lastIndexed = new Date();
		vertStore.clear();

		// pre-fetch the stuff we know we're going to need
		RetrievingLabelCache rlc = new RetrievingLabelCache( engine );
		Set<IRI> needLabels = new HashSet<>();
		for ( SEMOSSEdge e : graph.getEdges() ) {
			needLabels.addAll( e.getPropertyKeys() );
		}
		for ( SEMOSSVertex e : graph.getVertices() ) {
			needLabels.addAll( e.getPropertyKeys() );
		}
		rlc.putAll( Utility.getInstanceLabels( needLabels, engine ) );

		// now we can run the indexer
		RepositoryIndexer ri = new RepositoryIndexer( rlc );
		for ( SEMOSSEdge e : graph.getEdges() ) {
			vertStore.put( e.getIRI(), e );
			ri.handleProperties( e.getIRI(), e.getValues() );
		}
		for ( SEMOSSVertex v : graph.getVertices() ) {
			vertStore.put( v.getIRI(), v );
			ri.handleProperties( v.getIRI(), v.getValues() );
		}

		ri.finish();

		try {
			if ( null == reader ) {
				reader = DirectoryReader.open( ramdir );
			}
			else {
				DirectoryReader rdr = DirectoryReader.openIfChanged( reader );
				if ( null != rdr ) {
					reader = rdr;
				}
			}

			searcher = new IndexSearcher( reader );
		}
		catch ( IOException ioe ) {
			throw new IOException( "cannot read newly-created search index", ioe );
		}
		finally {
			indexing = false;
			log.debug( "done indexing graph: "
					+ Utility.getDuration( lastIndexed, new Date() ) );
		}
	}

	private class RepositoryIndexer {

		private IndexWriter indexer;
		private final Map<IRI, Document> doccache = new HashMap<>();
		private final Map<IRI, StringBuilder> textcache = new HashMap<>();
		private final RetrievingLabelCache labels;

		public RepositoryIndexer( RetrievingLabelCache rlc ) {
			labels = rlc;
			IndexWriterConfig config = new IndexWriterConfig( analyzer );
			config.setOpenMode( IndexWriterConfig.OpenMode.CREATE );
			try {
				indexer = new IndexWriter( ramdir, config );
			}
			catch ( Exception e ) {
				log.error( "could not create lucene index", e );
			}
		}

		public void finish() {
			try {
				for ( Map.Entry<IRI, Document> en : doccache.entrySet() ) {
					String sb = textcache.get( en.getKey() ).toString().trim();
					if ( !sb.isEmpty() ) {
						en.getValue().add( new TextField( ALL_TEXT, sb, Field.Store.YES ) );
					}
				}

				indexer.addDocuments( doccache.values() );
				indexer.commit();
				indexer.close();

				if ( log.isTraceEnabled() ) {
					File lucenedir
							= new File( FileUtils.getTempDirectory(), "search.lucene" );
					IndexWriterConfig config
							= new IndexWriterConfig( analyzer );
					try ( IndexWriter iw = new IndexWriter( FSDirectory.open( lucenedir.toPath() ), config ) ) {
						iw.addDocuments( doccache.values() );
						iw.commit();
					}
				}

				textcache.clear();
				doccache.clear();
			}
			catch ( Exception e ) {
				log.error( "could not add/commit lucene index", e );
			}
		}

		public void handleProperties( IRI sub, Map<IRI, Value> props ) {
			for ( Map.Entry<IRI, Value> en : props.entrySet() ) {
				IRI pred = en.getKey();

				if ( !doccache.containsKey( sub ) ) {
					Document doc = new Document();
					doccache.put( sub, doc );
					doc.add( new StringField( "IRI", sub.stringValue(), Field.Store.YES ) );
					textcache.put( sub, new StringBuilder() );
				}

				textcache.get( sub ).append( " " ).append( en.getValue().toString() );

				Document doc = doccache.get( sub );

				String label = labels.get( pred );
				String text = labels.get( en.getValue() );
				Field f = new TextField( label, text, Field.Store.YES );
				f.setBoost( boosts.getOrDefault( pred, 1f ) );
				doc.add( f );

				Field f2 = new TextField( pred.stringValue(), text, Field.Store.YES );
				f2.setBoost( boosts.getOrDefault( pred, 1f ) );
				doc.add( f2 );
			}
		}
	}
}
