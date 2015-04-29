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

import gov.va.semoss.poi.main.ImportValidationException.ErrorType;
import gov.va.semoss.poi.main.LoadingSheetData.LoadingNodeAndPropertyValues;
import gov.va.semoss.rdf.engine.util.EngineLoader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import gov.va.semoss.util.MultiMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Loading data into SEMOSS using Microsoft Excel Loading Sheet files
 */
public class POIReader implements ImportFileReader {

	private static final Logger logger = Logger.getLogger( POIReader.class );

	private static final String METADATA = "Metadata";
	private static final String USUAL = "Usual";
	private static final String LOADER = "Loader";
	private static final String RELATION = "Relation";
	private static final String NODE = "Node";
	private static final String COMMENTSTART = "#";

	private static enum SheetType {

		METADATA, NODE, RELATION, LOADER, UNKNOWN, UNSPECIFIED, EMPTY
	};

	private ImportData readNonloadingSheet( Workbook workbook ) {
		ImportData id = new ImportData();

		int sheets = workbook.getNumberOfSheets();
		for ( int sheetnum = 0; sheetnum < sheets; sheetnum++ ) {
			Sheet sheet = workbook.getSheetAt( sheetnum );
			Row firstRow = sheet.getRow( 0 );

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
				Row row = sheet.getRow( r );
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
	public ImportMetadata getMetadata( File file ) throws IOException, ImportValidationException {
		logger.debug( "getting metadata from file: " + file );
		final Workbook workbook = new XSSFWorkbook( new FileInputStream( file ) );
		workbook.setMissingCellPolicy( Row.RETURN_BLANK_AS_NULL );

		ImportData data = new ImportData();

		for ( String name : categorizeSheets( workbook ).getNN( SheetType.METADATA ) ) {
			Sheet metadataSheet = workbook.getSheet( name );
			loadMetadata( metadataSheet, data );
		}

		return data.getMetadata();
	}

	@Override
	public ImportData readOneFile( File file ) throws IOException, ImportValidationException {
		return read( new XSSFWorkbook( new FileInputStream( file ) ) );
	}

	public ImportData read( Workbook workbook ) throws ImportValidationException {
		workbook.setMissingCellPolicy( Row.RETURN_BLANK_AS_NULL );

		MultiMap<SheetType, String> typeToSheetNameLkp = categorizeSheets( workbook );

		ImportData data = new ImportData();
		EngineLoader.initNamespaces( data );

		// we have sheets without a specified type, so open like a regular spreadsheet
		if ( !typeToSheetNameLkp.getNN( SheetType.UNKNOWN ).isEmpty() ) {
			logger.warn( "Trying to import sheet with no loader tab" );
			return readNonloadingSheet( workbook );
		}

		for ( String sheetname : typeToSheetNameLkp.getNN( SheetType.METADATA ) ) {
			Sheet metadataSheet = workbook.getSheet( sheetname );
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

	private MultiMap<SheetType, String> categorizeSheets( Workbook workbook )
			throws ImportValidationException {
		MultiMap<SheetType, String> typeToSheetNameLkp = new MultiMap<>();
		// figure out what this spreadsheet contains...if we have a "Loader" tab,
		// only worry about those sheets named in it. Otherwise, go through every
		// sheet and figure out what we have

		Sheet lSheet = workbook.getSheet( "Loader" );
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
			typeToSheetNameLkp.add( SheetType.LOADER, LOADER );

			Map<String, SheetType> fromloading = categorizeFromLoadingSheet( lSheet );

			if ( fromloading.isEmpty() ) {
				throw new ImportValidationException( ErrorType.MISSING_DATA,
						"No data to process" );
			}

			for ( Map.Entry<String, SheetType> en : fromloading.entrySet() ) {
				String name = en.getKey();
				SheetType loadertype = en.getValue();
				SheetType realtype = getSheetType( workbook, name );

				if ( SheetType.EMPTY != realtype ) {
					if ( SheetType.UNSPECIFIED != loadertype ) {
						if ( SheetType.METADATA == loadertype && SheetType.METADATA != realtype ) {
							throw new ImportValidationException( ErrorType.INVALID_TYPE,
									"Sheet " + name + " does not include \"Metadata\" keyword in cell A1" );
						}
						else if ( SheetType.METADATA == realtype && SheetType.METADATA != loadertype ) {
							throw new ImportValidationException( ErrorType.WRONG_TABTYPE,
									"Loader Sheet data type for " + name + " conflicts with sheet type" );
						}
					}

					typeToSheetNameLkp.add( realtype, name );
				}
			}
		}

		if ( 1 == typeToSheetNameLkp.keySet().size()
				&& typeToSheetNameLkp.containsKey( SheetType.METADATA ) ) {
			throw new ImportValidationException( ErrorType.MISSING_DATA,
					"No data to process" );
		}

		if ( typeToSheetNameLkp.getNN( SheetType.METADATA ).size() > 1 ) {
			throw new ImportValidationException( ErrorType.TOO_MUCH_DATA,
					"Too many metadata tabs in loading file" );
		}

		return typeToSheetNameLkp;
	}

	private static Map<String, SheetType> categorizeFromLoadingSheet( Sheet lSheet )
			throws ImportValidationException {
		Map<String, SheetType> map = new HashMap<>();
		Row header = lSheet.getRow( 0 );
		Cell a1 = header.getCell( 0 );
		Cell b1 = header.getCell( 1 );
		String a1val = getString( a1 );
		String b1val = getString( b1 );
		boolean mustHaveType = !isEmpty( b1 );

		if ( !"Sheet Name".equals( a1val ) ) {
			throw new ImportValidationException( ErrorType.MISSING_DATA, "Cell A1 must be \"Sheet Name\"" );
		}
		if ( mustHaveType && !"Type".equals( b1val ) ) {
			throw new ImportValidationException( ErrorType.MISSING_DATA, "Cell B1 must be \"Type\"" );
		}

		int rIndex = nextRow( lSheet, 0 );
		while ( -1 != rIndex ) {
			Row row = lSheet.getRow( rIndex );
			if ( row == null ) {
				continue;
			}

			Cell rowx = row.getCell( 2 );
			if ( !( isEmpty( rowx ) || isComment( rowx ) ) ) {
				throw new ImportValidationException( ErrorType.TOO_MUCH_DATA,
						"Too much data in row " + rIndex );
			}

			String sheetNameToLoad = getString( row.getCell( 0 ) );
			String sheetTypeToLoad = getString( row.getCell( 1 ) );

			if ( sheetNameToLoad.startsWith( COMMENTSTART ) ) {
				continue;
			}
			if ( sheetTypeToLoad.startsWith( COMMENTSTART ) ) {
				sheetTypeToLoad = "";
			}

			if ( sheetNameToLoad.isEmpty() && sheetTypeToLoad.isEmpty() ) {
				logger.debug( "empty row at " + ( rIndex + 1 ) + "...skipping rest of tab" );
				break;
			}

			if ( sheetNameToLoad.isEmpty() || ( sheetTypeToLoad.isEmpty() && mustHaveType ) ) {
				if ( sheetNameToLoad.isEmpty() ) {
					throw new ImportValidationException( ErrorType.MISSING_DATA,
							"No sheet name on row " + ( rIndex + 1 ) );
				}
				else {
					throw new ImportValidationException( ErrorType.MISSING_DATA,
							"No type specified for sheet " + sheetNameToLoad );
				}
			}
			if ( mustHaveType
					&& !( METADATA.equals( sheetTypeToLoad ) || USUAL.equals( sheetTypeToLoad ) ) ) {
				throw new ImportValidationException( ErrorType.INVALID_TYPE,
						"Invalid type specified: " + sheetTypeToLoad );
			}

			if ( mustHaveType ) {
				map.put( sheetNameToLoad, ( METADATA.equals( sheetTypeToLoad )
						? SheetType.METADATA : SheetType.UNKNOWN ) );
			}
			else {
				map.put( sheetNameToLoad, SheetType.UNSPECIFIED );
			}

			rIndex = nextRow( lSheet, rIndex );
		}

		return map;
	}

	private SheetType getSheetType( Workbook workbook, String sheetname )
			throws ImportValidationException {
		Sheet sheet = workbook.getSheet( sheetname );

		if ( null == sheet ) {
			throw new ImportValidationException( ErrorType.MISSING_DATA,
					"Missing sheet: " + sheetname );
		}

		Row row0 = sheet.getRow( 0 );
		int next = nextRow( sheet, 0 );
		Row row1 = sheet.getRow( next );
		if ( null == row0 || null == row1 ) {
			// make sure we have some "data" in this sheet
			return SheetType.EMPTY;
		}

		if ( getString( row1.getCell( 1 ) ).isEmpty() ) {
			// no data in the first data cell...assume we're empty
			return SheetType.EMPTY;
		}

		String type = getString( row0.getCell( 0 ) );

		switch ( type ) {
			case METADATA:
				return SheetType.METADATA;
			case RELATION:
				return SheetType.RELATION;
			case NODE:
				return SheetType.NODE;
			default:
				return SheetType.UNKNOWN;
		}
	}

	private void loadMetadata( Sheet metadataSheet, ImportData data )
			throws ImportValidationException {
		if ( metadataSheet == null ) {
			return;
		}

		ImportMetadata metas = data.getMetadata();
		metas.setLegacyMode( false );
		// we want to load the base uri first, data-namespace, schema-namespace,
		// prefixes, and finally triples. so read everything first, and load later
		String datanamespace = null;
		String schemanamespace = null;
		String baseuri = null;
		Map<String, String> namespaces = metas.getNamespaces();
		List<String[]> triples = new ArrayList<>();

		logger.debug( ( metadataSheet.getLastRowNum() + 1 )
				+ " metadata rows to interpret" );

		// read the data
		int i = nextRow( metadataSheet, -1 );
		while ( -1 != i ) {
			Row row = metadataSheet.getRow( i );
			if ( row == null ) {
				logger.warn( "skipping row: " + i + " (doesn't exist?)" );
				i = nextRow( metadataSheet, i );
				continue;
			}

			Cell cell1 = row.getCell( 1 );
			Cell cell2 = row.getCell( 2 );
			Cell cell3 = row.getCell( 3 );

			if ( isComment( cell1 ) || isComment( cell3 ) || isComment( cell2 ) ) {
				logger.warn( "skipping row: " + i + " (commented)" );
				i = nextRow( metadataSheet, i );
				continue;
			}

			if ( isEmpty( cell1 ) || isEmpty( cell3 ) ) {
				logger.warn( "skipping row: " + i + " (empty cell)" );
				i = nextRow( metadataSheet, i );
				continue;
			}

			String propName = cell1.getStringCellValue();
			String propValue = cell3.getStringCellValue();
			String propertyMiddleColumn = getString( cell2 );

			if ( "@schema-namespace".equals( propName ) ) {
				if ( null == schemanamespace ) {
					schemanamespace = propValue;
				}
				else {
					throw new ImportValidationException( ErrorType.TOO_MUCH_DATA,
							"Multiple @schema-namespace lines in Metadata sheet" );
				}
			}
			else if ( "@data-namespace".equals( propName ) ) {
				if ( null == datanamespace ) {
					datanamespace = propValue;
				}
				else {
					throw new ImportValidationException( ErrorType.TOO_MUCH_DATA,
							"Multiple @schema-namespace lines in Metadata sheet" );
				}
			}
			else if ( "@base".equals( propName ) ) {
				if ( null == baseuri ) {
					baseuri = propValue;
				}
				else {
					throw new ImportValidationException( ErrorType.TOO_MUCH_DATA,
							"Multiple @base lines in Metadata sheet" );
				}
			}
			else if ( "@prefix".equals( propName ) ) {
				namespaces.put( propertyMiddleColumn.replaceAll( ":$", "" ), propValue );
			}
			else {
				if ( !propertyMiddleColumn.isEmpty() ) {
					triples.add( new String[]{ propName, propertyMiddleColumn, propValue } );
				}
			}

			i = nextRow( metadataSheet, i );
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

			try {
				metas.add( triple[0], triple[1], triple[2] );
			}
			catch ( Exception e ) {
				throw new ImportValidationException( ErrorType.INVALID_DATA, e );
			}
		}
	}

	private Map<String, Value> getPropertyValues( Row row, SheetConfig props,
			Map<String, Integer> properties, ImportData id ) {
		Map<String, Value> propHash = new HashMap<>();

		ValueFactory vf = new ValueFactoryImpl();
		for ( Map.Entry<String, Integer> prop : properties.entrySet() ) {
			String propName = prop.getKey();
			Cell cellValue = row.getCell( prop.getValue() );

			if ( !isEmpty( cellValue ) ) {
				if ( isComment( cellValue ) ) {
					continue;
				}

				if ( cellValue.getCellType() != Cell.CELL_TYPE_NUMERIC ) {
					cellValue.setCellType( Cell.CELL_TYPE_STRING );
					propHash.put( propName,
							EngineLoader.getRDFStringValue( cellValue.getStringCellValue(),
									id.getMetadata().getNamespaces(), vf ) );
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

	private static boolean isEmpty( Cell cell ) {
		return ( null == cell );
	}

	/**
	 * Always return a non-null string (will be "" for null cells).
	 *
	 * @param cell
	 * @return
	 */
	private static String getString( Cell cell ) {
		return ( isEmpty( cell ) ? "" : cell.getStringCellValue() );
	}

	private static boolean isComment( Cell cell ) {
		if ( isEmpty( cell ) ) {
			return false;
		}

		if ( Cell.CELL_TYPE_NUMERIC == cell.getCellType() ) {
			return false;
		}

		return getString( cell ).startsWith( COMMENTSTART );
	}

	/**
	 * Gets the next non-comment row from the worksheet, or -1 if there isn't one.
	 * This function is useful in the case that a sheet contains a comment
	 * starting in column 1
	 *
	 * @param sheet the sheet to scan
	 * @param lastrow the last row we found
	 * @return the next row number
	 */
	private static int nextRow( Sheet sheet, int currentRow ) {
		Row row = null;
		if ( currentRow < 0 ) {
			currentRow = -1; // start with row 0 in the following loop
		}

		while ( null != ( row = sheet.getRow( ++currentRow ) ) ) {
			if ( !isComment( row.getCell( 0 ) ) ) {
				return currentRow;
			}
		}
		return -1;
	}

	private void loadSheet( String sheetToLoad, Workbook workbook, ImportData id ) {
		Sheet lSheet = workbook.getSheet( sheetToLoad );
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
		int rowIndex = nextRow( lSheet, 0 );
		while ( -1 != rowIndex ) {
			Row thisRow = lSheet.getRow( rowIndex );
			if ( null == thisRow ) {
				logger.warn( "skipping row " + rowIndex + " (doesn't exist?)" );
				continue;
			}

			Cell instanceSubjectNodeCell = thisRow.getCell( 1 );
			if ( isEmpty( instanceSubjectNodeCell ) ) {
				logger.warn( "skipping row " + rowIndex + " (no instance name)" );
				continue;
			}

			instanceSubjectNodeCell.setCellType( Cell.CELL_TYPE_STRING );

			String subjectName = instanceSubjectNodeCell.getStringCellValue();

			Map<String, Value> props = getPropertyValues( thisRow, sc, properties, id );

			if ( sc.isRelationSheet() ) {
				// relationship sheets need the object's name as well as the subject's

				Cell instanceObjectNodeCell = thisRow.getCell( 2 );
				if ( !isEmpty( instanceObjectNodeCell ) ) {
					instanceObjectNodeCell.setCellType( Cell.CELL_TYPE_STRING );
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
			rowIndex = nextRow( lSheet, rowIndex );
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

		public SheetConfig( Sheet wk ) {
			Row firstRow = wk.getRow( 0 );

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
				Row secondRow = wk.getRow( 1 );
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
				Cell cell = firstRow.getCell( i );
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
