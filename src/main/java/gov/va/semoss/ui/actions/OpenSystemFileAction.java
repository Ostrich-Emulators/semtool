package gov.va.semoss.ui.actions;

import gov.va.semoss.util.Utility;
import info.aduna.io.IOUtil;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import javax.swing.Icon;

/**
 * Action to save a Grid or Raw Grid to a CSV or tab-delimited text file, or to
 * Excel.
 *
 * @author Thomas
 *
 */
public class OpenSystemFileAction extends AbstractAction {

	private static final Logger log = Logger.getLogger( OpenSystemFileAction.class );
	private final String filePath;
	private static boolean firstrun = true;

	/**
	 * Creates an Action with the given key, short description, and image
	 *
	 * @param text the action command
	 * @param description the short description
	 * @param filePath the url to open when selected
	 * @param imagePart the image name locator
	 */
	public OpenSystemFileAction( String text, String description, String filePath,
			String imagePart ) {
		super( text, getIcon( imagePart ) );

		putValue( AbstractAction.SHORT_DESCRIPTION, description );
		putValue( AbstractAction.MNEMONIC_KEY, KeyEvent.VK_M );
		this.filePath = filePath;

		setEnabled( true );
	}

	public static OpenSystemFileAction getHelpManual() {
		try {
			return new OpenSystemFileAction( "V-CAMP SEMOSS User Manual",
					"Opens Local VCAMP SEMOSS User Manual",
					"/help/V-CAMP SEMOSS Tool User Manual.pdf",
					"helpbook" );

		}
		catch ( Exception e ) {
			log.error( e, e );
			Utility.showError( e.getMessage() );
			throw new IllegalArgumentException( "Could not find manual" );
		}
	}

	public void openFileFromJar( String fileName ) {
		try {
			File cachedir = new File( defaultDirectory(),
					System.getProperty( "release.name", "SEMOSS" ) );
			File outputFile = new File( cachedir, fileName );

			if ( firstrun ) {
				// clear out the cache the first time we run (per app start)
				firstrun = false;
				if ( cachedir.exists() ) {
					cachedir.delete();
				}
			}

			if ( !outputFile.exists() ) {
				cachedir.mkdirs();
				IOUtil.writeStream( getClass().getResourceAsStream( fileName ), outputFile );
			}

			Desktop.getDesktop().open( outputFile );

		}
		catch ( Exception e ) {
			log.error( e, e );
		}
	}

	private String defaultDirectory() {
		String OS = System.getProperty( "os.name" ).toUpperCase();
		if ( OS.contains( "WIN" ) ) {
			return System.getenv( "APPDATA" );
		}
		else if ( OS.contains( "MAC" ) ) {
			return System.getProperty( "user.home" ) + "/Library/Application "
					+ "Support";
		}
		else if ( OS.contains( "NUX" ) ) {
			return System.getProperty( "user.home" );
		}
		return System.getProperty( "user.dir" );
	}

	@Override
	public void actionPerformed( ActionEvent av ) {
		openFileFromJar( filePath );
	}

	public static Icon getIcon( String middle ) {
		return new ImageIcon( OpenSystemFileAction.class.getResource( "/images/icons16/"
				+ middle + "_16.png" ) );
	}
}
