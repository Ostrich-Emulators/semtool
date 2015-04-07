/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.query.util.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import gov.va.semoss.rdf.engine.api.ModificationExecutor;

/**
 *
 * @author ryan
 */
public class StatementAddingExecutor implements ModificationExecutor {

  private boolean trans;
  private final List<Statement> stmts = new ArrayList<>();

  public StatementAddingExecutor() {
    trans = true;
  }

  public StatementAddingExecutor( boolean usetrans ) {
    trans = usetrans;
  }

  public void resetStatements( Collection<Statement> todo ) {
    stmts.clear();
    stmts.addAll( todo );
  }

  public void resetStatements( Statement... stmts ) {
    resetStatements( Arrays.asList( stmts ) );
  }

  public void addStatement( Statement stmt ) {
    stmts.add( stmt );
  }

  public void clear() {
    stmts.clear();
  }

  public void useTransaction( boolean usetran ) {
    trans = usetran;
  }

  public void useTransaction() {
    useTransaction( true );
  }

  @Override
  public boolean execInTransaction() {
    return trans;
  }

  @Override
  public void exec( RepositoryConnection conn ) throws RepositoryException {
    conn.add( stmts );
  }
}
