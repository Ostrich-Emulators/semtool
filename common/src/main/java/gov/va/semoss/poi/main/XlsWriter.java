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
package gov.va.semoss.poi.main;

import gov.va.semoss.poi.main.LoadingSheetData.DataIterator;
import gov.va.semoss.poi.main.LoadingSheetData.LoadingNodeAndPropertyValues;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openrdf.model.URI;
import java.awt.Color;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.openrdf.model.Value;

/**
 * Create a workbook containing data formated in the Microsoft Excel Sheet
 * Format. This class has two modes of operation: the ImportData mode, and the
 * raw mode. In raw mode, callers should use {@link #createWorkbook() },
 * {@link #createTab(java.lang.String) }, {@link #addRow(java.lang.String[]) }
 * and {@link #write(java.io.File) } to generate the XLS file. In ImportData
 * mode, just use the all-in-one function
 * {@link #write(gov.va.semoss.poi.main.ImportData, java.io.File) } instead.
 */
public class XlsWriter implements GraphWriter {

	private static final Logger log = Logger.getLogger( XlsWriter.class );
	private static final int TAB_ROWLIMIT = 999999;

	private XSSFWorkbook currentwb;
	private XSSFSheet currentsheet;
	private XSSFRow currentrow;
	private String desiredtabname;
	private final List<String> currentheader = new ArrayList<>();
	private int rowcount = 0;
	private final Set<String> currentnames = new HashSet<>();
	private int maxtabrows = TAB_ROWLIMIT;

	/**
	 * Sets the max rows that can be added to a tab before continuing the data on
	 * another tab. This must be less than {@link #TAB_ROWLIMIT}
	 *
	 * @param rowspertab
	 */
	public void setTabRowLimit( int rowspertab ) {
		if ( rowspertab < 1 || rowspertab > TAB_ROWLIMIT ) {
			log.warn( "cannot set rows/tab to " + rowspertab + "; using "
					+ TAB_ROWLIMIT + " instead" );
			rowspertab = TAB_ROWLIMIT;
		}
		maxtabrows = rowspertab;
	}

	public XSSFWorkbook getCurrentWb() {
		return currentwb;
	}

	public XSSFSheet getCurrentSheet() {
		return currentsheet;
	}

	public XSSFRow getCurrentRow() {
		return currentrow;
	}

	@Override
	public void write( ImportData data, File output ) throws IOException {
		write( data, new FileOutputStream( output ) );
	}

	@Override
	public void write( ImportData data, OutputStream output ) throws IOException {
		createWorkbook( data );

		CellStyle errorstyle = currentwb.createCellStyle();
		errorstyle.setFillPattern( CellStyle.SOLID_FOREGROUND );
		errorstyle.setFillForegroundColor( IndexedColors.PINK.getIndex() );

		for ( LoadingSheetData nodes : data.getNodes() ) {
			List<String> props = new ArrayList<>( nodes.getProperties() );
			createTab( nodes.getName(), makeHeaderRow( nodes, props ) );

			// +2 -> 1 for the blank first col and 1 for the subject type
			String[] row = new String[2 + props.size()];
			CellStyle[] fmts = new CellStyle[2 + props.size()];

			DataIterator di = nodes.iterator();
			while ( di.hasNext() ) {
				LoadingNodeAndPropertyValues nap = di.next();
				row[1] = nap.getSubject();
				fmts[1] = ( nap.isSubjectError() ? errorstyle : null );

				int col = 2;
				for ( String prop : props ) {
					Value val = nap.get( prop );
					row[col++] = ( null == val ? null : val.stringValue() );
				}

				addRow( row, fmts );
			}
		}

		for ( LoadingSheetData rels : data.getRels() ) {
			List<String> props = new ArrayList<>( rels.getProperties() );
			createTab( rels.getName(), makeHeaderRow( rels, props ) );

			// +3 -> 1 for the blank first col and 1 for the subject type, 1 for object type
			String[] row = new String[3 + props.size()];
			CellStyle[] fmts = new CellStyle[3 + props.size()];

			if ( rels.isEmpty() ) {
				// no rows to add, but still add the relationship name field
				row[0] = rels.getRelname();
			}

			DataIterator di = rels.iterator();
			while ( di.hasNext() ) {
				LoadingNodeAndPropertyValues nap = di.next();
				if ( rels.hasErrors() ) {
					currentsheet.setTabColor( IndexedColors.ROSE.getIndex() );
				}

				// do we need the relation name in the first column?
				row[0] = ( nextRowIsFirstRowOfTab() ? rels.getRelname() : null );

				row[1] = nap.getSubject();
				fmts[1] = ( nap.isSubjectError() ? errorstyle : null );

				row[2] = nap.getObject();
				fmts[2] = ( nap.isObjectError() ? errorstyle : null );

				int col = 3;
				for ( String prop : props ) {
					Value val = nap.get( prop );
					row[col++] = ( null == val ? null : val.stringValue() );
				}

				addRow( row, fmts );
			}
		}

		write( output );
	}

