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
package com.ostrichemulators.semtool.ui.components.playsheets;

import com.ostrichemulators.semtool.om.SEMOSSVertex;
import com.ostrichemulators.semtool.om.SEMOSSVertexImpl;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.query.util.impl.ModelQueryAdapter;
import com.ostrichemulators.semtool.ui.actions.DbAction;
import com.ostrichemulators.semtool.ui.actions.SaveAllGridAction;
import com.ostrichemulators.semtool.ui.actions.SaveAsGridAction;
import com.ostrichemulators.semtool.ui.actions.SaveGridAction;
import com.ostrichemulators.semtool.ui.components.LineNumberTableRowHeader;
import com.ostrichemulators.semtool.ui.components.PlaySheetFrame;
import com.ostrichemulators.semtool.ui.components.models.ValueTableModel;
import com.ostrichemulators.semtool.ui.components.renderers.URIEditor;
import com.ostrichemulators.semtool.util.DIHelper;
import com.ostrichemulators.semtool.util.MultiMap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.RepositoryException;

/**
 */
public class GridRAWPlaySheet extends PlaySheetCentralComponent {

	private static final long serialVersionUID = -8685007953734205297L;
	private static final Logger log = Logger.getLogger( GridPlaySheet.class );
	private final ValueTableModel model;
	private final JTable table;
	private final SaveGridAction save = new SaveGridAction( false );
	private final SaveAsGridAction saveas = new SaveAsGridAction( true );
	private final SaveAllGridAction saveall = new SaveAllGridAction();
	private final JLabel searchlabel = new JLabel( "Search" );
	private final JTextField searchfield = new JTextField();
	private final HighlightingRenderer renderer = new HighlightingRenderer();

	public GridRAWPlaySheet() {
		this( new ValueTableModel() );
	}

	public GridRAWPlaySheet( ValueTableModel mod ) {
		model = mod;
		table = new JTable( model );
		addMouseListener();
		setLayout( new BorderLayout() );

		final JScrollPane jsp = new JScrollPane( table );
		table.setAutoCreateRowSorter( true );
		table.setDefaultEditor( IRI.class, new URIEditor() );

		table.getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW ).
				put( KeyStroke.getKeyStroke( KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK ),
						"saveGridAction" );
		table.getInputMap( JComponent.WHEN_FOCUSED ).
				put( KeyStroke.getKeyStroke( KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK ),
						"saveGridAction" );
		table.getActionMap().put( "saveGridAction", save );

		table.setCellSelectionEnabled( true );

		jsp.setAutoscrolls( true );

		LineNumberTableRowHeader tableLineNumber
				= new LineNumberTableRowHeader( jsp, table );
		JTableHeader header = table.getTableHeader();
		tableLineNumber.setBackground( header.getBackground() );
		jsp.setRowHeaderView( tableLineNumber );

		add( jsp, BorderLayout.CENTER );

		JLabel btn = new JLabel( DbAction.getIcon( "selectall" ), JLabel.TRAILING );
		btn.setBorder( new EmptyBorder( 0, 0, 0, 5 ) );

		jsp.setCorner( JScrollPane.UPPER_LEFT_CORNER, btn );
		btn.setToolTipText( "Select All" );
		btn.addMouseListener( new MouseAdapter() {

			@Override
			public void mouseClicked( MouseEvent e ) {
				table.selectAll();
				ActionEvent ae
						= new ActionEvent( table, ActionEvent.ACTION_PERFORMED, "copy" );
				table.getActionMap().get( "copy" ).actionPerformed( ae );
			}
		} );

		model.addPropertyChangeListener( ValueTableModel.NEEDS_SAVE,
				new PropertyChangeListener() {

					@Override
					public void propertyChange( PropertyChangeEvent evt ) {
						boolean show = Boolean.parseBoolean( evt.getNewValue().toString() );
						GridRAWPlaySheet.this.getPlaySheetFrame().showSaveMnemonic( show );
					}
				} );

