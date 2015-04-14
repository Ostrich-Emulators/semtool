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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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

	public static void doGraphExportWithDialogue(Component component) {
		Map<String,Object> returnHash = getExportFileLocationAndExportType(component);
		if (returnHash == null) {
			return;
		}
		
		ExportType exportType = (ExportType) returnHash.get("exportType");
		File file = (File) returnHash.get("fileLocation");
		String fileLocation = file.getAbsolutePath();
		
		try {
			if ( exportType.equals( ExportType.PNG ) ){
				writePNG(component, fileLocation);
			} else if ( exportType.equals( ExportType.PDF ) ) {
				writePDF(component, fileLocation);
			}

			Utility.showExportMessage( JOptionPane.getFrameForComponent( component ), 
					"Export successful: " + fileLocation, "Export Successful", file );
		}
		catch ( IOException | DocumentException e ) {
			Utility.showError( "Graph export failed." );
			log.error( e, e );
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

	private static Map<String,Object> getExportFileLocationAndExportType(Component component) {
		try {
			String lastDirUsedKey = "lastgraphexportdir";
	
			Preferences prefs = Preferences.userNodeForPackage( ExportUtility.class );
			File loc = FileBrowsePanel.getLocationForEmptyPref( prefs, lastDirUsedKey );
	
			String p = prefs.get( lastDirUsedKey, loc.getAbsolutePath() );
			JFileChooser chooser = new JFileChooser( p );
			chooser.setControlButtonsAreShown( false );
			chooser.addChoosableFileFilter( new FileBrowsePanel.CustomFileFilter( "PNG image", "png" ) );
			chooser.addChoosableFileFilter( new FileBrowsePanel.CustomFileFilter( "PDF document", "pdf" ) );
			
			//Display dialog to choose export quality
			Object[] options = { "PDF document", "PNG image", "Cancel" };
			int n = JOptionPane.showOptionDialog( JOptionPane.getFrameForComponent( component ),
					chooser,
					"Export", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0] );
			String suffix = "";
			
			ExportType exportType = ExportType.PNG;
			switch ( n ) {
				case -1:
					return null;
				case 0:
					exportType = ExportType.PDF;
					suffix = ".pdf";
					break;
				case 1:
					exportType = ExportType.PNG;
					suffix = ".png";
					break;
				case 2:
					return null;
			}
			
	
			File file = chooser.getSelectedFile();
			if ( null == file ) {
				file = new File( chooser.getCurrentDirectory(),
						new SimpleDateFormat( "'Graph_Export_'MMM_dd_yyyy_HHmm" ).format( new Date() ) );
			}
	
			prefs.put( lastDirUsedKey, file.getParent() );
			String fileLocation = file.getAbsolutePath();

			if ( !( fileLocation.endsWith(".png") || fileLocation.endsWith(".pdf") ) ) {
				file = new File(fileLocation + suffix);
			}
			
			HashMap<String,Object> returnHash = new HashMap<String,Object>();
			returnHash.put("exportType", exportType);
			returnHash.put("fileLocation", file);
			return returnHash;
		}
		catch ( Exception e ) {
			Utility.showError( "Export failed." );
			log.error( e, e );
			return null;
		}
	}

	@SuppressWarnings("unused")
	private static void exportImage( VisualizationViewer<SEMOSSVertex, SEMOSSEdge> viewer, ControlData cd,
			File exportfile, ExportType exptype ) throws IOException { 
		Layout<SEMOSSVertex, SEMOSSEdge> gl = viewer.getGraphLayout();
		Dimension gls = gl.getSize();

		// make the dimension a little bigger, just for fun
		Dimension canvasSize = new Dimension();
		canvasSize.setSize( gls.getWidth(), gls.getHeight() );
		Dimension imgSize = new Dimension();
		imgSize.setSize( gls.getWidth() * 1.2, gls.getHeight() * 1.2 );

		VisualizationImageServer<SEMOSSVertex, SEMOSSEdge> vis
				= new VisualizationImageServer<>( gl, canvasSize );

		vis.setBackground( viewer.getBackground() );
		GraphPlaySheet.initVvRenderer( vis.getRenderContext(), cd );
		vis.getRenderer().getVertexLabelRenderer().setPosition( Renderer.VertexLabel.Position.CNTR );

		BufferedImage image
				= (BufferedImage) vis.getImage( new Point2D.Double( imgSize.getWidth() / 2,
								imgSize.getHeight() / 2 ), imgSize );

		try ( OutputStream os = new FileOutputStream( exportfile ) ) {
			switch ( exptype ) {
				case PNG:
					ImageIO.write( image, "png", os );
					break;
				case EPS:
					Graphics2D g = new EpsGraphics2D( "Graph Export", os, 0, 0,
							canvasSize.width, canvasSize.height );
					g.drawImage( image, 0, 0, null );
					break;
				case PDF:
					try {
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						ImageIO.write( image, "png", baos );

						Document doc = new Document();
						PdfWriter.getInstance( doc, os );
						doc.open();
						doc.add( com.itextpdf.text.Image.getInstance( baos.toByteArray() ) );
						doc.close();
					}
					catch ( DocumentException de ) {
						log.error( de, de );
					}
					break;
			}
		}
	}
}
