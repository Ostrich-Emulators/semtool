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
package gov.va.semoss.search;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.ui.transformer.EdgeStrokeTransformer;
import gov.va.semoss.ui.transformer.PaintTransformer;
import gov.va.semoss.ui.transformer.VertexShapeTransformer;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.Utility;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import javax.swing.SwingWorker;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 */
public class SearchController implements KeyListener, FocusListener,
		ActionListener, Runnable {

	private static final Logger log = Logger.getLogger( SearchController.class );
	private static final String TEXT_FIELD = "alltext";
	private static final String URI_FIELD = "URI";
	private JPopupMenu menu = new JPopupMenu();
	private JTextField searchText;

	private long lastTime = 0;
	private Thread thread = null;
	private boolean typed = false;
	private boolean searchContinue = true;

	private PaintTransformer oldTx = null;
	private EdgeStrokeTransformer oldeTx = null;
	private VertexShapeTransformer oldsTx = null;
	private double oldedgesize = 0;
	private int oldfontsize = 0;

	private GraphPlaySheet gps;

	private final StandardAnalyzer analyzer = new StandardAnalyzer( Version.LUCENE_36 );
	private final Directory ramdir = new RAMDirectory();
	private IndexReader reader;
	private IndexSearcher searcher;
	private final Map<URI, SEMOSSVertex> vertStore = new HashMap<>();

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

		// gps.resetTransformers();
	}

	private void handleSelectionOfButton() {
		VisualizationViewer<SEMOSSVertex, SEMOSSEdge> view = gps.getView();
		RenderContext<SEMOSSVertex, SEMOSSEdge> rc = view.getRenderContext();

		Collection<SEMOSSVertex> picked = view.getPickedVertexState().getPicked();

		// set the transformers
		oldTx = (PaintTransformer) rc.getVertexFillPaintTransformer();
		oldTx.setSelected( picked );

		gps.getVertexLabelFontTransformer().setSelected( picked );
		gps.getEdgeLabelFontTransformer().setSelected( view.getPickedEdgeState().getPicked() );
		oldfontsize = gps.getEdgeLabelFontTransformer().getNormalFontSize();
		gps.getEdgeLabelFontTransformer().setNormalFontSize( 0 );

		oldeTx = (EdgeStrokeTransformer) rc.getEdgeStrokeTransformer();
		oldedgesize = oldeTx.getNormalSize();
		oldeTx.setNormalSize( 0.1f );

		oldsTx = (VertexShapeTransformer) rc.getVertexShapeTransformer();
//		Set<SEMOSSVertex> unpicked = new HashSet<>( gps.getGraph().getVertices() );
//		unpicked.removeAll( view.getPickedVertexState().getPicked() );
//		for ( SEMOSSVertex v : unpicked ) {
//			oldsTx.decreaseSize( v );
//		}

		view.repaint();
	}

	private void handleDeselectionOfButton() {
		oldTx.setSelected( null );
		oldeTx.setSelectedEdges( null );
		gps.getVertexLabelFontTransformer().setSelected( null );
		gps.getEdgeLabelFontTransformer().setSelected( null );
		gps.getEdgeLabelFontTransformer().setNormalFontSize( oldfontsize );
		oldsTx.emptySelected();
		oldeTx.reset( oldedgesize, -1, -1 );
		gps.getView().repaint();

		if ( !searchText.getText().isEmpty() ) {
			searchStatement( searchText.getText() );
		}
	}

	/**
	 * Method searchStatement.
	 *
	 * @param searchString String
	 */
	private void searchStatement( String searchString ) {
		StringBuilder query = new StringBuilder( searchString );
		query.append( " label: " ).append( searchString );
		query.append( " description: " ).append( searchString );
		query.append( " type: " ).append( searchString );

		QueryParser alltextqp
				= new QueryParser( Version.LUCENE_36, TEXT_FIELD, analyzer );

		try {
			Query q = alltextqp.parse( query.toString() );

			TopDocs hits = searcher.search( q, 500 );

			gps.getView().getPickedVertexState().clear();

			for ( ScoreDoc sd : hits.scoreDocs ) {
				Document doc = searcher.doc( sd.doc );
				URI uri = new URIImpl( doc.get( URI_FIELD ) );

				if ( vertStore.containsKey( uri ) ) {
					SEMOSSVertex v = vertStore.get( uri );
					log.debug( "selecting node: " + uri + " from search" );
					gps.getView().getPickedVertexState().pick( v, true );
				}
			}
		}
		catch ( ParseException | IOException e ) {
			log.error( e, e );
		}

		gps.getView().repaint();
		searchText.requestFocus( true );
	}

	/**
	 * Method run.
	 */
	@Override
	public void run() {
		try {
			while ( searchContinue ) {
				long thisTime = System.currentTimeMillis();
				if ( thisTime - lastTime > 300 && typed ) {
					synchronized ( menu ) {
						menu.setVisible( false );
						menu.removeAll();
					}
					if ( !( searchText.getText().isEmpty() || 0 == lastTime ) ) {
						searchStatement( searchText.getText() );
					}
					else if ( searchText.getText().isEmpty() && lastTime != 0 ) {
						handleDeselectionOfButton();
						log.debug( "cleared" );
					}
					lastTime = System.currentTimeMillis();
					typed = false;
				}
				else {
					// menu.setVisible(false);
					Thread.sleep( 100 );
				}
			}
		}
		catch ( InterruptedException e ) {
			log.error( e, e );
		}
	}

	/**
	 * Method keyTyped.
	 *
	 * @param e KeyEvent
	 */
	@Override
	public void keyTyped( KeyEvent e ) {
		lastTime = System.currentTimeMillis();
		menu.setVisible( false );
		typed = true;
		synchronized ( this ) {
			this.notify();
		}
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
		if ( thread == null || thread.getState() == Thread.State.TERMINATED ) {
			thread = new Thread( this );
			searchContinue = true;
			thread.start();
			log.info( "Starting thread again" );
		}
	}

	/**
	 * Method focusLost.
	 *
	 * @param e FocusEvent
	 */
	@Override
	public void focusLost( FocusEvent e ) {
		if ( searchText.getText().equalsIgnoreCase( "" ) ) {
			searchText.setText( Constants.ENTER_TEXT );
			searchContinue = false;
			log.info( "Ended the thread" );
		}
	}

	/**
	 * Method indexGraph.
	 *
	 * @param jenaModel Model
	 */
	public void indexGraph( Graph<SEMOSSVertex, SEMOSSEdge> graph, IEngine engine ) {
		SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {

				Set<Resource> needLabels = new HashSet<>();
				for ( SEMOSSEdge e : graph.getEdges() ) {
					needLabels.addAll( e.getProperties().keySet() );
				}
				for ( SEMOSSVertex e : graph.getVertices() ) {
					needLabels.addAll( e.getProperties().keySet() );
				}

				Map<Resource, String> labels = Utility.getInstanceLabels( needLabels, engine );
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
						reader = IndexReader.open( ramdir );
					}
					else {
						IndexReader rdr = IndexReader.openIfChanged( reader );
						if ( null != rdr ) {
							reader = rdr;
						}
						
						searcher.close();
					}

					searcher = new IndexSearcher( reader );
					searchText.setEnabled( true );
				}
				catch ( IOException ioe ) {
					log.warn( "cannot read newly-created search index", ioe );
					searchText.setEnabled( false );
				}
			}
		};

		sw.execute();
	}

	/**
	 * Method setText.
	 *
	 * @param text JTextField
	 */
	public void setText( JTextField _searchText ) {
		searchText = _searchText;
	}

	/**
	 * Method setGPS.
	 *
	 * @param ps GraphPlaySheet
	 */
	public void setGPS( GraphPlaySheet _gps ) {
		gps = _gps;
	}

	/**
	 * Method keyPressed.
	 *
	 * @param arg0 KeyEvent
	 */
	@Override
	public void keyPressed( KeyEvent arg0 ) {
	}

	/**
	 * Method keyReleased.
	 *
	 * @param arg0 KeyEvent
	 */
	@Override
	public
			void keyReleased( KeyEvent arg0 ) {
	}

	private class RepositoryIndexer {

		private IndexWriter indexer;
		private final Map<URI, Document> doccache = new HashMap<>();
		private final Map<URI, StringBuilder> textcache = new HashMap<>();
		private final Map<Resource, String> labels;

		public RepositoryIndexer( Map<Resource, String> labs ) {
			labels = labs;
			IndexWriterConfig config
					= new IndexWriterConfig( Version.LUCENE_36, analyzer );
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
						en.getValue().add( new Field( TEXT_FIELD, sb, Field.Store.YES,
								Field.Index.ANALYZED ) );
					}
				}

				indexer.addDocuments( doccache.values() );
				indexer.commit();
				indexer.close();

				if ( log.isTraceEnabled() ) {
					File lucenedir
							= new File( FileUtils.getTempDirectory(), "search.lucene" );
					IndexWriterConfig config
							= new IndexWriterConfig( Version.LUCENE_36, analyzer );
					IndexWriter iw
							= new IndexWriter( FSDirectory.open( lucenedir ), config );
					iw.addDocuments( doccache.values() );
					iw.commit();
					iw.close();
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
					doc.add( new Field( "URI", sub.stringValue(), Field.Store.YES,
							Field.Index.NOT_ANALYZED ) );

					textcache.put( sub, new StringBuilder() );
				}

				textcache.get( sub ).append( " " ).append( en.getValue().toString() );

				Document doc = doccache.get( sub );

				String label = ( labels.containsKey( pred )
						? labels.get( pred ) : pred.getLocalName() );
				Field f = new Field( label, en.getValue().toString(), Field.Store.YES,
						Field.Index.ANALYZED );
				if ( "Description".equals( label ) ) {
					f.setBoost( 2.0f );
				}
				else if ( "type".equals( label ) ) {
					f.setBoost( 4.0f );
				}
				else if ( "label".equals( label ) ) {
					f.setBoost( 8.0f );
				}

				doc.add( f );
			}
		}
	}
}
