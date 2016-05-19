/**
 * *****************************************************************************
 * Copyright 2013 SEMOSS.ORG
 *
 * This file is part of SEMOSS.
 *
 * SEMOSS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SEMOSS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SEMOSS. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package com.ostrichemulators.semtool.util;

import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.engine.util.EngineUtil2;

import static com.ostrichemulators.semtool.util.Utility.unzip;
import java.awt.Desktop;
import java.awt.Frame;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.util.Properties;
import java.util.zip.ZipInputStream;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

/**
 * The GuiUtility class contains a variety of miscellaneous functions
 * implemented extensively throughout SEMOSS. Some of these functionalities
 * include getting concept names, printing messages, loading engines, and
 * writing Excel workbooks.
 */
public class GuiUtility {

	private static final Logger log = Logger.getLogger( GuiUtility.class );

	public static void extractHTML( String zipname, File datadir ) throws IOException {
		// check for html directory
		// extract

		// see if we've already extracted the html before doing it again
		File localHtmlPath = new File( datadir, "html" );
		if ( localHtmlPath.exists() ) {
			return;
		}

		try ( InputStream htmlIs = Utility.class.getResourceAsStream( zipname ) ) {
			unzip( new ZipInputStream( htmlIs ), localHtmlPath );
		}
	}

	/**
	 * Displays error message.
	 *
	 * @param text to be displayed.
	 */
	public static void showError( String text ) {
		JFrame playPane = DIHelper.getInstance().getPlayPane();
		JOptionPane.showMessageDialog( playPane, text, "Error",
				JOptionPane.ERROR_MESSAGE );
	}

	/**
	 * Displays option message.
	 *
	 * @param text to be displayed.
	 *
	 * @return int value of choice selected: 0 Yes, 1 No, 2 Cancel, -1 message
	 * closed
	 */
	public static int showOptionsYesNoCancel( String text ) {
		JFrame playPane = DIHelper.getInstance().getPlayPane();
		return JOptionPane.showConfirmDialog( playPane, text );
	}

	/**
	 * Displays warning message.
	 *
	 * @param text to be displayed.
	 *
	 * @return int value of choice selected: 0 Ok, 2 Cancel, -1 message closed
	 */
	public static int showWarningOkCancel( String text ) {
		JFrame playPane = DIHelper.getInstance().getPlayPane();
		return JOptionPane.showConfirmDialog( playPane, text, "Select An Option",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE );
	}

	/**
	 * Displays confirmation message.
	 *
	 * @param text to be displayed.
	 *
	 * @return int value of choice selected: 0 Ok, 2 Cancel, -1 message closed
	 */
	public static int showConfirmOkCancel( String text ) {
		JFrame playPane = DIHelper.getInstance().getPlayPane();
		return JOptionPane.showConfirmDialog( playPane, text, "Select An Option",
				JOptionPane.OK_CANCEL_OPTION );
	}

	/**
	 * Displays a message on the screen.
	 *
	 * @param text to be displayed.
	 */
	public static void showMessage( String text ) {
		JFrame playPane = DIHelper.getInstance().getPlayPane();
		JOptionPane.showMessageDialog( playPane, text );
	}

	public static void showExportMessage( Frame frame, String message, String title,
			File exportloc ) {
		if ( Desktop.isDesktopSupported() ) {
			String options[] = { "Open Location", "Close" };
			int opt = JOptionPane.showOptionDialog( frame, message, title,
					JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
					null, options, options[0] );

			if ( 0 == opt ) {
				try {
					Desktop.getDesktop().open( exportloc.getParentFile() );
				}
				catch ( Exception e ) {
					log.error( e, e );
				}
			}
		}
		else {
			JOptionPane.showMessageDialog( frame, message );
		}
	}

	/**
	 * Loads an engine - sets the core properties, loads base data engine and
	 * ontology file.
	 *
	 * @param smssfile
	 *
	 * @return Loaded engine.
	 *
	 * @throws java.io.IOException
	 */
	public static IEngine loadEngine( File smssfile ) throws IOException {
		IEngine engine = EngineUtil2.loadEngine( smssfile );
		DIHelper.getInstance().registerEngine( engine );
		return engine;
	}

	public static void closeEngine( IEngine engine ) {
		EngineUtil2.closeEngine( engine );
		DIHelper.getInstance().unregisterEngine( engine );
	}

	/**
	 * Tries to load an image by first checking the filesystem, then the jar
	 * itself. The filesystem location is &lt;CWD&gt;/pictures/&lt;filename&gt;
	 * while the jar location is jar:/images/&lt;filename&gt;
	 *
	 * @param imagename
	 *
	 * @return the image, or null if anything went wrong
	 */
	public static BufferedImage loadImage( String imagename ) {
		try {
			return ImageIO.read( new File( "pictures", imagename ) );
		}
		catch ( IOException ignored ) {
		}

		try {
			return ImageIO.read( GuiUtility.class.getResourceAsStream(
					"/images/" + imagename ) );
		}
		catch ( IOException | IllegalArgumentException ie ) {
			log.warn( "could not load file: " + imagename );
		}

		return null;
	}

	/**
	 * Loads the image, scales it to Icon size, and creates an ImageIcon to return
	 *
	 * @param imagename
	 * @return the loaded ImageIcon, or blank ImageIcon if anything went wrong
	 */
	public static ImageIcon loadImageIcon( String imagename ) {
		try {
			Image img = loadImage( imagename );
			Image newimg = img.getScaledInstance( 15, 15, java.awt.Image.SCALE_SMOOTH );
			return new ImageIcon( newimg );
		}
		catch ( Exception e ) {
			log.warn( "Error loading image icon for imagename " + imagename + ": " + e, e );
		}

		return new ImageIcon();
	}

	public static Properties getBuildProperties() {
		return Utility.getBuildProperties( GuiUtility.class );
	}
}
