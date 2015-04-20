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

import static gov.va.semoss.poi.main.AbstractFileReader.getRDFStringValue;
import static gov.va.semoss.poi.main.AbstractFileReader.getUriFromRawString;
import gov.va.semoss.poi.main.FileLoadingException.ErrorType;
import gov.va.semoss.poi.main.LoadingSheetData.LoadingNodeAndPropertyValues;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import gov.va.semoss.util.MultiMap;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Loading data into SEMOSS using Microsoft Excel Loading Sheet files
 */
public class POIReader implements ImportFileReader {

	private static final Logger logger = Logger.getLogger( POIReader.class );

	private static enum SheetType {

		METADATA, NODE, RELATION, LOADER, UNKNOWN, EMPTY
	};

	private ImportData readNonloadingSheet( XSSFWorkbook workbook ) {
		ImportData id = new ImportData();

		int sheets = workbook.getNumberOfSheets();
		for ( int sheetnum = 0; sheetnum < sheets; sheetnum++ ) {
			XSSFSheet sheet = workbook.getSheetAt( sheetnum );
			XSSFRow firstRow = sheet.getRow( 0 );

			String subjectType = firstRow.getCell( 0 ).getStringCellValue();
			LoadingSheetData nlsd = new LoadingSheetData( sheet.getSheetName(), subjectType );
			ValueFactory vf = new ValueFactoryImpl();

			Map<Integer, String> propnames = new HashMap<>();
			int lastpropcol = firstRow.getLastCellNum();
			for ( int col = 1; col < lastpropcol; col++ ) {
				String prop = firstRow.getCell( col ).getStringCellValue();
				propnames.put( col, prop );
				nlsd.addProperty( prop );
			}

			int rows = sheet.getLastRowNum();
			for ( int r = 1; r <= rows; r++ ) {
				XSSFRow row = sheet.getRow( r );
				String nodename = row.getCell( 0 ).getStringCellValue();
				LoadingNodeAndPropertyValues nap = nlsd.add( nodename );

				for ( Map.Entry<Integer, String> en : propnames.entrySet() ) {
					String val = row.getCell( en.getKey() ).getStringCellValue();
					nap.put( en.getValue(), vf.createLiteral( val ) );
				}
			}

			id.add( nlsd );
		}

		return id;
	}

	@Override
	public ImportMetadata getMetadata( File file ) throws IOException, FileLoadingException {
		logger.debug( "getting metadata from file: " + file );
		final XSSFWorkbook workbook
				= new XSSFWorkbook( new FileInputStream( file ) );

		ImportData data = new ImportData();
		for ( String name : categorizeSheets( workbook ).getNN( SheetType.METADATA ) ) {
			XSSFSheet metadataSheet = workbook.getSheet( name );
			loadMetadata( metadataSheet, data );
		}

		return data.getMetadata();
	}

	@Override
	public ImportData readOneFile( File file ) throws IOException, FileLoadingException {
		final XSSFWorkbook workbook
				= new XSSFWorkbook( new FileInputStream( file ) );

		MultiMap<SheetType, String> typeToSheetNameLkp = categorizeSheets( workbook );

		ImportData data = new ImportData();
		AbstractFileReader.initNamespaces( data );

		// we have sheets without a specified type, so open like a regular spreadsheet
		if ( !typeToSheetNameLkp.getNN( SheetType.UNKNOWN ).isEmpty() ) {
			logger.warn( "Trying to import sheet with no loader tab" );
			return readNonloadingSheet( workbook );
		}

		for ( String sheetname : typeToSheetNameLkp.getNN( SheetType.METADATA ) ) {
			XSSFSheet metadataSheet = workbook.getSheet( sheetname );
			loadMetadata( metadataSheet, data );
		}

		for ( String sheetname : typeToSheetNameLkp.getNN( SheetType.NODE ) ) {
			loadSheet( sheetname, workbook, data );
		}

		for ( String sheetname : typeToSheetNameLkp.getNN( SheetType.RELATION ) ) {
			loadSheet( sheetname, workbook, data );
		}

		return data;
	}

