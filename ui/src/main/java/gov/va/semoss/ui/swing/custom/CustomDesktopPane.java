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
package gov.va.semoss.ui.swing.custom;

import gov.va.semoss.ui.actions.OpenAction;
import gov.va.semoss.ui.components.OperationsProgress;
import gov.va.semoss.ui.components.PlayPane;
import gov.va.semoss.ui.components.PlaySheetFrame;
import gov.va.semoss.ui.components.ProgressTask;
import gov.va.semoss.ui.main.listener.impl.PlaySheetFrameToolBarListener;
import gov.va.semoss.util.DIHelper;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JDesktopPane;
import org.apache.log4j.Logger;

import gov.va.semoss.util.GuiUtility;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JInternalFrame;
import javax.swing.JToolBar;
import javax.swing.event.InternalFrameListener;

/**
 * This class extends JDesktopPane in order to create a custom desktop pane.
 */
public class CustomDesktopPane extends JDesktopPane {

	public static final int CASCADE_STEPSIZE = 25;
	private static final Logger log = Logger.getLogger( CustomDesktopPane.class );

	// check to see if our image is in the jar, and if not, check the filesystem
	private final BufferedImage img;
	private final PlaySheetFrameToolBarListener tblistener;

	private final List<InternalFrameListener> framelisteners = new ArrayList<>();

	public CustomDesktopPane() {
		this( null );
	}

	public CustomDesktopPane( JToolBar tb ) {
		img = GuiUtility.loadImage( "desktop.png" );
		if ( null == img ) {
			log.error( "could not load desktop image" );
		}
		tblistener = new PlaySheetFrameToolBarListener( tb );

		DropTarget dt = new DropTarget( this, DnDConstants.ACTION_COPY_OR_MOVE,
				new DesktopDropListener(), true );
		dt.setActive( true );
		setDropTarget( dt );
	}

	/**
	 * Registers a frame listener to add to all frames added to this desktop
	 *
	 * @param ifl
	 */
	public void registerFrameListener( InternalFrameListener ifl ) {
		framelisteners.add( ifl );
	}

	/**
	 * Unregisters a frame listener to add to all frames added to this desktop
	 *
	 * @param ifl
	 */
	public void unregisterFrameListener( InternalFrameListener ifl ) {
		framelisteners.remove( ifl );
	}

	/**
	 * Paints the desktop pane.
	 *
	 * @param g Graphics	Graphic to be displayed.
	 */
	@Override
	protected void paintComponent( Graphics g ) {
		super.paintComponent( g );
		if ( img != null ) {
			g.drawImage( img, 0, 0, this.getWidth(), this.getHeight(), this );
		}
		else {
			g.drawString( "Image not found", 50, 50 );
		}
	}

	@Override
	public Component add( Component toadd ) {
		Component frames[] = getAllFrames();
		// we want to stagger the top left corner of the window
		int topleft = CASCADE_STEPSIZE * ( null == frames ? 0 : frames.length );
		Component cmp = super.add( toadd );

		if ( toadd instanceof JInternalFrame ) {
			JInternalFrame jif = JInternalFrame.class.cast( toadd );

			for ( InternalFrameListener ifl : framelisteners ) {
				jif.addInternalFrameListener( ifl );
			}

			if ( null != tblistener ) {
				jif.addInternalFrameListener( tblistener );
				jif.pack();

				Dimension paneSize = getSize();
				toadd.setSize( paneSize.width - topleft, paneSize.height - topleft );
				toadd.setLocation( topleft, topleft );

				if ( jif instanceof PlaySheetFrame ) {
					PlaySheetFrame psf = PlaySheetFrame.class.cast(  jif );
					
					psf.addChangeListener( tblistener );
					
					psf.addedToDesktop( this );
				}

				toadd.setVisible( true );
			}
		}

		return cmp;
	}

	private class DesktopDropListener extends DropTargetAdapter {

		@Override
		public void drop( DropTargetDropEvent dtde ) {
			if ( !dtde.isDataFlavorSupported( DataFlavor.javaFileListFlavor ) ) {
				dtde.rejectDrop();
				return;
			}
			dtde.acceptDrop( dtde.getDropAction() );

			Transferable trans = dtde.getTransferable();
			List<File> files;
			try {
				files = (List<File>) trans.getTransferData( DataFlavor.javaFileListFlavor );
			}
			catch ( UnsupportedFlavorException | IOException e ) {
				log.error( e );
				dtde.dropComplete( false );
				return;
			}

			log.debug( "file drop" );

			ProgressTask pt = OpenAction.openFiles( DIHelper.getInstance().getDesktop(),
					files, null );
			OperationsProgress.getInstance( PlayPane.UIPROGRESS ).add( pt );

			dtde.dropComplete( true );
		}

		@Override
		public void dragEnter( DropTargetDragEvent dtde ) {
			if ( dtde.isDataFlavorSupported( DataFlavor.javaFileListFlavor ) ) {
				dtde.acceptDrag( dtde.getDropAction() );
			}
			else {
				dtde.rejectDrag();
			}
		}

		@Override
		public void dragOver( DropTargetDragEvent dtde ) {
			if ( dtde.isDataFlavorSupported( DataFlavor.javaFileListFlavor ) ) {
				dtde.acceptDrag( dtde.getDropAction() );
			}
			else {
				dtde.rejectDrag();
			}
		}

		@Override
		public void dropActionChanged( DropTargetDragEvent dtde ) {
			if ( dtde.isDataFlavorSupported( DataFlavor.javaFileListFlavor ) ) {
				dtde.acceptDrag( dtde.getDropAction() );
			}
			else {
				dtde.rejectDrag();
			}
		}
	}
}
