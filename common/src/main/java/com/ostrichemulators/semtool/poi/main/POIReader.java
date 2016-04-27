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
package com.ostrichemulators.semtool.poi.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openrdf.model.ValueFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Loading data into SEMOSS using Microsoft Excel Loading Sheet files
 */
public class POIReader implements ImportFileReader {

	private static final Logger logger = Logger.getLogger( POIReader.class );
	private boolean keepLoadInMemory = false;

	public static ImportData readNonloadingSheet( File file ) throws IOException {
		ImportData d
				= readNonloadingSheet( new XSSFWorkbook( new FileInputStream( file ) ) );
		d.getMetadata().setSourceOfData( new URIImpl( file.toURI().toString() ) );
		return d;
	}

	public static ImportData readNonloadingSheet( Workbook workbook ) {
		ImportData id = new ImportData();

		int sheets = workbook.getNumberOfSheets();
		for ( int sheetnum = 0; sheetnum < sheets; sheetnum++ ) {
			Sheet sheet = workbook.getSheetAt( sheetnum );
			String sheetname = workbook.getSheetName( sheetnum );

			// we need to shoehorn the arbitrary data from a spreadsheet into our
			// ImportData class, which has restrictions on the data...we're going
			// to do it by figuring out the row with the most columns, and then
			// naming all the columns with A, B, C...AA, AB...
			// then load everything as if it was plain data
			// first, figure out our max number of columns
			int rows = sheet.getLastRowNum();
			int maxcols = Integer.MIN_VALUE;
			for ( int r = 0; r <= rows; r++ ) {
				Row row = sheet.getRow( r );
				if ( null != row ) {
					int cols = (int) row.getLastCellNum();
					if ( cols > maxcols ) {
						maxcols = cols;
					}
				}
			}

			// second, make "properties" for each column
			LoadingSheetData nlsd = new LoadingSheetData( sheetname, "A" );
			for ( int c = 1; c < maxcols; c++ ) {
				nlsd.addProperty( Integer.toString( c ) );
			}

			// lastly, fill the sheets
			ValueFactory vf = new ValueFactoryImpl();
			for ( int r = 0; r <= rows; r++ ) {
				Row row = sheet.getRow( r );
				if ( null != row ) {
					Map<String, Value> propmap = new HashMap<>();

					int lastpropcol = row.getLastCellNum();
					for ( int c = 1; c <= lastpropcol; c++ ) {
						String val = getString( row.getCell( c ) );
						if ( !val.isEmpty() ) {
							propmap.put( Integer.toString( c ), vf.createLiteral( val ) );
						}
					}

					nlsd.add( getString( row.getCell( 0 ) ), propmap );
				}
			}

			if ( !nlsd.isEmpty() ) {
				id.add( nlsd );
			}
		}

		return id;
	}

	@Override
	public ImportMetadata getMetadata( File file ) throws IOException, ImportValidationException {
		logger.debug( "getting metadata from file: " + file );
		LowMemXlsReader reader = null;
		try {
			reader = new LowMemXlsReader( file );
			ImportMetadata data = reader.getMetadata();
			data.setSourceOfData( new URIImpl( file.toURI().toString() ) );
			return data;
		}
		finally {
			if ( null != reader ) {
				reader.release();
			}
		}
	}

	@Override
	public ImportData readOneFile( File file ) throws IOException, ImportValidationException {
		logger.debug( "loading data from file: " + file );
		LowMemXlsReader rdr = null;
		try {
			rdr = new LowMemXlsReader( file );
			rdr.keepSheetDataInMemory( keepLoadInMemory );
			ImportData d = rdr.getData();

			d.getMetadata().setSourceOfData( new URIImpl( file.toURI().toString() ) );
			logger.debug( "finished reading file: " + file );
			return d;
		}
		finally {
			if ( null != rdr ) {
				rdr.release();
			}
		}
	}

	@Override
	public void keepLoadInMemory( boolean b ) {
		keepLoadInMemory = b;
	}

	/**
	 * Always return a non-null string (will be "" for null cells).
	 *
	 * @param cell
	 * @return
	 */
	private static String getString( Cell cell ) {
		if ( null == cell ) {
			return "";
		}

		switch ( cell.getCellType() ) {
			case Cell.CELL_TYPE_NUMERIC:
				return Double.toString( cell.getNumericCellValue() );
			case Cell.CELL_TYPE_BOOLEAN:
				return Boolean.toString( cell.getBooleanCellValue() );
			case Cell.CELL_TYPE_FORMULA:
				return cell.getCellFormula();
			default:
				return cell.getStringCellValue();
		}
	}
}