		searchfield.setPreferredSize( new Dimension( 50, 20 ) );
		Border b1 = BorderFactory.createLineBorder( Color.DARK_GRAY, 1 );
		Border b2 = BorderFactory.createEmptyBorder( 0, 3, 0, 3 );
		searchfield.setBorder( BorderFactory.createCompoundBorder( b1, b2 ) );

		searchfield.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed( ActionEvent e ) {
				search( searchfield.getText() );
			}
		} );
	}

	protected void setupToolBarButtons( final String tabTitle ) {
		save.setDefaultFileName( tabTitle );
		save.setTable( model );
		saveas.setDefaultFileName( tabTitle );
		saveas.setTable( model );
	}

	private void search( String ltxt ) {
		log.debug( "searching for: " + ltxt );
		String txt = ltxt.toUpperCase();
		renderer.clear();

		for ( int r = 0; r < table.getRowCount(); r++ ) {
			for ( int c = 0; c < table.getColumnCount(); c++ ) {
				Object val = table.getValueAt( r, c );
				if ( null != val ) {
					if ( val.toString().toUpperCase().contains( txt ) ) {
						log.debug( "found " + txt + " at (" + r + "," + c + ") {" + val + "}" );
						renderer.highlight( r, c );
						model.fireTableCellUpdated( r, c );
					}
				}
			}
		}
	}

	@Override
	public void populateToolBar( JToolBar jtb, final String tabTitle ) {
		setupToolBarButtons( tabTitle );
		jtb.add( save );

		saveall.setPlaySheetFrame( getPlaySheetFrame() );
		jtb.add( saveall );

		jtb.add( searchlabel );
		jtb.add( searchfield );
	}

	@Override
	public boolean hasChanges() {
		return model.needsSave();
	}

	@Override
	public Map<String, Action> getActions() {
		Map<String, Action> map = super.getActions();
		map.put( PlaySheetFrame.SAVE, save );
		map.put( PlaySheetFrame.SAVE_ALL, saveall );
		map.put( PlaySheetFrame.SAVE_AS, saveas );
		return map;
	}

	@Override
	public void incrementFont( float incr ) {
		super.incrementFont( incr );
		table.setRowHeight( table.getRowHeight() + (int) incr );
	}

	@Override
	public void create( List<Value[]> data, List<String> newheaders, IEngine engine ) {
		setHeaders( newheaders );
		log.debug( "into create: " + data.size() + " items" );
		model.setData( data, newheaders );

		TableColumnModel tm = table.getColumnModel();
		for ( int c = 0; c < tm.getColumnCount(); c++ ) {
			tm.getColumn( c ).setCellRenderer( renderer );
		}
	}

	@Override
	public void overlay( List<Value[]> data, List<String> newheaders, IEngine eng ) {
		log.debug( "into overlay: " + data.size() + " items" );
		model.addData( data );
	}

	protected JTable getTable() {
		return table;
	}

	protected ValueTableModel getModel() {
		return model;
	}

	@Override
	public List<Object[]> getTabularData() {
		List<Object[]> data = new ArrayList<>();

		final int cols = model.getColumnCount();
		final int rows = model.getRowCount();

		for ( int r = 0; r < rows; r++ ) {
			Object[] row = new Object[cols];

			for ( int c = 0; c < cols; c++ ) {
				row[c] = model.getValueAt( r, c );
			}

			data.add( row );
		}

		return data;
	}

	@Override
	public boolean prefersTabs() {
		return true;
	}

	/**
	 * Retrieves a Resource if the data from {@link JTable#getValueAt(int, int) }
	 * can be treated as Resource, or null.
	 *
	 * @param row
	 * @param col
	 * @return a Resource or null
	 */
	protected Resource asResource( int row, int col ) {
		if ( Resource.class.isAssignableFrom( table.getColumnClass( col ) ) ) {
			return Resource.class.cast( table.getValueAt( row, col ) );
		}
		return null;
	}

	private void addMouseListener() {
		table.addMouseListener( new MouseAdapter() {
			@Override
			public void mouseReleased( MouseEvent e ) {
				int r = table.convertRowIndexToModel( table.rowAtPoint( e.getPoint() ) );
				int c = table.columnAtPoint( e.getPoint() );
				if ( r >= 0 && r < table.getRowCount() ) {
					table.setRowSelectionInterval( r, r );
				}
				else {
					table.clearSelection();
				}

				int rowindex = table.getSelectedRow();
				if ( rowindex < 0 ) {
					return;
				}
				if ( SwingUtilities.isRightMouseButton( e ) ) {
					Resource rsr = asResource( r, c );
					if ( null != rsr ) {
						JPopupMenu popup = new JPopupMenu();
						popup.add( new ShowInGraphAction( rsr ) );
						popup.add( new EditPropertiesAction( rsr ) );
						popup.show( e.getComponent(), e.getX(), e.getY() );
					}
				}
			}
		} );
	}

	private class ShowInGraphAction extends AbstractAction {

		public final Resource uri;

		public ShowInGraphAction( Resource myuri ) {
			super( "Explore in Graph" );
			uri = myuri;
		}

		@Override
		public void actionPerformed( ActionEvent e ) {
			GraphPlaySheet gps = new GraphPlaySheet();
			gps.setShapeRepository( DIHelper.getInstance().getPlayPane().getColorShapeRepository() );
			gps.setTitle( "Node Explorer" );
			List<Value[]> vals = new ArrayList<>();
			vals.add( new Value[]{ uri } );
			addSibling( gps );
			gps.create( vals, Arrays.asList( "Subject" ), getEngine() );
		}
	}

	private class EditPropertiesAction extends AbstractAction {

		public final Resource uri;

		public EditPropertiesAction( Resource myuri ) {
			super( "View All Properties" );
			uri = myuri;
		}

		@Override
		public void actionPerformed( ActionEvent e ) {
			SEMOSSVertex vertex = new SEMOSSVertexImpl( IRI.class.cast( uri ) );
			try {
				Model model = getEngine().construct( ModelQueryAdapter.describe( uri ) );
				for ( Statement s : model ) {
					vertex.setValue( s.getPredicate(), s.getObject() );
				}
			}
			catch ( RepositoryException | MalformedQueryException | QueryEvaluationException ex ) {
				log.error( ex, ex );
			}
			addSibling( new PropertyEditorPlaySheet( Arrays.asList( vertex ),
					"Selected Node Properties", getEngine() ) );
		}
	}

	private class HighlightingRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1858290305167884631L;
		private final MultiMap<Integer, Integer> highlights = new MultiMap<>();

		@Override
		public Component getTableCellRendererComponent( JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column ) {
			JComponent comp = (JComponent) super.getTableCellRendererComponent( table, value, isSelected,
					hasFocus, row, column );
			if ( null != value ) {
				comp.setToolTipText( packageValueInHTML( value.toString() ) );
			}
			setOpaque( true );

			setBackground( table.getBackground() );
			setForeground( table.getForeground() );
			if ( highlights.containsKey( row ) ) {
				if ( highlights.get( row ).contains( column ) ) {
					setBackground( table.getSelectionBackground() );
					setForeground( table.getSelectionForeground() );
				}
			}
			return comp;
		}

		private String packageValueInHTML( String val ) {
			StringBuilder content = new StringBuilder();
			content.append( "<html>" );
			String[] words = val.split( " " );
			int lineLength = 0;
			int maxLineLength = 80;
			StringBuilder line = new StringBuilder();
			for ( String word : words ) {
				line.append( word ).append( " " );
				lineLength += word.length();
				if ( lineLength > maxLineLength ) {
					content.append( line.toString() );
					content.append( "<br>" );
					lineLength = 0;
					line = new StringBuilder();
				}
			}
			content.append( line.toString() );
			content.append( "</html>" );
			return content.toString();
		}

		public void clear() {
			highlights.clear();
		}

		public void highlight( int row, int col ) {
			highlights.add( row, col );
		}
	}
}
