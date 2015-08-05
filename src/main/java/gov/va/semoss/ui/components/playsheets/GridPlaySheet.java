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
package gov.va.semoss.ui.components.playsheets;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.ui.components.models.ValueTableModel;
import gov.va.semoss.ui.components.renderers.LabeledPairTableCellRenderer;
import gov.va.semoss.util.MultiSetMap;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.DefaultRowSorter;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * The GridPlaySheet class creates the panel and table for a grid view of data
 * from a SPARQL query.
 */
public class GridPlaySheet extends GridRAWPlaySheet {

	private static final long serialVersionUID = 983237700844677993L;

	private static final Logger log = Logger.getLogger( GridPlaySheet.class );

	private final LabeledPairTableCellRenderer<URI> renderer = new LabeledPairTableCellRenderer<>();
	
	private final MultiSetMap<Integer, Integer> urilocations = new MultiSetMap<>();
	
	private final Map<RowCol, Resource> uris = new HashMap<>();
	
	private final CopyPasteMediator cpMediator;

	public GridPlaySheet() {
		super( new ValueTableModel( false ) );
		JTable table = getTable();
		// Initialize a Copy/Paste mediator to handle the Ctrl+C, Ctrl+V events
		cpMediator = new CopyPasteMediator();
		JTableHeader header = getTable().getTableHeader();
		header.addMouseListener(new HeaderActionMediator(getTable()));
		table.setDefaultRenderer( URI.class, renderer );
		// In order to enable the excel-style mouse interactions, we'll set
		// the enhanced table interactions here
		HeaderActionMediator headerMediator = new HeaderActionMediator(table);
		header.addMouseListener(headerMediator);
		
		CellActionMediator cellMediator = new CellActionMediator(table);
		table.setCellSelectionEnabled(true);
		table.setSelectionBackground(new Color(120,120,255));
	}

	@Override
	public void create( List<Value[]> data, List<String> newheaders, IEngine engine ) {
		uris.clear();
		urilocations.clear();
		markResources( data );
		super.create( convertUrisToLabels( data, engine ), newheaders, engine );
	}

	@Override
	public void overlay( List<Value[]> data, List<String> newheaders, IEngine eng ) {
		markResources( data );
		super.overlay( convertUrisToLabels( data, eng ), newheaders, eng );
	}

	private void markResources( List<Value[]> data ) {
		final int startrow = getTable().getRowCount();

		int row = -1;
		for ( Value[] rowdata : data ) {
			row++;
			int col = -1;
			for ( Value val : rowdata ) {
				col++;

				if ( val instanceof Resource ) {
					urilocations.add( startrow + row, col );
					uris.put( new RowCol( startrow + row, col ),
							Resource.class.cast( val ) );
				}
			}
		}
	}

	@Override
	protected Resource asResource( int row, int col ) {
		return uris.get( new RowCol( row, col ) );
	}

	private static class RowCol {

		public final int row;
		private final int col;

		public RowCol( int row, int col ) {
			this.row = row;
			this.col = col;
		}

		@Override
		public int hashCode() {
			int hash = 5;
			hash = 61 * hash + this.row;
			hash = 61 * hash + this.col;
			return hash;
		}

		@Override
		public boolean equals( Object obj ) {
			if ( obj == null ) {
				return false;
			}
			if ( getClass() != obj.getClass() ) {
				return false;
			}
			final RowCol other = (RowCol) obj;
			if ( this.row != other.row ) {
				return false;
			}
			if ( this.col != other.col ) {
				return false;
			}
			return true;
		}
	}
	
	
	
	public class HeaderActionMediator extends MouseAdapter {
		
		private final JTableHeader header;  
 
		public HeaderActionMediator(JTable table){
			header = table.getTableHeader();
			int columnCount = table.getColumnCount();
			TableRowSorter sorter = (TableRowSorter)getTable().getRowSorter();
			getTable().getTableHeader().setEnabled(false);
			for (int i=0; i<columnCount; i++){
				sorter.setSortable(i, false);
				
			}

		}
		
        public void mouseClicked(MouseEvent event) {  
            int col = header.columnAtPoint(event.getPoint());
            getTable().clearSelection();
            if ((event.getModifiers()& ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK) {
            	getTable().setColumnSelectionAllowed(false);
                getTable().setRowSelectionAllowed(true);
            	sort(col);
            }
            else {
            	ListSelectionModel lsModel = getTable().getSelectionModel();
            	lsModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            	getTable().setColumnSelectionAllowed(true);
                getTable().setRowSelectionAllowed(false);
                getTable().setColumnSelectionInterval(col, col);
            }  
        }   

		private boolean descending = false;
        
		private void sort(int columnIndex){
			TableRowSorter sorter = (TableRowSorter)getTable().getRowSorter();
			sorter.setSortable(columnIndex, true); 
			sorter.toggleSortOrder(columnIndex);
			sorter.setSortable(columnIndex, false);
			int rowCount = getTable().getRowCount();
			getModel().fireTableRowsUpdated(0, rowCount - 1);
			
			if (descending){
				sorter.setSortKeys( Arrays.asList( new RowSorter.SortKey( columnIndex, SortOrder.DESCENDING ) ) );
			}
			else {
				sorter.setSortKeys( Arrays.asList( new RowSorter.SortKey( columnIndex, SortOrder.ASCENDING ) ) );
			}
			descending = !descending;
			
			/*
			TableRowSorter<ValueTableModel> trs = new TableRowSorter<>( getModel() );
			getTable().setRowSorter( trs );
			
			trs.sort();
			getModel().fireTableDataChanged();
			*/
		}
	}
	
