/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.apache.log4j.Logger;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.Utility;
import gov.va.semoss.rdf.engine.util.EngineManagementException;
import gov.va.semoss.rdf.engine.util.EngineManagementException.ErrorCode;
import gov.va.semoss.rdf.engine.util.EngineUtil;
import gov.va.semoss.ui.components.FileBrowsePanel;
import gov.va.semoss.ui.components.ProgressTask;
import gov.va.semoss.ui.components.SemossFileView;

/**
 *
 * @author ryan
 */
public class MountAction extends DbAction {

  private static final Logger log = Logger.getLogger( MountAction.class );
  private Frame frame;
  private File smssfile;

  public MountAction( String optg, Frame frame ) {
    super( optg, MOUNT, "attachdb" );
    putValue( AbstractAction.SHORT_DESCRIPTION, "Mount an existing database" );
  }

  @Override
  protected boolean preAction( ActionEvent e ) {
    Preferences prefs = Preferences.userNodeForPackage( MountAction.class );
    JFileChooser chsr = new JFileChooser( prefs.get( "lastmountloc", "." ) );
    chsr.setDialogTitle( "Select Directory" );
    chsr.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );

    // we want to hide smss files that are already loaded
    final Set<File> loaded = new HashSet<>();
    for ( IEngine engo : DIHelper.getInstance().getEngineMap().values() ) {
      loaded.add( new File( engo.getProperty( Constants.SMSS_LOCATION ) ) );
    }
		
		chsr.setFileFilter( FileBrowsePanel.getDatabaseFilter( loaded ) );

    chsr.setApproveButtonText( "Attach" );
    chsr.setFileView(new SemossFileView() );
    int rslt = chsr.showOpenDialog( frame );
		if( JFileChooser.APPROVE_OPTION == rslt ){
			smssfile = chsr.getSelectedFile();

			if ( null != smssfile ) {
				prefs.put( "lastmountloc", smssfile.getParent() );
				return true;
			}
		}
    return false;
  }

  @Override
  protected ProgressTask getTask( ActionEvent e ) {
    ProgressTask pt = new ProgressTask( "Mounting " + smssfile,
        new Runnable() {
          @Override
          public void run() {
            try {
              EngineUtil.getInstance().mount( smssfile, true );
            }
            catch ( EngineManagementException eme ) {
              String msg = ( ErrorCode.DUPLICATE_NAME == eme.getCode()
                  ? "A repository with this name is already open. Please choose another."
                  : eme.getLocalizedMessage() );
              Utility.showError( msg );
              log.error( eme );
            }
          }
        } );

    return pt;
  }
}
