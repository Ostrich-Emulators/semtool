package gov.va.semoss.rdf.engine.api;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * An interface to specify generic update/inserts against the triplestore
 *
 * @author ryan
 */
public interface ModificationExecutor {

  /**
   * Executes the this instance's logic on the given connection. If 
   * {@link #execInTransaction() } returns true, the logic will be executed in
   * within a transaction.
   * @param conn
   * @throws RepositoryException 
   */
  public void exec( RepositoryConnection conn ) throws RepositoryException;
  
  /**
   * Should this modification be done in a transaction?
   * @return 
   */
  public boolean execInTransaction();
}