	private MultiMap<SheetType, String> categorizeSheets( XSSFWorkbook workbook )
			throws FileLoadingException {
		MultiMap<SheetType, String> typeToSheetNameLkp = new MultiMap<>();
		// figure out what this spreadsheet contains...if we have a "Loader" tab,
		// only worry about those sheets named in it. Otherwise, go through every
		// sheet and figure out what we have

		XSSFSheet lSheet = workbook.getSheet( "Loader" );
		if ( null == lSheet ) {
			// no loader sheet, so check through all the sheets
			for ( int i = 0; i < workbook.getNumberOfSheets(); i++ ) {
				String name = workbook.getSheetName( i );
				SheetType st = getSheetType( workbook, name );
				if ( SheetType.EMPTY != st ) {
					typeToSheetNameLkp.add( st, name );
				}
			}
		}
		else {
			// we have a loader sheet, so only worry about the sheets named in it
			typeToSheetNameLkp.add( SheetType.LOADER, "Loader" );

			Map<String, SheetType> fromloading = categorizeFromLoadingSheet( lSheet );
			for ( Map.Entry<String, SheetType> en : fromloading.entrySet() ) {
				String name = en.getKey();
				SheetType loadertype = en.getValue();
				SheetType realtype = getSheetType( workbook, name );

				if ( SheetType.EMPTY != realtype ) {
					if ( SheetType.METADATA == loadertype && SheetType.METADATA != realtype ) {
						throw new FileLoadingException( ErrorType.INCONSISTENT_DATA,
								"Sheet " + name + " does not include \"Metadata\" keyword in cell A1" );
					}
					else {
						typeToSheetNameLkp.add( realtype, name );
					}
				}
			}
		}

		return typeToSheetNameLkp;
	}

	private static Map<String, SheetType> categorizeFromLoadingSheet( XSSFSheet lSheet )
			throws FileLoadingException {
		Map<String, SheetType> map = new HashMap<>();
		XSSFRow header = lSheet.getRow( 0 );
		XSSFCell a1 = header.getCell( 0 );
		XSSFCell b1 = header.getCell( 1 );
		String a1val = ( cellIsEmpty( a1 ) ? "" : a1.getStringCellValue() );
		String b1val = ( cellIsEmpty( b1 ) ? "" : b1.getStringCellValue() );
		boolean mustHaveType = !cellIsEmpty( b1 );

		if ( !"Sheet Name".equals( a1val ) ) {
			throw new FileLoadingException( ErrorType.MISSING_DATA, "Cell A1 must be \"Sheet Name\"" );
		}
		if ( mustHaveType && !"Type".equals( b1val ) ) {
			throw new FileLoadingException( ErrorType.MISSING_DATA, "Cell B1 must be \"Type\"" );
		}

		for ( int rIndex = 1; rIndex <= lSheet.getLastRowNum(); rIndex++ ) {
			XSSFRow row = lSheet.getRow( rIndex );
			if ( row == null ) {
				continue;
			}

			if ( row.getLastCellNum() > 2 ) {
				throw new FileLoadingException( ErrorType.UNTYPED_DATA,
						"Too much data is row " + rIndex );
			}

			String sheetNameToLoad = getString( row.getCell( 0 ) );
			String sheetTypeToLoad = getString( row.getCell( 1 ) );
			if ( sheetNameToLoad.isEmpty() || ( sheetTypeToLoad.isEmpty() && mustHaveType ) ) {
				if ( sheetNameToLoad.isEmpty() ) {
					throw new FileLoadingException( ErrorType.MISSING_DATA,
							"No sheet name on row " + rIndex );
				}
				else {
					throw new FileLoadingException( ErrorType.MISSING_DATA,
							"No type specified for sheet " + sheetNameToLoad );
				}
			}
			if ( mustHaveType
					&& !( "Metadata".equals( sheetTypeToLoad ) || "Usual".equals( sheetTypeToLoad ) ) ) {
				throw new FileLoadingException( ErrorType.INCONSISTENT_DATA,
						"Invalid type specified: " + sheetTypeToLoad );
			}

			map.put( sheetNameToLoad, ( "Metadata".equals( sheetNameToLoad )
					? SheetType.METADATA : SheetType.UNKNOWN ) );
		}

		return map;
	}

	private SheetType getSheetType( XSSFWorkbook workbook, String sheetname ) {
		XSSFSheet sheet = workbook.getSheet( sheetname );
		XSSFRow row = sheet.getRow( 0 );
		if ( null == row ) {
			return SheetType.EMPTY;
		}

		// see what's in cell A1
		Cell cell = row.getCell( 0 );
		String type = cell.getStringCellValue();

		switch ( type ) {
			case "Metadata":
				return SheetType.METADATA;
			case "Relation":
				return SheetType.RELATION;
			case "Node":
				return SheetType.NODE;
			default:
				return SheetType.UNKNOWN;
		}
	}

