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
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openrdf.model.URI;
import gov.va.semoss.util.Utility;
import java.awt.Color;
import java.io.File;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

/**
 * Create a workbook containing data formated in the Microsoft Excel Sheet
 * Format
 */
public class XlsWriter {

	private static final Logger log = Logger.getLogger( XlsWriter.class );
	protected static final Pattern NUMERIC = Pattern.compile( "^\\d+.?\\d*$" );

	private final Map<URI, String> labelcache = new HashMap<>();

	private void makeHeaderRow( XSSFSheet sheet, LoadingSheetData b,
			Collection<String> props ) {
		// make the headers
		XSSFRow row = sheet.createRow( 0 );
		int col;

		if ( b.isRel() ) {
			row.createCell( 0 ).setCellValue( "Relation" );
			row.createCell( 1 ).setCellValue( b.getSubjectType() );
			row.createCell( 2 ).setCellValue( b.getObjectType() );
			col = 3;
		}
		else {
			col = 2;
			row.createCell( 0 ).setCellValue( "Node" );
			row.createCell( 1 ).setCellValue( b.getSubjectType() );
		}

		for ( String prop : props ) {
			row.createCell( col++ ).setCellValue( prop );
		}
	}

	/**
	 * Writes the given data to the output file. This file (and it's parents) will
	 * be created if they don't already exist
	 *
	 * @param data
	 * @param output
	 * @throws IOException
	 */
	public void write( ImportData data, File output ) throws IOException {
		XSSFWorkbook wb = createWorkbook( data );
		Set<String> sheetNamesSoFar = new HashSet<>();

		XSSFCellStyle errorstyle = wb.createCellStyle();
		errorstyle.setFillPattern( XSSFCellStyle.SOLID_FOREGROUND );
    errorstyle.setFillForegroundColor( new XSSFColor( Color.PINK ) );

		for ( NodeLoadingSheetData nodes : data.getNodes() ) {
			List<String> props = new ArrayList<>( nodes.getProperties() );
			String name = generateSheetName( nodes.getName(), sheetNamesSoFar );
			XSSFSheet sheet = wb.createSheet( name );

			makeHeaderRow( sheet, nodes, props );

			int rownum = 0;
			for ( LoadingNodeAndPropertyValues nap : nodes.getData() ) {
				// the first row needs to have the header values
				XSSFRow row = sheet.createRow( ++rownum );

				Cell cell1 = row.createCell( 1 );
				cell1.setCellValue( nap.getSubject() );
				if ( nap.isSubjectError() ) {
					cell1.setCellStyle( errorstyle );
				}

				int col = 2;
				for ( String prop : props ) {
					Value val = nap.get( prop );
					if ( null != val ) {
						Cell cellx = row.createCell( col );
						cellx.setCellValue( val.stringValue() );
					}
					col++;
				}
			}
		}

		for ( RelationshipLoadingSheetData rels : data.getRels() ) {
			List<String> props = new ArrayList<>( rels.getProperties() );
			String name = generateSheetName( rels.getName(), sheetNamesSoFar );

			XSSFSheet sheet = wb.createSheet( name );

			if ( rels.hasErrors() ) {
				sheet.setTabColor( IndexedColors.ROSE.getIndex() );
			}

			makeHeaderRow( sheet, rels, props );

			int rownum = 0;
			for ( LoadingNodeAndPropertyValues nap : rels.getData() ) {
				XSSFRow row = sheet.createRow( rownum + 1 );

				if ( 0 == rownum ) {
					// cell needs the relation name
					Cell cell0 = row.createCell( 0 );
					cell0.setCellValue( rels.getRelname() );
				}

				Cell cell1 = row.createCell( 1 );
				cell1.setCellValue( nap.getSubject() );
				if ( nap.isSubjectError() ) {
					cell1.setCellStyle( errorstyle );
				}

				Cell cell2 = row.createCell( 2 );
				cell2.setCellValue( nap.getObject() );
				if ( nap.isObjectError() ) {
					cell2.setCellStyle( errorstyle );
				}

				int col = 3;
				for ( String prop : props ) {
					Value val = nap.get( prop );
					if ( null != val ) {
						Cell cellx = row.createCell( col );
						cellx.setCellValue( val.stringValue() );
					}
					col++;
				}
				rownum++;
			}
		}

		output.getParentFile().mkdirs();
		try ( OutputStream newExcelFile = new BufferedOutputStream( new FileOutputStream( output ) ) ) {
			wb.write( newExcelFile );
		}
	}

