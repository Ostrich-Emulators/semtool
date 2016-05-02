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

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import com.ostrichemulators.semtool.om.SEMOSSEdge;
import com.ostrichemulators.semtool.om.SEMOSSVertex;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.ui.components.playsheets.GraphPlaySheet;
import com.ostrichemulators.semtool.util.Constants;

import com.ostrichemulators.semtool.util.Utility;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingWorker;

import javax.swing.Timer;
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
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 */
public class SearchController implements KeyListener, FocusListener,
		ActionListener {

	private static final Logger log = Logger.getLogger( SearchController.class );
	private static final int REINDEX_WAIT_MS = 2500; // 2.5 seconds

	private static final String TEXT_FIELD = "alltext";
	private static final String URI_FIELD = "URI";
	private final JTextField searchText;

	private GraphPlaySheet gps;

	private final StandardAnalyzer analyzer = new StandardAnalyzer();
	private final Directory ramdir = new RAMDirectory();
	private DirectoryReader reader;
	private IndexSearcher searcher;
	private final Map<URI, SEMOSSVertex> vertStore = new HashMap<>();
	private Date lastIndexed = null;
	private boolean indexing = false;

	public SearchController( JTextField field ) {
		searchText = field;

		field.addKeyListener( this );
		field.addFocusListener( this );
		field.setText( Constants.ENTER_TEXT );
	}

	// toggle button listener
	// this will swap the view based on what is being presented
	/**
	 * Method actionPerformed.
	 *
	 * @param e ActionEvent
	 */
	@Override
	public void actionPerformed( ActionEvent e ) {
		if ( ( (JToggleButton) e.getSource() ).isSelected() ) {
			handleSelectionOfButton();
		}
		else {
			handleDeselectionOfButton();
		}
	}

	private void handleSelectionOfButton() {
		VisualizationViewer<SEMOSSVertex, SEMOSSEdge> view = gps.getView();
		gps.getView().clearHighlighting();
		gps.getView().skeleton( view.getPickedVertexState().getPicked(), null );
	}

	private void handleDeselectionOfButton() {
		VisualizationViewer<SEMOSSVertex, SEMOSSEdge> view = gps.getView();
		gps.getView().clearHighlighting();
		gps.getView().highlight( view.getPickedVertexState().getPicked(), null );
	}

	private void searchStatement( String searchString ) {
		StringBuilder query = new StringBuilder();
		if ( searchString.isEmpty() ) {
			VisualizationViewer<SEMOSSVertex, SEMOSSEdge> view = gps.getView();
			view.getPickedVertexState().clear();
			gps.getView().clearHighlighting();
		}

		query.append( " label: " ).append( searchString ).append( "*" );
		query.append( " description: " ).append( searchString );
		query.append( " type: " ).append( searchString );

		QueryParser alltextqp = new QueryParser( TEXT_FIELD, analyzer );

		try {
			Query q = alltextqp.parse( query.toString() );

			TopDocs hits = searcher.search( q, 500 );

			List<SEMOSSVertex> verts = new ArrayList<>();

			gps.getView().getPickedVertexState().clear();
			for ( ScoreDoc sd : hits.scoreDocs ) {
				Document doc = searcher.doc( sd.doc );
				URI uri = new URIImpl( doc.get( URI_FIELD ) );

				if ( vertStore.containsKey( uri ) ) {
					SEMOSSVertex v = vertStore.get( uri );
					verts.add( v );
					log.debug( "selecting node: " + uri + " from search" );
					gps.getView().getPickedVertexState().pick( v, true );
				}
			}

			boolean skel = gps.getVertexLabelFontTransformer().isSkeletonMode();
			gps.getView().clearHighlighting();
			if ( skel ) {
				gps.getView().skeleton( verts, null );
			}
			else {
				gps.getView().highlight( verts, null );
			}
		}
		catch ( ParseException | IOException e ) {
			log.error( e, e );
		}

		searchText.requestFocus( true );
	}

	// focus listener
	/**
	 * Method focusGained.
	 *
	 * @param e FocusEvent
	 */
	@Override
	public void focusGained( FocusEvent e ) {
		if ( searchText.getText().equalsIgnoreCase( Constants.ENTER_TEXT ) ) {
			searchText.setText( "" );
		}
	}

	/**
	 * Method focusLost.
	 *
	 * @param e FocusEvent
	 */
	@Override
	public void focusLost( FocusEvent e ) {
		if ( searchText.getText().isEmpty() ) {
			searchText.setText( Constants.ENTER_TEXT );
		}
	}

	/**
	 * Method indexGraph.
	 *
	 * @param jenaModel Model
	 */
	public void indexGraph( Graph<SEMOSSVertex, SEMOSSEdge> graph, IEngine engine ) {
		log.trace( "asking to update searchbar index" );
		if ( indexing ) {
			Timer timer = new Timer( REINDEX_WAIT_MS, new ActionListener() {

				@Override
				public void actionPerformed( ActionEvent e ) {
					int timeSinceLastUpdate
							= (int) ( new Date().getTime() - lastIndexed.getTime() );
					if ( timeSinceLastUpdate >= REINDEX_WAIT_MS ) {
						indexGraph( graph, engine );
					}
				}
			} );
			timer.setRepeats( false );
			timer.start();
		}
		else {
			reallyIndexGraph( graph, engine );
			lastIndexed = new Date();
		}
	}

	private void reallyIndexGraph( Graph<SEMOSSVertex, SEMOSSEdge> graph, IEngine engine ) {
		SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				log.debug( "indexing graph for searchbar" );
				indexing = true;
				vertStore.clear();
				Set<Resource> needLabels = new HashSet<>();
				for ( SEMOSSEdge e : graph.getEdges() ) {
					needLabels.addAll( e.getProperties().keySet() );
				}
				for ( SEMOSSVertex e : graph.getVertices() ) {
					needLabels.addAll( e.getProperties().keySet() );
				}

				Map<Resource, String> labels
						= Utility.getInstanceLabels( needLabels, engine );
				RepositoryIndexer ri = new RepositoryIndexer( labels );

				for ( SEMOSSEdge e : graph.getEdges() ) {
					ri.handleProperties( e.getURI(), e.getProperties() );
				}
				for ( SEMOSSVertex v : graph.getVertices() ) {
					vertStore.put( v.getURI(), v );
					ri.handleProperties( v.getURI(), v.getProperties() );
				}

				ri.finish();
				return null;
			}

			@Override
			public void done() {
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
					searchText.setEnabled( true );
				}
				catch ( IOException ioe ) {
					log.warn( "cannot read newly-created search index", ioe );
					searchText.setEnabled( false );
				}
				finally {
					indexing = false;
					log.debug( "done indexing graph: "
							+ Utility.getDuration( lastIndexed, new Date() ) );
				}
			}
		};

		sw.execute();
	}

	public void setGPS( GraphPlaySheet _gps ) {
		gps = _gps;
	}

	@Override
	public void keyTyped( KeyEvent e ) {
	}

	@Override
	public void keyPressed( KeyEvent e ) {
	}

	@Override
	public void keyReleased( KeyEvent e ) {
		searchStatement( searchText.getText() );
	}

	private class RepositoryIndexer {

		private IndexWriter indexer;
		private final Map<URI, Document> doccache = new HashMap<>();
		private final Map<URI, StringBuilder> textcache = new HashMap<>();
		private final Map<Resource, String> labels;

		public RepositoryIndexer( Map<Resource, String> labs ) {
			labels = labs;
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
				for ( Map.Entry<URI, Document> en : doccache.entrySet() ) {
					String sb = textcache.get( en.getKey() ).toString().trim();
					if ( !sb.isEmpty() ) {
						en.getValue().add( new TextField( TEXT_FIELD, sb, Field.Store.YES ) );
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

		public void handleProperties( URI sub, Map<URI, Object> props ) {
			for ( Map.Entry<URI, Object> en : props.entrySet() ) {
				URI pred = en.getKey();
				if ( !doccache.containsKey( sub ) ) {
					Document doc = new Document();
					doccache.put( sub, doc );
					doc.add( new StringField( "URI", sub.stringValue(), Field.Store.YES ) );

					textcache.put( sub, new StringBuilder() );
				}

				textcache.get( sub ).append( " " ).append( en.getValue().toString() );

				Document doc = doccache.get( sub );

				String label = ( labels.containsKey( pred )
						? labels.get( pred ) : pred.getLocalName() );
				Field f = new TextField( label, en.getValue().toString(), Field.Store.YES );
				if ( null != label ) {
					switch ( label ) {
						case "Description":
							f.setBoost( 2.0f );
							break;
						case "type":
							f.setBoost( 4.0f );
							break;
						case "label":
							f.setBoost( 8.0f );
							break;
					}
				}

				doc.add( f );
			}
		}
	}
}
