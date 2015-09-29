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
package gov.va.semoss.util;

import gov.va.semoss.ui.components.FileBrowsePanel;
import gov.va.semoss.ui.components.api.IPlaySheet;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import org.apache.commons.io.FileUtils;

/**
 * The GuiUtility class contains functions for use with exporting images.
 */
public class ExportUtility {

	private static final Logger log = Logger.getLogger( ExportUtility.class );

	public static void doExportCSVWithDialogue( Component component, String data ) {
		File file = getExportFileLocation( component, ".csv" );
		if ( null == file ) {
			return;
		}

		try {
			FileUtils.write( file, data );
			GuiUtility.showExportMessage( JOptionPane.getFrameForComponent( component ),
					"Export successful: " + file.getAbsolutePath(), "Export Successful", file );
		}
		catch ( IOException e ) {
			log.error( e, e );
			GuiUtility.showError( e.getLocalizedMessage() );
		}
	}

	public static File getExportFileLocation( Component component, String suffix ) {
		try {
			String lastDirUsedKey = "lastgraphexportdir";

			Preferences prefs = Preferences.userNodeForPackage( ExportUtility.class );
			File loc = FileBrowsePanel.getLocationForEmptyPref( prefs, lastDirUsedKey );

			String p = prefs.get( lastDirUsedKey, loc.getAbsolutePath() );

			JFileChooser fileChooser = new JFileChooser( p );
			fileChooser.setDialogTitle( "Specify a " + suffix + " file to save" );
			fileChooser.setSelectedFile( getSuggestedFilename( component, suffix ) );

			int userSelection = fileChooser.showSaveDialog( JOptionPane.getFrameForComponent( component ) );
			if ( userSelection != JFileChooser.APPROVE_OPTION ) {
				return null;
			}

			File file = fileChooser.getSelectedFile();
			prefs.put( lastDirUsedKey, file.getParent() );

			String fileLocation = file.getAbsolutePath();
			if ( !fileLocation.toUpperCase().endsWith( suffix.toUpperCase() ) ) {
				file = new File( fileLocation + suffix );
			}

			return file;
		}
		catch ( Exception e ) {
			GuiUtility.showError( "Export failed." );
			log.error( e, e );
			return null;
		}
	}

	public static File getSuggestedFilename( String title, String suffix ) {
		try {
			title = title.replaceAll( "[^A-Za-z0-9 ()]", "" );
			if ( title.length() > 100 ) {
				title = title.substring( 0, 100 );
			}

			return new File( title + suffix );
		}
		catch ( Exception e ) {
			log.warn( "Couldn't create a suggested filename from title: " + title + e, e );
			return new File( "SemossExport" + suffix );
		}		
	}

	public static File getSuggestedFilename( Component component, String suffix ) {
		return getSuggestedFilename( ((IPlaySheet) component ).getTitle(), suffix );
	}

	public static void exportAsPdf( BufferedImage img, File pdf )
			throws IOException, DocumentException {
		final double MAX_DIM = 14400;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write( img, "PNG", baos );
		Image image1 = Image.getInstance( baos.toByteArray(), true );
		Rectangle r;
		if ( image1.getHeight() > MAX_DIM ) {
			r = new Rectangle( (int) image1.getWidth(), (int) MAX_DIM );
		}
		else if ( image1.getWidth() > MAX_DIM ) {
			r = new Rectangle( (int) MAX_DIM, (int) image1.getHeight() );
		}
		else {
			r = new Rectangle( (int) image1.getWidth() + 20, (int) image1.getHeight() + 20 );
		}
		Document document = new Document( r, 15, 25, 15, 25 );
		PdfWriter.getInstance( document, new FileOutputStream( pdf ) );
		document.open();

		int pages = (int) Math.ceil( (double) img.getHeight() / MAX_DIM );
		if ( pages == 0 ) {
			pages = 1;
		}
		for ( int i = 0; i < pages; i++ ) {
			BufferedImage temp;
			if ( i < pages - 1 ) {
				temp = img.getSubimage( 0, i * (int) MAX_DIM, img.getWidth(), (int) MAX_DIM );
			}
			else {
				temp = img.getSubimage( 0, i * (int) MAX_DIM, img.getWidth(), img.getHeight() % (int) MAX_DIM );
			}
			File tempFile = new File( i + Constants.PNG );
			ImageIO.write( temp, Constants.PNG, tempFile );
			Image croppedImage = Image.getInstance( i + Constants.PNG );
			document.add( croppedImage );
			tempFile.delete();
			if ( i < pages - 1 ) {
				document.newPage();
			}
		}

		document.close();
	}
}