	public class CellActionMediator extends MouseAdapter {
 
		public CellActionMediator(JTable table){

		}
		
		
        public void mouseClicked(MouseEvent event) {  
        	getTable().setColumnSelectionAllowed(false);
            getTable().setRowSelectionAllowed(true);
        }   

	}
	
	

	
	/**
	 * The CopyPasteMediator enables copying and pasting into and out of,
	 * respectively, the system clipboard. Typically, programs like excel, and
	 * data from HTML tables are stored as /t and /n delimited strings in the
	 * clipboard. This mediator allows for copying and pasting to and from this
	 * parent class' JTable, using such syntax.
	 */
	public class CopyPasteMediator implements ActionListener {

		public CopyPasteMediator() {
			// Map the copy and paste keystroke to local attributes
			KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C,
					ActionEvent.CTRL_MASK, false);
			KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V,
					ActionEvent.CTRL_MASK, false);
			// Register the keyboard actions to the JTable
			getTable().registerKeyboardAction(this, "Copy", copy,
					JComponent.WHEN_FOCUSED);
			getTable().registerKeyboardAction(this, "Paste", paste,
					JComponent.WHEN_FOCUSED);
		}

		/**
		 * Listens to CTRL+C, and CTRL+V keystroke events
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().compareTo("Copy") == 0) {
				copy();
			}
			if (e.getActionCommand().compareTo("Paste") == 0) {
				paste();
			}
		}

		/**
		 * Copy content from the JTable, based on the current selection
		 * @return True if the selection is valid and contiguous, and false otherwise
		 */
		private boolean copy() {
			// Initialize the String
			StringBuilder content = new StringBuilder();
			// Ensure the cells are contiguous
			int totalCols = getTable().getSelectedColumnCount();
			int totalRows = getTable().getSelectedRowCount();
			int[] selectedRows = getTable().getSelectedRows();
			int[] seletedCols = getTable().getSelectedColumns();
			// Check if the cells are contiguous, othwise, the copy/paste won't
			// work
			if (areCellsContiguous(totalRows, totalCols, selectedRows,
					seletedCols)) {
				// If the selection is valid, build a string with tabs as
				// delimiters between
				// cells, and line feeds between rows
				for (int i = 0; i < totalRows; i++) {
					for (int j = 0; j < totalCols; j++) {
						content.append(getTable().getValueAt(selectedRows[i],
								seletedCols[j]));
						if (j < totalCols - 1) {
							content.append("\t");
						}
					}
					content.append("\n");
				}
				// Set the selection contents
				StringSelection copiedContent = new StringSelection(
						content.toString());
				// Get a reference to the system clipboard
				Clipboard clipboard = Toolkit.getDefaultToolkit()
						.getSystemClipboard();
				// Set the contents of the clipboard
				clipboard.setContents(copiedContent, copiedContent);
				return true;
			}
			else {
				return false;
			}
		}

		/**
		 * Paste data into the JTable of the parent class, currently 
		 * found in the system clipboard
		 * @return True if the paste is successful, false otherwise
		 */
		private boolean paste() {
			// Get the location row/col of the "cursor" so that we
			// know where to begin the paste operation
			int startRow = (getTable().getSelectedRows())[0];
			int startCol = (getTable().getSelectedColumns())[0];
			try {
				// Get a reference to the system clipboard
				Clipboard clipboard = Toolkit.getDefaultToolkit()
						.getSystemClipboard();
				// Get the contents of the clipboard
				String content = (String) (clipboard.getContents(this)
						.getTransferData(DataFlavor.stringFlavor));
				// Create a tokenizer to iterate through the lines/rows
				StringTokenizer rowTokenizer = new StringTokenizer(content,
						"\n");
				int rowCount = 0;
				while (rowTokenizer.hasMoreTokens()) {
					// Get the row content
					String rowContent = rowTokenizer.nextToken();
					// Initialize the cell tokenizer on the row content
					StringTokenizer cellTokenizer = new StringTokenizer(
							rowContent, "\t");
					int cellCount = 0;
					// Iterate through the cells in the row
					while (cellTokenizer.hasMoreTokens()) {
						// Capture the value
						String value = (String) cellTokenizer.nextToken();
						// If the row and cell position of the paste action is
						// not beyond the total
						// row and cells of the table, proceed with setting the
						// value
						if (startRow + rowCount < getTable().getRowCount()
								&& startCol + cellCount < getTable()
										.getColumnCount())
							getTable().setValueAt(value, startRow + rowCount,
									startCol + cellCount);
						cellCount++;
					}
					rowCount++;
				}
				return true;
			} catch (Exception ex) {
				return false;
			}
		}

		private boolean areCellsContiguous(int totalRows, int totalCols,
				int[] seletedRows, int[] selectedColumns) {
			if (((totalRows - 1 == seletedRows[seletedRows.length - 1]
					- seletedRows[0] && totalRows == seletedRows.length) && (totalCols - 1 == selectedColumns[selectedColumns.length - 1]
					- selectedColumns[0] && totalCols == selectedColumns.length))) {
				return true;
			} else {
				return false;
			}
		}
		
	}
}
