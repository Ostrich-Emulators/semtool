/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.playsheets;

import com.itextpdf.text.DocumentException;

import com.ostrichemulators.semtool.ui.actions.DbAction;
import com.ostrichemulators.semtool.ui.components.OperationsProgress;
import com.ostrichemulators.semtool.ui.components.PlayPane;
import com.ostrichemulators.semtool.ui.components.ProgressTask;
import com.ostrichemulators.semtool.util.ExportUtility;
import static com.ostrichemulators.semtool.util.ExportUtility.getExportFileLocation;

import com.ostrichemulators.semtool.util.GuiUtility;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public abstract class ImageExportingPlaySheet extends PlaySheetCentralComponent {

	private static final Logger log = Logger.getLogger( ImageExportingPlaySheet.class );
	private static final String PDF = "Export PDF";
	private static final String PNG = "Export PNG";

	private final AbstractAction pdfer = new ExportAction( PDF, "save_pdf" );
	private final AbstractAction pnger = new ExportAction( PNG, "save_png" );

	protected abstract BufferedImage getExportImage() throws IOException;

	@Override
	public void populateToolBar( JToolBar toolBar, final String tabTitle ) {
		toolBar.add( pdfer );
		toolBar.add( pnger );
	}

	private class ExportAction extends AbstractAction {

		public ExportAction( String name, String iconloc ) {
			super( name, DbAction.getIcon( iconloc ) );
			putValue( Action.SHORT_DESCRIPTION, name );
		}

		@Override
		public void actionPerformed( ActionEvent ae ) {
			boolean ispdf = PDF.equals( getValue( Action.SHORT_DESCRIPTION ) );
			String sfx = ( ispdf ? ".pdf" : ".png" );
			File output = getExportFileLocation( ImageExportingPlaySheet.this, sfx );
			if( null == output ){
				return;
			}
			Exception exceptions[] = { null };

			ProgressTask pt = new ProgressTask( "Exporting " + getTitle(), new Runnable() {

				@Override
				public void run() {
					try {
						BufferedImage img = getExportImage();
						if( null == img ){
							throw new IOException( "An unknown error occurred" );
						}
						if ( ispdf ) {
							ExportUtility.exportAsPdf( img, output );
						}
						else {
							ImageIO.write( img, "PNG", output );
						}
					}
					catch ( IOException | DocumentException e ) {
						log.error( e, e );
						exceptions[0] = e;
					}
				}
			} ) {

				@Override
				public void done() {
					super.done();

					if ( null == exceptions[0] ) {
						GuiUtility.showExportMessage(
								JOptionPane.getFrameForComponent( ImageExportingPlaySheet.this ),
								"Export successful: " + output.getAbsolutePath(),
								"Export Successful", output );
					}
					else {
						log.error( exceptions[0], exceptions[0] );
					}
				}
			};

			OperationsProgress.getInstance( PlayPane.UIPROGRESS ).add( pt );
		}
	}
}
