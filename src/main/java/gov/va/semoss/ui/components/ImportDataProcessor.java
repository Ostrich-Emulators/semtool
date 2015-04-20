package gov.va.semoss.ui.components;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import gov.va.semoss.rdf.engine.api.IEngine;

public class ImportDataProcessor {

	private static final Logger logger = Logger.getLogger( ImportDataProcessor.class );

	public static void clearEngine( IEngine engine, Collection<File> filesToCheck ) {
		for ( File file : filesToCheck ) {
			try {
				XSSFWorkbook book = new XSSFWorkbook( new FileInputStream( file ) );
				XSSFSheet lSheet = book.getSheet( "Loader" );
				int lastRow = lSheet.getLastRowNum();

				List<String> nodes = new ArrayList<>();
				List<String[]> relationships = new ArrayList<>();
				for ( int rIndex = 1; rIndex <= lastRow; rIndex++ ) {
					XSSFRow sheetNameRow = lSheet.getRow( rIndex );
					XSSFCell cell = sheetNameRow.getCell( 0 );
					XSSFSheet sheet = book.getSheet( cell.getStringCellValue() );

					XSSFRow row = sheet.getRow( 0 );
					String sheetType = "";
					if ( row.getCell( 0 ) != null ) {
						sheetType = row.getCell( 0 ).getStringCellValue();
					}
					if ( "Node".equalsIgnoreCase( sheetType ) ) {
						if ( row.getCell( 1 ) != null ) {
							nodes.add( row.getCell( 1 ).getStringCellValue() );
						}
					}
					if ( "Relation".equalsIgnoreCase( sheetType ) ) {
						String subject;
						String object;
						String relationship = "";
						if ( row.getCell( 1 ) != null && row.getCell( 2 ) != null ) {
							subject = row.getCell( 1 ).getStringCellValue();
							object = row.getCell( 2 ).getStringCellValue();

							row = sheet.getRow( 1 );
							if ( row.getCell( 0 ) != null ) {
								relationship = row.getCell( 0 ).getStringCellValue();
							}

							relationships.add( new String[]{ subject, relationship, object } );
						}
					}
				}
				String deleteQuery;
				UpdateProcessor proc = new UpdateProcessor();
				proc.setEngine( engine );

				int numberNodes = nodes.size();
				if ( numberNodes > 0 ) {
					for ( String node : nodes ) {
						deleteQuery = "DELETE {?s ?p ?prop. ?s ?x ?y} WHERE { {";
						deleteQuery += "SELECT ?s ?p ?prop ?x ?y WHERE { {?s a <http://semoss.org/ontologies/Concept/";
						deleteQuery += node;
						deleteQuery += "> ;} {?s ?x ?y} MINUS {?x <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> <http://semoss.org/ontologies/Relation> ;} ";
						deleteQuery += "OPTIONAL{ {?p a <http://semoss.org/ontologies/Relation/Contains> ;} {?s ?p ?prop ;} } } } ";
						deleteQuery += "}";

						proc.setQuery( deleteQuery );
						logger.debug( deleteQuery );
						proc.processQuery();
					}
				}

				int numberRelationships = relationships.size();
				if ( numberRelationships > 0 ) {
					for ( String[] rel : relationships ) {
						deleteQuery = "DELETE {?in ?relationship ?out. ?relationship ?contains ?prop} WHERE { {";
						deleteQuery += "SELECT ?in ?relationship ?out ?contains ?prop WHERE { "
								+ "{?in a <http://semoss.org/ontologies/Concept/";
						deleteQuery += rel[0];
						deleteQuery += "> ;} ";

						deleteQuery += "{?out a <http://semoss.org/ontologies/Concept/";
						deleteQuery += rel[2];
						deleteQuery += "> ;} ";

						deleteQuery += "{?relationship <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> <http://semoss.org/ontologies/Relation/";
						deleteQuery += rel[1];
						deleteQuery += "> ;} {?in ?relationship ?out ;} ";
						deleteQuery += "OPTIONAL { {?relationship ?contains ?prop ;} } } } ";
						deleteQuery += "}";

						proc.setQuery( deleteQuery );
						logger.debug( deleteQuery );
						proc.processQuery();
					}
				}
			}
			catch ( Exception ex ) {
				logger.error( ex, ex );
			}
		}
	}
}