	protected XSSFWorkbook createWorkbook( ImportData importdata ) {
		final String metaSheetName = "MetadataInfo";
		ImportMetadata data = importdata.getMetadata();
		XSSFWorkbook wb = new XSSFWorkbook();

		XSSFSheet loader = wb.createSheet( "Loader" );
		List<String> tabnames = new ArrayList<>();
		Set<String> sheetnames = new HashSet<>();
		for ( LoadingSheetData lsd : importdata.getAllData() ) {
			String tname = generateSheetName( lsd.getName(), sheetnames );
			tabnames.add( tname );
		}
		writeLoadingSheet( loader, tabnames, metaSheetName );

		List<String[]> mddata = new ArrayList<>();
		if ( null != data.getSchemaBuilder() ) {
			mddata.add( new String[]{ "@schema-namespace", null, data.getSchemaBuilder().toString() } );
		}

		if ( null != data.getDataBuilder() ) {
			mddata.add( new String[]{ "@data-namespace", null, data.getDataBuilder().toString() } );
		}

		if ( null != data.getBase() ) {
			mddata.add( new String[]{ "@data", null, data.getBase().toString() } );
		}

		for ( Map.Entry<String, String> en : data.getNamespaces().entrySet() ) {
			mddata.add( new String[]{ "@prefix", en.getKey(), en.getValue() } );
		}

		for ( Map.Entry<URI, String> en : data.getExtras().entrySet() ) {
			mddata.add( new String[]{ en.getKey().toString(), null, en.getValue() } );
		}

		for ( Statement stmt : data.getStatements() ) {
			mddata.add( new String[]{ "@triple", stmt.getSubject().stringValue(),
				stmt.getPredicate().stringValue(), stmt.getObject().stringValue() } );
		}

		XSSFSheet metadata = wb.createSheet( metaSheetName );
		writeSheet( metadata, mddata );

		return wb;
	}

	/**
	 * Creates a loading sheet with the given name
	 *
	 * @param nodes
	 * @param metaSheetName if not null, add a metadata tab with this info
	 * @param loader the worksheet to write the data to
	 */
	protected void writeLoadingSheet( XSSFSheet loader, Collection<String> nodes,
			String metaSheetName ) {

		List<String[]> data = new ArrayList<>();
		data.add( new String[]{ "Sheet Name", "Type" } );
		for ( String key : nodes ) {
			data.add( new String[]{ key, "Usual" } );
		}
		if ( null != metaSheetName ) {
			data.add( new String[]{ metaSheetName, "Metadata" } );
		}

		writeSheet( loader, data );
	}

	protected void writeSheet( XSSFSheet loader, Collection<String[]> data ) {

		int count = 0;
		for ( String[] celldata : data ) {
			XSSFRow row = loader.createRow( count++ );
			int col = 0;
			for ( String val : celldata ) {
				XSSFCell cell = row.createCell( col++ );
				if ( !( null == val || val.isEmpty() ) ) {
					if ( NUMERIC.matcher( val ).find() ) {
						cell.setCellType( Cell.CELL_TYPE_NUMERIC );
						cell.setCellValue( Double.parseDouble( val ) );
					}
					else {
						cell.setCellValue( val.replaceAll( "\"", "" ) );
					}
				}
			}
		}
	}

	public void addLabels( Map<URI, String> newlabels ) {
		labelcache.putAll( newlabels );
	}

	protected String getLabel( URI uri ) {
		if ( !labelcache.containsKey( uri ) ) {
			labelcache.put( uri, uri.getLocalName() );
		}
		return labelcache.get( uri );
	}

	protected Map<URI, String> sortProperties( Collection<URI> properties ) {
		Map<URI, String> ret = new HashMap<>();
		for ( URI p : properties ) {
			ret.put( p, getLabel( p ) );
		}

		return Utility.sortUrisByLabel( ret );
	}

	protected void writePropertiesToArray( NodeAndPropertyValues npv,
			Collection<URI> properties, String[] vals, int startpos ) {
		for ( URI prop : properties ) {
			Value v = npv.get( prop );
			vals[startpos++] = ( null == v ? null : v.stringValue() );
		}
	}

	/**
	 * Common logic for finding a name for an excel workbook worksheet that is
	 * unique for that workbook and not longer than 32 characters
	 *
	 * @param nodeKey String to start with
	 * @param keySet Set of names that are aleady in use. the return of this
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
		while ( keySet.contains( nodeKey ) ) {
			nodeKey = nodeKey.substring( 0, maxSheetNameLength - 2 ) + ( inc++ );
		}
		keySet.add( nodeKey );

		return nodeKey;
	}

	private static void styleCell( Cell cell0, String name, int row, int col,
			Map<SheetRowCol, CellFormatting> cellformatting, Map<CellFormatting, CellStyle> styles,
			XSSFWorkbook wb ) {

		if ( cellformatting.isEmpty() ) {
			return;
		}

		SheetRowCol src = new SheetRowCol( name, row, col );
		if ( cellformatting.containsKey( src ) ) {
			CellFormatting fmt = cellformatting.get( src );
			if ( !styles.containsKey( fmt ) ) {
				XSSFCellStyle style = wb.createCellStyle();
				style.setFillPattern( XSSFCellStyle.FINE_DOTS );
				style.setFillBackgroundColor( new XSSFColor( fmt.background ) );
				styles.put( fmt, style );
			}

			cell0.setCellStyle( styles.get( fmt ) );
		}
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
