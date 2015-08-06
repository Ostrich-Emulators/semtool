/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.util;

import java.util.concurrent.CountDownLatch;
import org.apache.log4j.Logger;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * A class to encapsulate the lifespan of an engine. Bigdata repositories must
 * be closed on the same thread that started them, which doesn't happen in the
 * GUI. This class lets that happen. While the thread is running, the connection
 * will be available. Once {@link #stop()} is called, the connection will be
 * closed.
 *
 * @author ryan
 */
public class EngineRunningThread<T extends Repository, V extends RepositoryConnection> extends Thread {

	private static final Logger log = Logger.getLogger( EngineRunningThread.class );
	private T repo;
	private V conn;
	private boolean stopping = false;
	private boolean startup = false;
	private CountDownLatch latch;

	/**
	 * Creates a new runnable, and initializes the given repository (if needed)
	 *
	 * @param repo
	 */
	public EngineRunningThread() {
	}

	/**
	 * Closes the repository and its connection.
	 *
	 * @param l this latch will be counted down when the release is complete
	 */
	public synchronized void release( CountDownLatch l ) {
		stopping = true;
		startup = false;
		latch = l;
		notifyAll();
	}

	/**
	 * Sets the repository to initialize and get a connection from.
	 *
	 * @param repo the repository to initialize and get a connection from
	 * @param l this latch will be counted down when the release is complete
	 */
	public synchronized void startup( T repo, CountDownLatch l ) {
		this.repo = repo;
		startup = true;
		stopping = false;
		latch = l;
		notifyAll();
	}

	@Override
	public void run() {
		while ( !stopping ) {
			// if we're just now starting, open the repo and get a connection
			boolean needToStop = false;

			if ( startup ) {
				log.debug( "initializing a repository in a thread" );
				try {
					if ( !repo.isInitialized() ) {
						repo.initialize();
					}

					try {
						conn = (V) repo.getConnection();
					}
					catch ( RepositoryException re ) {
						try {
							repo.shutDown();
						}
						catch ( Exception e ) {
							log.warn( "could not shutdown repo after failing to make a connection",
									e );
							throw re;
						}
					}
				}
				catch ( RepositoryException re ) {
					log.error( re, re );
					needToStop = true; // we can't continue
				}
				finally {
					latch.countDown();
				}
			}

			if ( !needToStop ) {
				// nothing to do, just don't stop
				synchronized ( this ) {
					try {
						log.debug( "going to sleep until harvest time" );
						wait();
					}
					catch ( InterruptedException ie ) {
						log.error( "interrupted", ie );
					}
				}

				log.debug( "awaken, checking for harvest time" );
			}
		}

		// we need to stop
		log.debug( "harvesting the connection" );

		try {
			conn.close();
		}
		catch ( Exception re ) {
			log.warn( re, re );
		}

		try {
			repo.shutDown();
		}
		catch ( Exception re ) {
			log.warn( re, re );
		}

		latch.countDown();
	}

	/**
	 * Retrieves the connection created during 
	 * {@link #startup(org.openrdf.repository.Repository) }
	 *
	 * @return
	 */
	public V getConnection() {
		return conn;
	}
}