	/**
	 * Is the next row the first one of the tab (excluding headers)?
	 *
	 * @return true if the next call to {@link #addRow(java.lang.String[],
	 * org.apache.poi.xssf.usermodel.XSSFCellStyle[]) } will be the first row of
	 * the tab
	 */
	protected boolean nextRowIsFirstRowOfTab() {
		if ( maxtabrows == rowcount ) {
			return true;
		}

		if ( currentheader.isEmpty() ) {
			return ( 0 == rowcount );
		}
		return ( 1 == rowcount );
	}

	public void createWorkbook() {
		currentwb = new XSSFWorkbook();
	}

	/**
	 * Convenience function to
	 * {@link #createTab(java.lang.String, java.lang.String[])} without a header
	 * row
	 *
	 * @param tabname the desired tab name
	 * @return the actual tab name
	 */
	public String createTab( String tabname ) {
		return createTab( tabname, new String[0] );
	}

	/**
	 * Creates a new tab, but adds a header row that will be propagated to any new
	 * tabs if the number of rows added &gt; {@link #TAB_ROWLIMIT}. Subsequent
	 * calls to {@link #addRow(java.lang.String[]) } will write to this new tab
	 *
	 * @param tabname the desired tab name
	 * @param headerrow the header row to duplicate if new tabs must be created
	 * @return the actual tab name
	 */
	public String createTab( String tabname, String[] headerrow ) {
		currentheader.clear();
		desiredtabname = tabname;
		String realname = generateSheetName( tabname, currentnames );
		currentsheet = currentwb.createSheet( realname );
		rowcount = 0;

		if ( !( null == headerrow || 0 == headerrow.length ) ) {
			currentheader.addAll( Arrays.asList( headerrow ) );
			addRow( headerrow );
		}

		return realname;
	}

	/**
	 * Creates a new row in the current tab. If the current tab has more than
	 * {@link #TAB_ROWLIMIT} rows, a new tab (with a duplicate header row, if set)
	 * will be created and the row added to that tab instead.
	 *
	 * @param values the data
	 */
	public void addRow( String[] values ) {
		addRow( values, null );
	}

	/**
	 * Adds a new row to the current tab. If the new row requires a new tab to
	 * also be created, do it
	 *
	 * @param values the row data
	 * @param formatting cell formatting
	 * @return true, if a new tab is created, else false
	 */
	public boolean addRow( String[] values, CellStyle[] formatting ) {
		boolean newtab = ( maxtabrows == rowcount );

		if ( newtab ) {
			// need to make a new tab
			createTab( desiredtabname, currentheader.toArray( new String[0] ) );
		}

		currentrow = currentsheet.createRow( rowcount++ );
		for ( int col = 0; col < values.length; col++ ) {
			Cell cell = currentrow.createCell( col );
			if ( null != formatting ) {
				if ( formatting.length > col && null != formatting[col] ) {
					cell.setCellStyle( formatting[col] );
				}
			}

			String val = values[col];
			if ( null != val ) {
				if ( NUMERIC.matcher( val ).find() ) {
					cell.setCellType( Cell.CELL_TYPE_NUMERIC );
					cell.setCellValue( Double.parseDouble( val ) );
				}
				else {
					cell.setCellValue( val.replaceAll( "\"", "" ) );
				}
			}
		}

		return newtab;
	}

