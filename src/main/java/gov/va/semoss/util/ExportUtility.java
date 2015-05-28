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

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.ui.components.ControlData;
import gov.va.semoss.ui.components.FileBrowsePanel;
import gov.va.semoss.ui.components.api.IPlaySheet;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.sourceforge.jlibeps.epsgraphics.EpsGraphics2D;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * The Utility class contains functions for use with exporting images.
 */
public class ExportUtility {
	private static final Logger log = Logger.getLogger( ExportUtility.class );
	public static enum ExportType {
		EPS, PNG, PDF
	};

	public static void doGraphExportPDFWithDialogue(Component component) {
		doGraphExportWithDialogue(component, ExportType.PDF, ".pdf");
	}
	
	public static void doGraphExportPNGWithDialogue(Component component) {
		doGraphExportWithDialogue(component, ExportType.PNG, ".png");
	}
	
	private static void doGraphExportWithDialogue(Component component, ExportType exportType, String suffix) {
		File file = getExportFileLocation(component, suffix);
		if (file == null) {
			return;
		}

		try {
			if (exportType == ExportType.PNG)
				writePNG(component, file.getAbsolutePath());
			else if (exportType == ExportType.PDF)
				writePDF(component, file.getAbsolutePath());			
			
			Utility.showExportMessage( JOptionPane.getFrameForComponent( component ), 
					"Export successful: " + file.getAbsolutePath(), "Export Successful", file );
		}
		catch ( IOException | DocumentException e ) {
			Utility.showError( "Graph export to " + suffix + " failed." );
			log.error( e, e );
		}
	}
	
	private static File getExportFileLocation(Component component, String suffix) {
		try {
			String lastDirUsedKey = "lastgraphexportdir";
			
			Preferences prefs = Preferences.userNodeForPackage( ExportUtility.class );
			File loc = FileBrowsePanel.getLocationForEmptyPref( prefs, lastDirUsedKey );

			String p = prefs.get( lastDirUsedKey, loc.getAbsolutePath() );

			JFileChooser fileChooser = new JFileChooser( p );
			fileChooser.setDialogTitle("Specify a " + suffix + " file to save");
			fileChooser.setSelectedFile( getSuggestedFilename(component, suffix) );
			
			int userSelection = fileChooser.showSaveDialog( JOptionPane.getFrameForComponent( component ) );
			if (userSelection != JFileChooser.APPROVE_OPTION) {
				return null;
			}
			
			File file = fileChooser.getSelectedFile();			
			prefs.put( lastDirUsedKey, file.getParent() );
			
			String fileLocation = file.getAbsolutePath();
			if ( !fileLocation.toUpperCase().endsWith(suffix.toUpperCase()) ) {
				file = new File(fileLocation + suffix);
			}
			
			return file;
		}
		catch ( Exception e ) {
			Utility.showError( "Export failed." );
			log.error( e, e );
			return null;
		}
	}

	private static File getSuggestedFilename(Component component, String suffix) {
		try {
			String title = ((IPlaySheet) component).getTitle();
			title = title.replaceAll("[^A-Za-z0-9 ()]", "");
			if (title.length() > 100)
				title = title.substring(0, 100);

			return new File(title + suffix);
		} catch (Exception e) {
			log.debug("Couldn't create a suggested filename from component: " + component + "\n" + e, e);
			return new File("SemossExport" + suffix);
		}
	}

	private static void writePNG(Component component, String fileLocation) throws IOException{
		BufferedImage bufferedImage = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_ARGB);
		component.paint(bufferedImage.getGraphics());
		ImageIO.write(bufferedImage, Constants.PNG, new File(fileLocation));
	}

	private static void writePDF(Component component, String fileLocation) throws IOException, DocumentException {
		final double MAX_DIM = 14400;

		BufferedImage bufferedImage = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_ARGB);
		component.paint(bufferedImage.getGraphics());
		ImageIO.write(bufferedImage, Constants.PNG, new File(fileLocation));
		
		com.itextpdf.text.Image image1 = com.itextpdf.text.Image.getInstance( fileLocation );
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
		PdfWriter.getInstance( document, new FileOutputStream( fileLocation ) );
		document.open();

		int pages = (int) Math.ceil( (double) bufferedImage.getHeight() / MAX_DIM );
		if ( pages == 0 ) {
			pages = 1;
		}
		for ( int i = 0; i < pages; i++ ) {
			BufferedImage temp;
			if ( i < pages - 1 ) {
				temp = bufferedImage.getSubimage( 0, i * (int) MAX_DIM, bufferedImage.getWidth(), (int) MAX_DIM );
			}
			else {
				temp = bufferedImage.getSubimage( 0, i * (int) MAX_DIM, bufferedImage.getWidth(), bufferedImage.getHeight() % (int) MAX_DIM );
			}
			File tempFile = new File( i + Constants.PNG );
			ImageIO.write( temp, Constants.PNG, tempFile );
			com.itextpdf.text.Image croppedImage = com.itextpdf.text.Image.getInstance( i + Constants.PNG );
			document.add( croppedImage );
			tempFile.delete();

			if ( i < pages - 1 ) {
				document.newPage();
			}
		}
		
		document.close();
	}
}