	private void loadMetadata( XSSFSheet metadataSheet, ImportData data ) {
		if ( metadataSheet == null ) {
			return;
		}

		data.getMetadata().setLegacyMode( false );
		// we want to load the base uri first, data-namespace, schema-namespace,
		// prefixes, and finally triples. so read everything first, and load later
		String datanamespace = null;
		String schemanamespace = null;
		String baseuri = null;
		Map<String, String> namespaces = new HashMap<>();
		List<String[]> triples = new ArrayList<>();

		logger.debug( ( metadataSheet.getLastRowNum() + 1 )
				+ " metadata rows to interpret" );
		ImportMetadata metas = data.getMetadata();

		// read the data
		for ( int i = 0; i <= metadataSheet.getLastRowNum(); i++ ) {
			XSSFRow row = metadataSheet.getRow( i );
			if ( row == null ) {
				logger.warn( "skipping row: " + i + " (doesn't exist?)" );
				continue;
			}

			XSSFCell cell0 = row.getCell( 1 );
			XSSFCell cell2 = row.getCell( 3 );
			if ( cellIsEmpty( cell0 ) || cellIsEmpty( cell2 ) ) {
				logger.warn( "skipping row: " + i + " (empty cell)" );
				continue;
			}

			cell0.setCellType( XSSFCell.CELL_TYPE_STRING );
			cell2.setCellType( XSSFCell.CELL_TYPE_STRING );

			String propName = cell0.getStringCellValue();
			String propValue = cell2.getStringCellValue();

			XSSFCell cell1 = row.getCell( 2 );
			String propertyMiddleColumn = ( cellIsEmpty( cell1 )
					? "" : cell1.getStringCellValue() );

			if ( "@schema-namespace".equals( propName ) ) {
				schemanamespace = propValue;
			}
			else if ( "@data-namespace".equals( propName ) ) {
				datanamespace = propValue;
			}
			else if ( "@base".equals( propName ) ) {
				baseuri = propValue;
			}
			else if ( "@prefix".equals( propName ) ) {
				namespaces.put( propertyMiddleColumn, propValue );
			}
			else {
				if ( !propertyMiddleColumn.isEmpty() ) {
					triples.add( new String[]{ propName, propertyMiddleColumn, propValue } );
				}
			}
		}

		// now set the data
		if ( null != baseuri ) {
			logger.debug( "setting base uri to " + baseuri );
			metas.setBase( new URIImpl( baseuri ) );
		}
		if ( null != datanamespace ) {
			logger.debug( "setting data namespace to " + datanamespace );
			metas.setDataBuilder( datanamespace );
		}
		if ( null != schemanamespace ) {
			logger.debug( "setting schema namespace to " + schemanamespace );
			metas.setSchemaBuilder( schemanamespace );
		}

		for ( Map.Entry<String, String> en : namespaces.entrySet() ) {
			logger.debug( "registering namespace: "
					+ en.getKey() + " => " + en.getValue() );
			metas.setNamespace( en.getKey(), en.getValue() );
		}

		for ( String[] triple : triples ) {
			logger.debug( "adding custom triple: "
					+ triple[0] + " => " + triple[1] + " => " + triple[2] );

			metas.add( new StatementImpl( getUriFromRawString( triple[0], data ),
					getUriFromRawString( triple[1], data ),
					getUriFromRawString( triple[2], data ) ) );
		}
	}

	private Map<String, Value> getPropertyValues( XSSFRow row, SheetConfig props,
			Map<String, Integer> properties, ImportData id ) {
		Map<String, Value> propHash = new HashMap<>();

		ValueFactory vf = new ValueFactoryImpl();
		for ( Map.Entry<String, Integer> prop : properties.entrySet() ) {
			String propName = prop.getKey();
			XSSFCell cellValue = row.getCell( prop.getValue() );
			if ( !cellIsEmpty( cellValue ) ) {
				if ( cellValue.getCellType() != XSSFCell.CELL_TYPE_NUMERIC ) {
					cellValue.setCellType( XSSFCell.CELL_TYPE_STRING );
					propHash.put( propName,
							getRDFStringValue( cellValue.getStringCellValue(), id, vf ) );
				}
				else if ( DateUtil.isCellDateFormatted( cellValue ) ) {
					propHash.put( propName, vf.createLiteral( cellValue.getDateCellValue() ) );
				}
				else {
					propHash.put( propName, vf.createLiteral( cellValue.getNumericCellValue() ) );
				}
			}
		}

		return propHash;
	}

	private static boolean cellIsEmpty( Cell cell ) {
		if ( null == cell || cell.getCellType() == XSSFCell.CELL_TYPE_BLANK
				|| cell.toString().isEmpty() ) {
			return true;
		}
		return false;
	}

	/**
	 * Always return a non-null string (will be "" for null cells).
	 *
	 * @param cell
	 * @return
	 */
	private static String getString( Cell cell ) {
		return ( cellIsEmpty( cell ) ? "" : cell.getStringCellValue() );
	}

