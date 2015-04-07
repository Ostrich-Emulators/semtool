package gov.va.semoss.rdf.engine.api;

import java.util.Collection;

public interface IExplorable {

  // gets the from neighborhood for a given node
  public Collection<String> getFromNeighbors( String nodeType, int neighborHood );

  // gets the to nodes
  public Collection<String> getToNeighbors( String nodeType, int neighborHood );

  // gets the from and to nodes
  public Collection<String> getNeighbors( String nodeType, int neighborHood );

  // gets the insight database
  public InsightManager getInsightManager();

  public void setInsightManager( InsightManager eng );
}
