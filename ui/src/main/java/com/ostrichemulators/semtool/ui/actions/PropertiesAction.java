/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;

import com.ostrichemulators.semtool.ui.components.DbMetadataPanel;
import com.ostrichemulators.semtool.ui.components.ProgressTask;

/**
 *
 * @author ryan
 */
public class PropertiesAction extends DbAction {

  private static final Logger log = Logger.getLogger( PropertiesAction.class );
  private final Frame frame;

  public PropertiesAction( Frame frame ) {
    super( null, PROPS, "properties" );
    this.frame = frame;
    putValue( AbstractAction.SHORT_DESCRIPTION, "View/Edit the properties" );
    putValue( AbstractAction.MNEMONIC_KEY, KeyEvent.VK_A );
  }

  @Override
  public void actionPerformed( ActionEvent ae ) {
    DbMetadataPanel.showDialog( frame, getEngine() );
  }

  @Override
  protected ProgressTask getTask( ActionEvent ae ) {
    throw new UnsupportedOperationException( "Not supported yet." );
  }
}