	public boolean addRow( Object[] values ) {
		return addRow( values, null );
	}

	public boolean addRow( Object[] values, CellStyle[] formatting ) {
		String[] rows = new String[values.length];
		for ( int i = 0; i < rows.length; i++ ) {
			rows[i] = ( null == values[i] ? null : values[i].toString() );
		}

		return addRow( rows, formatting );
	}

	/**
	 * Writes the current worksheet to the given file. Any parent directories will
	 * be created automatically
	 *
	 * @param output the file to write to
	 * @throws IOException
	 */
	public void write( File output ) throws IOException {
		output.getParentFile().mkdirs();
		try ( OutputStream newExcelFile
				= new BufferedOutputStream( new FileOutputStream( output ) ) ) {
			write( newExcelFile );
		}
	}

	public void write( OutputStream output ) throws IOException {
		currentwb.write( output );
	}

	private String[] makeHeaderRow( LoadingSheetData b, Collection<String> props ) {
		// make the headers
		List<String> heads = new ArrayList<>();
		if ( b.isRel() ) {
			heads.add( "Relation" );
			heads.add( b.getSubjectType() );
			heads.add( b.getObjectType() );
		}
		else {
			heads.add( "Node" );
			heads.add( b.getSubjectType() );
		}

		for ( String prop : props ) {
			heads.add( prop );
		}

		return heads.toArray( new String[0] );
	}

	private void createWorkbook( ImportData importdata ) {
		ImportMetadata data = importdata.getMetadata();

		List<String[]> mddata = new ArrayList<>();
		if ( null != data.getSchemaBuilder() ) {
			mddata.add( new String[]{ "@prefix", ":schema",
				"<" + data.getSchemaBuilder().toString() + ">" } );
		}

		if ( null != data.getDataBuilder() ) {
			mddata.add( new String[]{ "@prefix", ":data",
				"<" + data.getDataBuilder().toString() + ">" } );
		}

		if ( null != data.getBase() ) {
			mddata.add( new String[]{ "@prefix", ":",
				"<" + data.getBase().toString() + ">" } );
		}

		for ( Map.Entry<String, String> en : data.getNamespaces().entrySet() ) {
			mddata.add( new String[]{ "@prefix", en.getKey(), "<" + en.getValue() + ">" } );
		}

		for ( String[] stmt : data.getStatements() ) {
			mddata.add( new String[]{ stmt[0], stmt[1], stmt[2] } );
		}

		createWorkbook();

		List<String> tabnames = new ArrayList<>();
		Set<String> sheetnames = new HashSet<>();

		for ( LoadingSheetData lsd : importdata.getSheets() ) {
			int count = 0;

			// if we have too many rows for one tab, we have
			// to separate this sheet data into multiple tabs
			while ( count < lsd.rows() ) {
				String tname = generateSheetName( lsd.getName(), sheetnames );
				tabnames.add( tname );
				count += maxtabrows;
			}
		}

		// don't write a metadata sheet if we don't have anything to put in it
		final String metaSheetName = ( mddata.isEmpty() ? null : "MetadataInfo" );

		writeLoadingSheet( tabnames, metaSheetName );

		if ( !mddata.isEmpty() ) {
			XlsWriter.this.createTab( metaSheetName );
			boolean first = true;
			for ( String[] row : mddata ) {
				String actuals[] = new String[4];
				System.arraycopy( row, 0, actuals, 1, row.length );
				if ( first ) {
					actuals[0] = "Metadata";
					first = false;
				}
				addRow( actuals );
			}
		}
	}

