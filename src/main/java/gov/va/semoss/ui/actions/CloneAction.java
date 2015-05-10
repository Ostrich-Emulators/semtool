/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;
import org.openrdf.repository.RepositoryException;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.util.Utility;
import gov.va.semoss.rdf.engine.util.EngineManagementException;
import gov.va.semoss.rdf.engine.util.EngineUtil;
import gov.va.semoss.ui.components.CloneDataPanel;
import gov.va.semoss.ui.components.ProgressTask;

/**
 *
 * @author ryan
 */
public class CloneAction extends DbAction {

  private static final Logger log = Logger.getLogger( CloneAction.class );
  private final Frame frame;
  private EngineUtil.DbCloneMetadata md = null;

  public CloneAction( String optg, Frame frame ) {
    super( optg, CLONE );
    this.frame = frame;
    putValue( AbstractAction.SHORT_DESCRIPTION, "Duplicate a database" );
    putValue( AbstractAction.MNEMONIC_KEY, KeyEvent.VK_C );
    putValue( AbstractAction.SMALL_ICON, DbAction.getIcon( "db_copy1") );
    
  }

  @Override
  public void setEngine( IEngine eng ) {
    super.setEngine( eng );
    putValue( AbstractAction.NAME, "Copy "+getEngineName() );
  }

  @Override
  public boolean preAction( ActionEvent ae ) {
    md = CloneDataPanel.showDialog( frame, getEngine(), true, true );
    return ( null != md );
  }

  @Override
  protected ProgressTask getTask( ActionEvent ae ) {
    StringBuilder name = new StringBuilder( "Cloning " );
    if ( md.isInsights() && !md.isData() ) {
      name.append( "Configuration of " );
    }
    name.append( getEngineName() );

    ProgressTask pt = new ProgressTask( name.toString(), new Runnable() {
      @Override
      public void run() {
        try {
          EngineUtil.getInstance().clone( getEngine(), md, true );
        }
        catch ( RepositoryException | IOException | EngineManagementException ex ) {
          Utility.showError( ex.getMessage() );
          log.error( ex );
        }
      }
    } );

    return pt;
  }
}
