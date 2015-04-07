/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.util.EngineUtil;
import gov.va.semoss.ui.components.ProgressTask;

/**
 *
 * @author ryan
 */
public class UnmountAction extends DbAction {

  private static final Logger log = Logger.getLogger( UnmountAction.class );
  private Frame frame;

  public UnmountAction( Frame frame ) {
    super( null, UNMOUNT, "rmdb" );
    putValue( AbstractAction.SHORT_DESCRIPTION, "Unmount a database" );
  }

  @Override
  public void setEngine( IEngine eng ) {
    super.setEngine( eng );

    if ( null != eng ) {
      putValue( AbstractAction.NAME, UNMOUNT + " " + getEngineName() );
    }
  }

  @Override
  public void actionPerformed( ActionEvent e ) {
    if ( null != getEngine() ) {
      int val = JOptionPane.showConfirmDialog( frame, "Really detach "
          + getEngineName() + "?", "Confirm Detach", JOptionPane.YES_NO_OPTION );
      if ( JOptionPane.YES_OPTION == val ) {
        EngineUtil.getInstance().unmount( getEngine() );
      }
    }
  }

  @Override
  protected ProgressTask getTask( ActionEvent ae ) {
    throw new UnsupportedOperationException( "Not supported yet." );
  }
}