	/**
	 * Creates a loading sheet with the given name
	 *
	 * @param nodes
	 * @param metaSheetName if not null, add a metadata tab with this info
	 */
	protected void writeLoadingSheet( Collection<String> nodes,
			String metaSheetName ) {

		List<String[]> data = new ArrayList<>();
		data.add( new String[]{ "Sheet Name", "Type" } );
		for ( String key : nodes ) {
			data.add( new String[]{ key, "Usual" } );
		}
		if ( null != metaSheetName ) {
			data.add( new String[]{ metaSheetName, "Metadata" } );
		}

		XlsWriter.this.createTab( "Loader" );
		for ( String[] row : data ) {
			addRow( row );
		}
	}

	/**
	 * Common logic for finding a name for an excel workbook worksheet that is
	 * unique for that workbook and not longer than 32 characters. Each call to
	 * this function with the same <code>nodeKey</code> will generate a different
	 * name (Excel tabs cannot have identical names).
	 *
	 * @param nodeKey String to start with
	 * @param keySet Set of names that are already in use. the return of this
	 * function is automatically added to this set
	 *
	 * @return
	 */
	public static String generateSheetName( String nodeKey, Set<String> keySet ) {
		final int maxSheetNameLength = 31;

		if ( nodeKey.length() > maxSheetNameLength ) {
			nodeKey = nodeKey.substring( 0, maxSheetNameLength );
		}

		while ( nodeKey.endsWith( "-" ) ) {
			nodeKey = nodeKey.substring( 0, nodeKey.length() - 1 );
		}

		int inc = 10;
		final String loopkey = nodeKey;
		while ( keySet.contains( nodeKey ) ) {
			boolean firstloop = loopkey.equals( nodeKey );

			// we don't have to chop off anything on the first loop unless it's too long
			if ( firstloop ) {
				if ( nodeKey.length() > maxSheetNameLength - 2 ) {
					nodeKey = nodeKey.substring( 0, maxSheetNameLength - 2 );
				}
			}
			else {
				// on subsequent loops, we need to chop off the last thing we added
				nodeKey = nodeKey.substring( 0, nodeKey.length() - 2 );
			}

			nodeKey = nodeKey + ( inc++ );
		}
		keySet.add( nodeKey );

		return nodeKey;
	}

	public static class NodeAndPropertyValues extends HashMap<URI, Value> {

		private final URI subject;
		private final URI object;

		public NodeAndPropertyValues( URI subj ) {
			this( subj, null );
		}

		public NodeAndPropertyValues( URI subject, URI object ) {
			this.subject = subject;
			this.object = object;
		}

		public URI getSubject() {
			return subject;
		}

		public URI getObject() {
			return object;
		}

		public boolean isRel() {
			return ( null != object );
		}
	}

	public static class SheetRowCol {

		public final String sheetname;
		public final int row;
		public final int col;

		public SheetRowCol( String sheetname, int row, int col ) {
			this.sheetname = sheetname;
			this.row = row;
			this.col = col;
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 41 * hash + Objects.hashCode( this.sheetname );
			hash = 41 * hash + this.row;
			hash = 41 * hash + this.col;
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
			final SheetRowCol other = (SheetRowCol) obj;
			if ( !Objects.equals( this.sheetname, other.sheetname ) ) {
				return false;
			}
			if ( this.row != other.row ) {
				return false;
			}
			return ( this.col == other.col );
		}

		@Override
		public String toString() {
			return sheetname + " (" + row + "," + col + ")";
		}
	}

	public static class CellFormatting {

		public final Color background;

		public CellFormatting( Color background ) {
			this.background = background;
		}

		@Override
		public String toString() {
			return background.toString();
		}

		@Override
		public int hashCode() {
			int hash = 5;
			hash = 89 * hash + Objects.hashCode( this.background );
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
			final CellFormatting other = (CellFormatting) obj;
			return ( Objects.equals( this.background, other.background ) );
		}
	}
}
