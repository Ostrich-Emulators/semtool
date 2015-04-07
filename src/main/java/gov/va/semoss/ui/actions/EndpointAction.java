/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.actions;

import java.awt.Desktop;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.ServerSocket;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.util.Utility;
import gov.va.semoss.ui.components.ProgressTask;

/**
 *
 * @author ryan
 */
public class EndpointAction extends DbAction {

  private static final Logger log = Logger.getLogger( EndpointAction.class );
  private final Frame frame;
  private Integer port = 0;

  public EndpointAction( String optg, Frame frame ) {
    super( optg, ENDPOINT, "world" );
    this.frame = frame;
    putValue( AbstractAction.SHORT_DESCRIPTION, "Start/Stop the SPARQL endpoint" );
  }

  @Override
  public void setEngine( IEngine eng ) {
    super.setEngine( eng );

    if ( null != eng ) {
      putValue( AbstractAction.NAME,
          ( eng.serverIsRunning() ? "Stop" : "Start" ) + " SPARQL endpoint" );
    }
  }

  @Override
  protected boolean preAction( ActionEvent ae ) {
    if ( getEngine().serverIsRunning() ) {
      int retval = JOptionPane.showConfirmDialog( frame, "Stop the server?",
          "Server is Running", JOptionPane.YES_NO_OPTION );
      return ( JOptionPane.YES_OPTION == retval );
    }

    port = 0;
    // to get an open port, start up a server, get the port, close the server
    ServerSocket socket = null;
    try {
      socket = new ServerSocket( 0 );
      port = socket.getLocalPort();
    }
    catch ( IOException ioe ) {
      log.warn( ioe );
    }
    finally {
      if ( null != socket ) {
        try {
          socket.close();
        }
        catch ( IOException ioe ) {
          log.warn( ioe );
          return false;
        }
      }
    }

    return true;
//    // prompt the user for a new port (give them a valid option)
//    String val = JOptionPane.showInputDialog( "Port (almost any is fine)?", port );
//    if ( null == val ) {
//      Utility.showMessage( "You must select a port to start the server" );
//      return false;
//    }
//
//    try {
//      port = Integer.parseInt( val );
//      return true;
//    }
//    catch ( Exception npe ) {
//      Utility.showError( "Unparseable port number: " + val );
//      log.error( "Unparseable port number: " + val, npe );
//    }
//
//    return false;
  }

  @Override
  protected ProgressTask getTask( ActionEvent ae ) {
    String name = ( getEngine().serverIsRunning() ? "Stopping " : "Starting " )
        + "SPARQL endpoint";
    final boolean alreadyRunning = getEngine().serverIsRunning();
    
    ProgressTask pt = new ProgressTask( name,
        new Runnable() {
          @Override
          public void run() {
            try {
              
              if ( alreadyRunning ){
                getEngine().stopServer();
              }
              else {
                getEngine().startServer( port );
              }
            }
            catch ( Exception re ) {
              Utility.showError( re.getLocalizedMessage() );
              log.error( re );
            }
          }
        } ) {
          @Override
          public void done() {
            super.done();
            
            if( alreadyRunning ){
              // we just stopped the server, so move along
              return;
            }
            
            // this is a little risky...I sure hope the server is started
            // by the time the user clicks "yes"
            if ( Desktop.isDesktopSupported() ) {
              int retval = JOptionPane.showConfirmDialog( frame,
                  "Server started at " + getEngine().getServerUri()
                      + ". Open a browser now?", "Server is up",
                  JOptionPane.YES_NO_OPTION );
              if ( JOptionPane.YES_OPTION == retval ) {
                try {
                  Desktop.getDesktop().browse( getEngine().getServerUri() );
                }
                catch ( IOException ioe ) {
                  //Utility.showError( "Problem opening the browser" );
                  log.warn( "problem opening a browser", ioe );
                  JOptionPane.showMessageDialog( frame, "Error opening a browser. "
                      + "You can manually point a browser to :"+getEngine().getServerUri() );
                }
              }
            }
          }
        };

    return pt;
  }
}
