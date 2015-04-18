package gov.va.semoss.ui.components;

import gov.va.semoss.poi.main.FileLoadingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openrdf.repository.RepositoryException;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.util.EngineLoader;
import gov.va.semoss.rdf.engine.util.EngineManagementException;
import gov.va.semoss.rdf.engine.util.EngineUtil;
import gov.va.semoss.rdf.main.ImportRDBMSProcessor;
import gov.va.semoss.util.DIHelper;

public class ImportDataProcessor {

	private static final Logger logger 
      = Logger.getLogger( ImportDataProcessor.class );

	public enum IMPORT_METHOD {CREATE_NEW, ADD_TO_EXISTING, OVERRIDE, RDBMS};
	public enum IMPORT_TYPE {CSV, NLP, EXCEL};

	private String baseDirectory;
  private final List<File> filesToImport = new ArrayList<>();

  public ImportDataProcessor(){
  }
  
  public ImportDataProcessor( String basedir ){
    setBaseDirectory( basedir );
  }
  
	public final void setBaseDirectory(String baseDirectory){
		this.baseDirectory = baseDirectory;
	}
  
  public String getBaseDir(){
    return baseDirectory;
  }
  
  /**
   * Determines the files to import based on a ;-delimited string
   * @param filenames 
   */
  public void setFilesToImport( String filenames ) {
    filesToImport.clear();
    if ( null == filenames || filenames.isEmpty() ) {
      return;
    }

    for ( String file : filenames.split( ";" ) ) {
      filesToImport.add( new File( file ) );
    }
  }

  public void setFilesToImport( Collection<File> files ) {
    filesToImport.clear();
    filesToImport.addAll( files );
  }

	//This method will take in all possible required information
	//After determining the desired import method and type, process with the subset of information that that processing requires.
	public boolean runProcessor(IMPORT_METHOD importMethod, IMPORT_TYPE importType, 
      String customBaseURI, String newDBname, String mapFile, String dbPropFile,
      String questionFile, String repoName){
		boolean success = false;
    logger.warn( "this class has been dramatically refactored. check the results." );

    if ( importMethod == IMPORT_METHOD.CREATE_NEW ) {
      try {
        File smss = EngineUtil.createNew( new File( baseDirectory ), newDBname, 
            customBaseURI, dbPropFile, mapFile, questionFile, filesToImport, 
            true, true, true, null );
        EngineUtil.getInstance().mount( smss, true );
        success = true;
      }
      catch ( IOException | EngineManagementException ioe ) {
        logger.error( ioe, ioe );
        success = false;
      }
    }
    else if ( importMethod == IMPORT_METHOD.ADD_TO_EXISTING ) {
      success = processAddToExisting( importType, customBaseURI,
          DIHelper.getInstance().getEngine( repoName ) );
    }
    else if ( importMethod == IMPORT_METHOD.OVERRIDE ) {
      success = processOverride( importType, customBaseURI,
          DIHelper.getInstance().getEngine( repoName ) );
    }
    return success;
  }

  public boolean processAddToExisting( IMPORT_TYPE importType,
      String customBaseURI, IEngine eng ) {
    boolean success = true;
    logger.warn( "this class has been dramatically refactored. check the results." );
    EngineLoader el = new EngineLoader();
    try{
      el.loadToEngine( filesToImport, eng, true, null );
    }
    catch( FileLoadingException | RepositoryException | IOException e ){
      logger.error( e,e );
      success = false;
    }
		finally {
			el.release();
		}

    return success;
  }
  
  public static void clearEngine( IEngine engine, Collection<File> filesToCheck ){
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
  
	/**
	 * Executes the deleting and loading of files.
   * @param importType
	 * @param customBaseURI String
   * @param engine
   * @return 
	 */
	public boolean processOverride(IMPORT_TYPE importType, String customBaseURI, 
      IEngine engine) {
    logger.warn( "this class has been dramatically refactored. check the results." );
		if(importType == IMPORT_TYPE.EXCEL){
      clearEngine( engine, filesToImport );
      return processAddToExisting( importType, customBaseURI, engine );
		}
		else{
			//currently only processing override for excel
			return false;
		}
	}

	/**
	 * Deletes all the files created from a run. Use this function when cleaning
   * up after an error
	 * @param files the mapping of files. really, on the values are used.
	 */
	public void deleteAll(Map<String,File> files) {
    for( File f : files.values() ){
      FileUtils.deleteQuietly( f );
    }
  }
	
  public boolean processNewRDBMS( String customBaseURI, String repoName,
      String type, String url, String username, char[] password ) {
    boolean success = false;

    ImportRDBMSProcessor proc = new ImportRDBMSProcessor( customBaseURI, 
        filesToImport, repoName, type, url, username, password );
    File propFile;
    if ( proc.checkConnection( type, url, username, password ) ) {
      propFile = proc.setUpRDBMS();
    }
    else {
      return false;
    }

    File newProp = new File( propFile.getParent(),
        propFile.getName().replace( "temp", "smss" ) );
    try {
      FileUtils.copyFile( propFile, newProp );
      success = true;
    }
    catch ( IOException e ) {
      logger.error( e );
    }

    return success;
  }
}