	private void loadSheet( String sheetToLoad, XSSFWorkbook workbook, ImportData id ) {
		XSSFSheet lSheet = workbook.getSheet( sheetToLoad );
		if ( lSheet == null ) {
			logger.warn( "Excel tab " + sheetToLoad + " not found, skipping..." );
			return;
		}
		logger.debug( "Loading Sheet: " + sheetToLoad );

		SheetConfig sc = new SheetConfig( lSheet );
		Map<String, Integer> properties = sc.getPropertyColumns();

		LoadingSheetData lsd = ( sc.isRelationSheet()
				? LoadingSheetData.relsheet( sheetToLoad, sc.getSubjectType(),
						sc.getObjectType(), sc.getRelationName() )
				: LoadingSheetData.nodesheet( sheetToLoad, sc.getSubjectType() ) );
		lsd.addProperties( properties.keySet() );

		id.add( lsd );

		if ( logger.isDebugEnabled() ) {
			int props = sc.getPropertyNames().size();
			logger.debug( "There are " + lSheet.getLastRowNum()
					+ ( sc.isRelationSheet() ? " relationships" : " entities" )
					+ " to create, each with " + props + " propert"
					+ ( 1 == props ? "y" : "ies" ) + " to load." );
		}

		int counter = 0;
		for ( int rowIndex = 1; rowIndex <= lSheet.getLastRowNum(); rowIndex++ ) {
			XSSFRow thisRow = lSheet.getRow( rowIndex );
			if ( null == thisRow ) {
				logger.warn( "skipping row " + rowIndex + " (doesn't exist?)" );
				continue;
			}

			XSSFCell instanceSubjectNodeCell = thisRow.getCell( 1 );
			if ( cellIsEmpty( instanceSubjectNodeCell ) ) {
				logger.warn( "skipping row " + rowIndex + " (no instance name)" );
				continue;
			}

			instanceSubjectNodeCell.setCellType( XSSFCell.CELL_TYPE_STRING );

			String subjectName = instanceSubjectNodeCell.getStringCellValue();

			Map<String, Value> props = getPropertyValues( thisRow, sc, properties, id );

			if ( sc.isRelationSheet() ) {
				// relationship sheets need the object's name as well as the subject's

				XSSFCell instanceObjectNodeCell = thisRow.getCell( 2 );
				if ( !cellIsEmpty( instanceObjectNodeCell ) ) {
					instanceObjectNodeCell.setCellType( XSSFCell.CELL_TYPE_STRING );
					String objectName = instanceObjectNodeCell.getStringCellValue();

					//createRelationship( subjectType, sc.getObjectType(), subjectName,
					//		objectName, sc.getRelationName(), props, rc );
					lsd.add( subjectName, objectName, props );
				}
			}
			else {
				//addNodeWithProperties( subjectType, subjectName, props, rc );
				lsd.add( subjectName, props );
			}

			counter++;
		}

		logger.debug( "Done processing: " + sheetToLoad );
		if ( lSheet.getLastRowNum() != counter ) {
			logger.warn( "only loaded " + counter + " of "
					+ lSheet.getLastRowNum() + " rows" );
		}
	}

	/**
	 * A class to encapsulate information about a worksheet
	 */
	public class SheetConfig {

		private final Map<String, Integer> proplkp = new HashMap<>();
		private final boolean isrel;
		private final String subjectType;
		private final String objectType;
		private final String relationName;

		public SheetConfig( XSSFSheet wk ) {
			XSSFRow firstRow = wk.getRow( 0 );

			// determine if relationship or property sheet
			String sheetType = firstRow.getCell( 0 ).getStringCellValue();
			subjectType = firstRow.getCell( 1 ).getStringCellValue();

			int lastpropcol = firstRow.getLastCellNum();
			int firstpropcol;

			isrel = "Relation".equalsIgnoreCase( sheetType );
			if ( isrel ) {
				objectType = firstRow.getCell( 2 ).getStringCellValue();
				// if relationship, properties start at column 2
				firstpropcol = 3;
				XSSFRow secondRow = wk.getRow( 1 );
				relationName = secondRow.getCell( 0 ).getStringCellValue();
			}
			else {
				objectType = "";
				relationName = "";
				firstpropcol = 2;
			}

			// determine property names for the relationship or node
			// colIndex starts at currentColumn+1 since if relationship, 
			// the object node name is in the second column
			for ( int i = firstpropcol; i < lastpropcol; i++ ) {
				// lastpropcol is sometimes bogus, it may be extend past
				// last property.  This can happen when invisible text
				// appears in a column header, unknown to the user.
				// So we check to make sure the header cell has content
				// and stop processing headers when the first empty one
				// is found.
				XSSFCell cell = firstRow.getCell( i );
				if ( cell == null ) {
					break;
				}
				proplkp.put( cell.getStringCellValue(), i );
			}
		}

		public boolean isRelationSheet() {
			return isrel;
		}

		public int getPropertyColumn( String name ) {
			return proplkp.get( name );
		}

		public Collection<String> getPropertyNames() {
			return proplkp.keySet();
		}

		public Map<String, Integer> getPropertyColumns() {
			return proplkp;
		}

		public String getSubjectType() {
			return subjectType;
		}

		public String getObjectType() {
			return objectType;
		}

		public String getRelationName() {
			return relationName;
		}
	}
}
