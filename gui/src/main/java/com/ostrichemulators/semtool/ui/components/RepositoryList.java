/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components;

import com.ostrichemulators.semtool.ui.main.PlayPane;
import com.ostrichemulators.semtool.model.vocabulary.SEMTOOL;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.query.util.MetadataQuery;
import org.openrdf.model.URI;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import com.ostrichemulators.semtool.rdf.engine.api.MetadataConstants;
import com.ostrichemulators.semtool.rdf.engine.util.EngineManagementException;
import com.ostrichemulators.semtool.rdf.engine.util.EngineUtil;
import com.ostrichemulators.semtool.ui.actions.DbAction;
import com.ostrichemulators.semtool.ui.actions.UnmountAction;
import com.ostrichemulators.semtool.ui.main.listener.impl.RepoListMouseListener;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.GuiUtility;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.plaf.LayerUI;
import org.openrdf.model.Value;

/**
 * A List that contains {@link IEngine} instances.
 *
 * @author ryan
 */
public class RepositoryList extends JList<IEngine> {

	private static final Logger log = Logger.getLogger( RepositoryList.class );
	private final RepositoryListModel model;
	private String copiedsmss;

	public RepositoryList() {
		super( new RepositoryListModel() );
		model = RepositoryListModel.class.cast( getModel() );
		setTransferHandler();
		initActions();
	}

	public RepositoryListModel getRepositoryModel() {
		return model;
	}

	@Override
	public String getToolTipText( MouseEvent event ) {
		Point p = event.getPoint();
		int location = locationToIndex( p );
		if ( -1 == location ) {
			return null;
		}

		IEngine eng = getModel().getElementAt( location );
		if ( null == eng ) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		try {
			Map<URI, Value> metadata = eng.query( new MetadataQuery() );
			Value summary = metadata.get( MetadataConstants.DCT_DESC );
			if ( null != summary ) {
				sb.append( "Summary: " ).append( summary.stringValue() );
			}

			String loc = eng.getProperty( Constants.SMSS_LOCATION );
			if ( null != loc ) {
				if ( sb.length() > 0 ) {
					sb.append( "<br>" );
				}
				sb.append( "Location: " ).append( FilenameUtils.getFullPathNoEndSeparator( loc ) );
			}

			String onto = metadata.get(SEMTOOL.Database ).stringValue();
			if ( null != onto ) {
				if ( sb.length() > 0 ) {
					sb.append( "<br>" );
				}
				sb.append( "Base URI: " ).append( onto );
			}
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
		}

		String tooltip = "<html>" + sb.toString() + "</html>";
		return tooltip;
	}

	private void setTransferHandler() {
		setTransferHandler(new TransferHandler() {
			@Override
			public boolean canImport( TransferHandler.TransferSupport support ) {
				return support.isDataFlavorSupported( DataFlavor.javaFileListFlavor );
			}

			@Override
			public boolean importData( TransferHandler.TransferSupport support ) {
				if ( !support.isDrop() ) {
					return false;
				}

				Transferable trans = support.getTransferable();
				boolean ok = false;
				try {
					List<File> files = (List<File>) trans.getTransferData( DataFlavor.javaFileListFlavor );
					log.debug( "file drop" );
					// we handle SMSS  (and JNL) files differently from everything else,
					// so separate them from the others before doing anything

					Set<File> smsses = new HashSet<>();
					Set<File> others = new HashSet<>();
					for ( File f : files ) {
						String fname = f.getName().toLowerCase();
						if ( "smss".equals( FilenameUtils.getExtension( fname ) )
								|| "jnl".equals( FilenameUtils.getExtension( fname ) ) ) {
							smsses.add( f );
						}
						else {
							others.add( f );
						}
					}

					// do the SMSS files first
					for ( File f : smsses ) {
						log.debug( "dropping mountable file: " + f );
						try {
							EngineUtil.getInstance().mount( f.getAbsolutePath(), true );
						}
						catch ( EngineManagementException eme ) {
							GuiUtility.showError( eme.getLocalizedMessage() );
						}
						ok = true;
					}

					// for everything that isn't an smss file try to do an import
					if ( !others.isEmpty() ) {
						JList.DropLocation dl
								= JList.DropLocation.class.cast( support.getDropLocation() );
						int idx = dl.getIndex();
						if ( idx >= 0 ) {
							IEngine eng = getModel().getElementAt( idx );
							ImportExistingDbPanel.showDialog( JOptionPane.getFrameForComponent( RepositoryList.this ),
									eng, others );
							ok = true;
						}
					}
				}
				catch ( UnsupportedFlavorException | IOException e ) {
					// don't care
					log.warn( e, e );
				}

				return ok;
			}
		} );

		setDropMode( DropMode.ON );
	}

