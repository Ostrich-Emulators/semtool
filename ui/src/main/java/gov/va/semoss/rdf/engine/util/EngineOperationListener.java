/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.util;

import gov.va.semoss.om.Perspective;
import gov.va.semoss.rdf.engine.api.IEngine;
import java.util.Collection;

/**
 *
 * @author ryan
 */
public interface EngineOperationListener {

  public void engineOpened( IEngine eng );

  public void engineClosed( IEngine eng );
  
  public void insightsModified( IEngine eng, Collection<Perspective> perspectives );
}
