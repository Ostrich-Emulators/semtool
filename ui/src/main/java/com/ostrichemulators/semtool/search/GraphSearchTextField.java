/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.search;

import com.ostrichemulators.semtool.om.SEMOSSEdge;
import com.ostrichemulators.semtool.om.SEMOSSVertex;
import com.ostrichemulators.semtool.ui.components.playsheets.GraphPlaySheet;
import edu.uci.ics.jung.graph.Graph;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class GraphSearchTextField extends JTextField {

	private static final Logger log = Logger.getLogger( GraphSearchTextField.class );
	private static final String ENTER_TEXT = "Graph Search";
	private static final int REINDEX_WAIT_MS = 2500; // 2.5 seconds

	private final GraphTextSearch gts = new GraphTextSearch();
	private GraphPlaySheet gps;

	public GraphSearchTextField() {
		super( ENTER_TEXT );

		addKeyListener( new KeyAdapter() {
			@Override
			public void keyReleased( KeyEvent e ) {
				gps.getView().clearHighlighting();

				if ( !getText().isEmpty() ) {
					Set<SEMOSSVertex> vs = new HashSet<>();
					Set<SEMOSSEdge> es = new HashSet<>();
					gts.search( getText(), vs, es );

					if ( !( vs.isEmpty() && es.isEmpty() ) ) {
						gps.getView().highlight( vs, null );
					}
					requestFocus( true );
				}
			}
		} );

		addFocusListener( new FocusListener() {
			@Override
			public void focusGained( FocusEvent e ) {
				if ( getText().equalsIgnoreCase( ENTER_TEXT ) ) {
					setText( "" );
				}
			}

			@Override
			public void focusLost( FocusEvent e ) {
				if ( getText().isEmpty() ) {
					setText( ENTER_TEXT );
				}
			}
		} );
	}

	public void setPlaySheet( GraphPlaySheet g ) {
		gps = g;
	}

	public void index( Graph<SEMOSSVertex, SEMOSSEdge> graph ) {
		if ( gts.isIndexing() ) {
			// we're already indexing, so try again in a little bit
			Timer timer = new Timer( REINDEX_WAIT_MS, new ActionListener() {

				@Override
				public void actionPerformed( ActionEvent e ) {
					index( graph );
				}
			} );

			timer.setRepeats( false );
			timer.start();
		}
		else {
			setText( "indexing..." );
			setEnabled( false );

			new SwingWorker<Boolean, Void>() {
				@Override
				protected Boolean doInBackground() throws Exception {
					boolean ok = false;
					try {
						gts.index( graph, gps.getEngine() );
						ok = true;
					}
					catch ( IOException ioe ) {
						log.error( ioe, ioe );
						setText( "indexing failed: " + ioe.getLocalizedMessage() );
					}

					return ok;
				}

				@Override
				protected void done() {
					try {
						if ( get() ) {
							setEnabled( true );
							setText( ENTER_TEXT );
						}
					}
					catch ( InterruptedException | ExecutionException ie ) {
						log.error( ie, ie );
					}
				}

			}.execute();
		}
	}
}
