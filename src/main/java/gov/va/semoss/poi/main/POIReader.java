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
import gov.va.semoss.util.Constants;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Loading data into SEMOSS using Microsoft Excel Loading Sheet files
 */
public class POIReader implements ImportFileReader {

	private static final Logger logger = Logger.getLogger( POIReader.class );

	private ImportData readNonloadingSheet( XSSFWorkbook workbook ) {
		ImportData id = new ImportData();
		ImportMetadata im = id.getMetadata();
//		im.setDataBuilder( getDataBuilder().toString() );
//		im.setSchemaBuilder( getSchemaBuilder().toString() );
//		im.setBase( getSchemaBuilder().build() );

		int sheets = workbook.getNumberOfSheets();
		for ( int sheetnum = 0; sheetnum < sheets; sheetnum++ ) {
			XSSFSheet sheet = workbook.getSheetAt( sheetnum );
			XSSFRow firstRow = sheet.getRow( 0 );

			String subjectType = firstRow.getCell( 0 ).getStringCellValue();
			NodeLoadingSheetData nlsd
					= new NodeLoadingSheetData( sheet.getSheetName(), subjectType );
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
	public ImportData readOneFile( File file ) throws IOException {
		final XSSFWorkbook workbook
				= new XSSFWorkbook( new FileInputStream( file ) );

		XSSFSheet lSheet = workbook.getSheet( "Loader" );

		if ( null == lSheet ) {
			logger.warn( "Trying to import sheet with no loader tab" );
			// there's no "Loader" sheet, so we don't have a LoadingSheet file
			return readNonloadingSheet( workbook );
		}

		ImportData data = new ImportData();
		AbstractFileReader.initNamespaces( data );

		String metadataSheetName = findMetadataSheetName( lSheet );
		if ( null == metadataSheetName ) {
			data.getMetadata().setLegacyMode( true );
		}
		else {
			XSSFSheet metadataSheet = workbook.getSheet( metadataSheetName );
			loadMetadata( metadataSheet, data );
		}

		// load the rest of the sheets
		for ( int rIndex = 1; rIndex <= lSheet.getLastRowNum(); rIndex++ ) {
			XSSFRow row = lSheet.getRow( rIndex );
			if ( row == null ) {
				continue;
			}

			XSSFCell cell0 = row.getCell( 0 );
			XSSFCell cell1 = row.getCell( 1 );
			if ( cell0 == null || cell1 == null ) {
				continue;
			}

			String sheetNameToLoad = cell0.getStringCellValue();
			String sheetTypeToLoad = cell1.getStringCellValue();
			if ( sheetNameToLoad.isEmpty() || sheetTypeToLoad.isEmpty() ) {
				continue;
			}

			if ( sheetTypeToLoad.contains( "Matrix" ) ) {
				// I don't know what this is
			}
			else if ( !sheetTypeToLoad.trim().equalsIgnoreCase( Constants.METADATA_SHEET_NAME ) ) {
				// we're skipping the METADATA sheet
				loadSheet( sheetNameToLoad, workbook, data );
			}
		}

		// rc.commit();
		return data;
	}

	private String findMetadataSheetName( XSSFSheet lSheet ) {
		for ( int rIndex = 1; rIndex <= lSheet.getLastRowNum(); rIndex++ ) {
			XSSFRow row = lSheet.getRow( rIndex );
			if ( row == null ) {
				continue;
			}

			XSSFCell cell0 = row.getCell( 0 );
			XSSFCell cell1 = row.getCell( 1 );
			if ( cell0 == null || cell1 == null ) {
				continue;
			}

			String sheetNameToLoad = cell0.getStringCellValue();
			String sheetTypeToLoad = cell1.getStringCellValue();
			if ( sheetNameToLoad.isEmpty() || sheetTypeToLoad.isEmpty() ) {
				continue;
			}

			if ( sheetTypeToLoad.trim().equalsIgnoreCase( Constants.METADATA_SHEET_NAME ) ) {
				return sheetNameToLoad;
			}
		}

		logger.debug( "no metadata sheet found" );
		return null;
	}

	private void loadMetadata( XSSFSheet metadataSheet, ImportData data ) {
		if ( metadataSheet == null ) {
			return;
		}

		data.getMetadata().setLegacyMode( false );

		logger.debug( ( metadataSheet.getLastRowNum() + 1 )
				+ " metadata rows to interpret" );
		ImportMetadata metas = data.getMetadata();
		for ( int i = 0; i <= metadataSheet.getLastRowNum(); i++ ) {
			XSSFRow row = metadataSheet.getRow( i );
			if ( row == null ) {
				logger.warn( "skipping row: " + i + " (doesn't exist?)" );
				continue;
			}

			XSSFCell cell0 = row.getCell( 0 );
			XSSFCell cell2 = row.getCell( 2 );
			if ( cellIsEmpty( cell0 ) || cellIsEmpty( cell2 ) ) {
				logger.warn( "skipping row: " + i + " (empty cell)" );
				continue;
			}

			cell0.setCellType( XSSFCell.CELL_TYPE_STRING );
			cell2.setCellType( XSSFCell.CELL_TYPE_STRING );

			String propName = cell0.getStringCellValue();
			String propValue = cell2.getStringCellValue();

			XSSFCell cell1 = row.getCell( 1 );
			String propertyMiddleColumn = ( cellIsEmpty( cell1 )
					? "" : cell1.getStringCellValue() );

			if ( "@schema-namespace".equals( propName ) ) {
				logger.debug( "setting schema namespace to " + propValue );
				metas.setSchemaBuilder( propValue );
			}
			else if ( "@data-namespace".equals( propName ) ) {
				logger.debug( "setting data namespaces to " + propValue );
				metas.setDataBuilder( propValue );
			}
			else if( "@base".equals( propName ) ){
				logger.debug( "setting base URI to " + propValue );
				ValueFactory vf = new ValueFactoryImpl();
				metas.setBase( vf.createURI( propValue ) );
			}
			else if ( "@prefix".equals( propName ) ) {
				logger.debug( "registering namespace: "
						+ propertyMiddleColumn + " => " + propValue );
				metas.setNamespace( propertyMiddleColumn, propValue );
			}
			else if ( "@triple".equals( propName ) ) {
				XSSFCell cell3 = row.getCell( 3 );
				if ( !cellIsEmpty( cell3 ) ) {
					logger.debug( "adding custom triple: "
							+ propertyMiddleColumn + " => " + propValue + " => " + cell3 );

					metas.add( new StatementImpl( getUriFromRawString( propertyMiddleColumn, data ),
							getUriFromRawString( propValue, data ),
							getUriFromRawString( cell3.getStringCellValue(), data ) ) );
				}
			}
			else if ( "@createModel".equals( propName ) ) {
				logger.debug( "setting autocreate model to: "
						+ Boolean.parseBoolean( propValue ) );
				try {
					metas.setAutocreateMetamodel( Boolean.parseBoolean( propValue ) );
				}
				catch ( Exception e ) {
					logger.warn( "@createModel flag set but without a boolean value in "
							+ "the third column. Couldnt parse boolean out of: " + propValue, e );
				}
			}
			else if ( !propName.startsWith( "@" ) ) {
				data.getMetadata().addExtra(
						getUriFromRawString( propName, data ), propValue );
			}
			else {
				logger.warn( "Metadata key " + propName + " not recognized." );
			}
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

	private boolean cellIsEmpty( XSSFCell cell ) {
		if ( null == cell || cell.getCellType() == XSSFCell.CELL_TYPE_BLANK
				|| cell.toString().isEmpty() ) {
			return true;
		}
		return false;
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

		NodeLoadingSheetData nlsd
				= new NodeLoadingSheetData( sheetToLoad, sc.getSubjectType(),
						properties.keySet() );
		RelationshipLoadingSheetData rlsd
				= new RelationshipLoadingSheetData( sheetToLoad, sc.getSubjectType(),
						sc.getObjectType(), sc.getRelationName(), properties.keySet() );
		if ( sc.isRelationSheet() ) {
			id.add( rlsd );
		}
		else {
			id.add( nlsd );
		}

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
					rlsd.add( subjectName, objectName, props );
				}
			}
			else {
				//addNodeWithProperties( subjectType, subjectName, props, rc );
				nlsd.add( subjectName, props );
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
				if( cell == null ) {
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