	private void initActions() {
		// add copy and paste support
		Object copyname = TransferHandler.getCopyAction().getValue( Action.NAME );
		Object pastename = TransferHandler.getPasteAction().getValue( Action.NAME );
		String detachname = "Detach";

		final Action copy = new AbstractAction() {
			@Override
			public void actionPerformed( ActionEvent ae ) {
				IEngine eng = getSelectedValue();
				copiedsmss
						= ( null == eng ? null : eng.getProperty( Constants.SMSS_LOCATION ) );
			}
		};

		Action paste = new AbstractAction() {
			@Override
			public void actionPerformed( ActionEvent ae ) {
				if ( null != copiedsmss ) {
					ProgressTask pt = new ProgressTask( "Pasting database", new Runnable() {
						@Override
						public void run() {
							try {
								EngineUtil.getInstance().clone( copiedsmss );
							}
							catch ( IOException | RepositoryException | EngineManagementException e ) {
								GuiUtility.showError( e.getMessage() );
								log.error( e, e );
							}
						}
					} );

					OperationsProgress.getInstance( PlayPane.UIPROGRESS ).add( pt );
				}
			}
		};

		Action detach = new UnmountAction( JOptionPane.getFrameForComponent( this ) ) {
			@Override
			public void actionPerformed( ActionEvent ae ) {
				IEngine eng = getSelectedValue();
				if ( null != eng ) {
					this.setEngine( eng );
					super.actionPerformed( ae );
				}
			}
		};

		getInputMap().put( KeyStroke.getKeyStroke( "ctrl pressed D" ), detachname );

		getActionMap().put( copyname, copy );
		getActionMap().put( pastename, paste );
		getActionMap().put( detachname, detach );

		addMouseListener( new RepoListMouseListener( this ) );

		addMouseListener( new MouseAdapter() {
			@Override
			public void mouseClicked( MouseEvent evt ) {
				if ( evt.getClickCount() == 2 ) {
					IEngine eng = getSelectedValue();
					if ( null != eng ) {
						DbMetadataPanel.showDialog( JOptionPane.getFrameForComponent( RepositoryList.this ), eng );
					}
				}
			}
		} );

	}

	public JComponent getNeverEmptyLayer() {
//		return this;
		LayerUI<RepositoryList> layer = new RepositoryListLayerUI();
		return new JLayer<>( this, layer );
	}

	private class RepositoryListLayerUI extends LayerUI<RepositoryList> {
	
		
		private final JLabel add = new JLabel( "Attach DB", DbAction.getIcon( "attachdb" ), SwingConstants.LEADING);
		private final JLabel create = new JLabel( "Create DB", DbAction.getIcon( "adddb" ), SwingConstants.LEADING );

		@Override
		public void paint( Graphics g, JComponent c ) {
			super.paint( g, c );

			if ( model.isEmpty() ) {
				Graphics2D g2 = Graphics2D.class.cast( g.create() );

				int h = g2.getFontMetrics().getHeight() + 5; // extra padding for the icon

				RepositoryList list = RepositoryList.this;

				Font f = list.getFont();
				add.setToolTipText("Attach a New Database");
				add.setFont( new Font( f.getName(), Font.ITALIC, f.getSize() ) );
				add.repaint();
				
				
				
				create.setFont( new Font( f.getName(), Font.ITALIC, f.getSize() ) );
				create.setToolTipText("Create a New Database");
				add.setSize( c.getWidth(), h );
				add.paint( g2 );

				create.setSize( c.getWidth(), h );
				g2.translate( 0, h );
				create.paint( g2 );

				g2.dispose();
			}
		}
	}

	public static class RepositoryListModel extends DefaultListModel<IEngine> {

		private final List<IEngine> engines = new ArrayList<>();

		@Override
		public int size() {
			return getSize();
		}

		@Override
		public int getSize() {
			return engines.size();
		}

		@Override
		public IEngine getElementAt( int index ) {
			return engines.get( index );
		}

		@Override
		public void addElement( IEngine e ) {
			add( e );
		}

		public boolean remove( IEngine e ) {
			int idx = engines.indexOf( e );
			boolean ok = engines.remove( e );
			fireIntervalRemoved( e, idx, idx );
			return ok;
		}

		public void add( IEngine e ) {
			engines.add( e );
			fireIntervalAdded( e, engines.size() - 1, engines.size() - 1 );
		}

		public void addAll( Collection<IEngine> engins ) {
			for ( IEngine eng : engins ) {
				add( eng );
			}
		}

		public Collection<IEngine> getElements() {
			return new ArrayList<>( engines );
		}

		@Override
		public boolean isEmpty() {
			return engines.isEmpty();
		}
	}
}
