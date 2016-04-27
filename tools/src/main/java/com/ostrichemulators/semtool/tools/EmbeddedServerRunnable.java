/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.tools;

import com.bigdata.journal.IIndexManager;
import com.bigdata.journal.ITx;
import com.bigdata.journal.Journal;
import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.webapp.NanoSparqlServer;
import com.bigdata.rdf.store.AbstractTripleStore;
import com.ostrichemulators.semtool.rdf.engine.impl.BigDataEngine;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;

/**
 * A class to run the bigdata jetty server
 *
 * @author ryan
 */
public class EmbeddedServerRunnable implements Runnable {

	private static final Logger log = Logger.getLogger( EmbeddedServerRunnable.class );
	private final int port;
	private final IIndexManager mgr;
	private final Map<String, String> opts;

	public EmbeddedServerRunnable( int port, IIndexManager mgr,
			Map<String, String> opts ) {
		this.port = port;
		this.mgr = mgr;
		this.opts = opts;
	}

	public EmbeddedServerRunnable( File jnl ) throws IOException {
		this.port = getFreePort();
		opts = new HashMap<>();

		// the journal is the file itself
		Properties dbprops = BigDataEngine.generateProperties( jnl );		
		Journal journal = new Journal( dbprops );
		AbstractTripleStore triples
				= AbstractTripleStore.class.cast( journal.getResourceLocator().
						locate( "kb", ITx.UNISOLATED ) );
		mgr = triples.getIndexManager();

		for ( String key : dbprops.stringPropertyNames() ) {
			opts.put( key, dbprops.getProperty( key ) );
		}
		opts.put( BigdataSail.Options.READ_ONLY, Boolean.toString( true ) );
	}

	public int getPort() {
		return port;
	}

	private static int getFreePort() throws IOException {
		// find an open port
		int port = 0;
		for ( int i = 1024; i < 65536; i++ ) {
			ServerSocket ss = null;
			try {
				ss = new ServerSocket( i );
				port = i;
				break;
			}
			catch ( Exception e ) {
				// don't care; just go to the next port
			}
			finally {
				if ( null != ss ) {
					ss.close();
				}
			}
		}

		return port;
	}

	@Override
	public void run() {
		Server server = null;

		try {
			log.info( "starting jetty server on port: " + port + "..." );
			server = NanoSparqlServer.newInstance( port,
					mgr, opts );
			server.setStopAtShutdown( true );

			NanoSparqlServer.awaitServerStart( server );
			// Block and wait. The NSS is running.
			log.info( "jetty server started at http://localhost:" + port + "/bigdata" );
			log.info( "use CTRL-C to end" );
			server.join();
		}
		catch ( Throwable t ) {
			log.error( t );
		}
		finally {
			if ( server != null ) {
				try {
					server.stop();
				}
				catch ( Exception e ) {
					log.error( e, e );
				}
				server = null;
				System.gc();
				log.debug( "jetty stopped" );
			}
		}
	}
}
