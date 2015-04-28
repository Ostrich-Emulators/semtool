/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.actions;

import gov.va.semoss.ui.components.FileBrowsePanel;
import gov.va.semoss.util.DefaultIcons;
import gov.va.semoss.util.Utility;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

/**
 * An action to handle saving stuff. This can be used as a base for either
 * "Save" or "Save As" actions, or really, any action that should open a file
 * chooser. This class handles remembering the "last save" location
 *
 * @author ryan
 */
public abstract class AbstractSavingAction extends AbstractAction {

	private static final Logger log = Logger.getLogger( AbstractSavingAction.class );
	private final boolean saveas;
	private final Preferences prefs;
	private final String prefkey;
	private boolean appendDate = false;
	private String defaultFileName = "";
	private File lastsave = null;

	public AbstractSavingAction( String name, Icon icon, boolean issaveas,
			Preferences prefs, String prefkey ) {
		super( name, icon );
		saveas = issaveas;
		this.prefs = prefs;
		this.prefkey = prefkey;
	}

	public AbstractSavingAction( String name, Icon icon, boolean issaveas ) {
		this( name, icon, issaveas,
				Preferences.userNodeForPackage( AbstractSavingAction.class ), "lastsavedir" );
	}

	public AbstractSavingAction( String name ) {
		this( name, DefaultIcons.defaultIcons.get( DefaultIcons.SAVE ), false,
				Preferences.userNodeForPackage( AbstractSavingAction.class ), "lastsavedir" );
	}

	public AbstractSavingAction( String name, boolean issaveas ) {
		this( name, DefaultIcons.defaultIcons.get( DefaultIcons.SAVE ), issaveas,
				Preferences.userNodeForPackage( AbstractSavingAction.class ), "lastsavedir" );
	}

	public void setToolTip( String tt ) {
		this.putValue( Action.SHORT_DESCRIPTION, tt );
	}

	public void setAppendDate( boolean b ) {
		appendDate = b;
	}

	public void setDefaultFileName( String defaultFileName ) {
		this.defaultFileName = defaultFileName;
	}

	protected String getSuccessMessage( File expfile ) {
		return "Saved to " + expfile;
	}

	protected String getSuccessTitle() {
		return "Save Success";
	}

	protected String getFailMessage( File expfile, Exception e ) {
		return "Save Failed: " + e.getLocalizedMessage();
	}

	@Override
	public void actionPerformed( ActionEvent e ) {
		if ( saveas || null == lastsave ) {
			File dir = FileBrowsePanel.getLocationForEmptyPref( prefs, prefkey );
			JFileChooser fileChooser = new JFileChooser( dir );

			StringBuilder sb = new StringBuilder( defaultFileName );
			if ( appendDate ) {
				SimpleDateFormat sdf = new SimpleDateFormat( "_MM dd, yyyy HHmm" );
				sb.append( sdf.format( new Date() ) );
			}

			fileChooser.setSelectedFile( new File( sb.toString() ) );
			fileChooser.setAcceptAllFileFilterUsed( false );
			finishFileChooser( fileChooser );

			int returnVal = fileChooser.showSaveDialog( null );
			if ( returnVal == JFileChooser.APPROVE_OPTION ) {
				lastsave = fileChooser.getSelectedFile();
				prefs.put( prefkey, lastsave.getParent() );

				//Make sure that the file name has the selected extension:
				String fileName = lastsave.getAbsolutePath();
				if ( FilenameUtils.getExtension( fileName ).isEmpty() ) {
					String extension = "." + ( (FileNameExtensionFilter) fileChooser.getFileFilter() ).getExtensions()[0];
					if ( !fileName.endsWith( extension ) ) {
						lastsave = new File( fileName + extension );
					}
				}
			}
		}

		if ( null != lastsave ) {
			if ( lastsave.exists() ) {
				int rslt = JOptionPane.showConfirmDialog( null,
						"File exists. Overwrite?", "Overwrite?", JOptionPane.YES_NO_OPTION );
				if ( rslt != JOptionPane.YES_OPTION ) {
					return;
				}
			}

			try {
				saveTo( lastsave );
				Utility.showExportMessage( null, getSuccessMessage( lastsave ),
						getSuccessTitle(), lastsave );
			}
			catch ( IOException ex ) {
				log.error( ex, ex );
				Utility.showError( getFailMessage( lastsave, ex ) );
				lastsave = null;
			}
		}
	}

	/**
	 * Completes the setup for a FileChooser before presenting it to the user. The
	 * {@link #actionPerformed(java.awt.event.ActionEvent) } function sets up the
	 * default file and directory, but subclases might want something more
	 * specific. By default, does nothing
	 *
	 * @param chsr the chooser to setup
	 */
	protected void finishFileChooser( JFileChooser chsr ) {
	}

	/**
	 * Performs the action save logic
	 *
	 * @param exploc the file to save to
	 * @throws java.io.IOException
	 */
	protected abstract void saveTo( File exploc ) throws IOException;

}
