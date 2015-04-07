package gov.va.semoss.ui.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import gov.va.semoss.util.DefaultIcons;
import gov.va.semoss.util.Utility;
import javax.swing.Icon;

/**
 * Action to save a Grid or Raw Grid to a CSV or tab-delimited text file, or to
 * Excel.
 *
 * @author Thomas
 *
 */
public class SaveGridAction extends AbstractAction {

	private static final Logger logger = Logger.getLogger( SaveGridAction.class );
	private static final long serialVersionUID = 3476209795433405175L;
	private JTable table = new JTable();
	private String defaultFileName = "";

	public SaveGridAction() {
		this( "Save Grid", DefaultIcons.defaultIcons.get( DefaultIcons.SAVE ) );
	}

	public SaveGridAction( String name, Icon icon ) {
		super( name, icon );
		putValue( AbstractAction.SHORT_DESCRIPTION, "Save Current Grid" );
	}

	/**
	 * Exposes a setter for the JTable to display Grid or Raw Grid data.
	 *
	 * @param table -- (JTable) Display for grid data.
	 */
	public void setTable( JTable table ) {
		this.table = table;
	}

	/**
	 * Exposes a setter for the default file name to appear in the "Save Grid"
	 * dialog, when it is first opened.
	 *
	 * @param defaultFileName -- (String) A default file name.
	 */
	public void setDefaultFileName( String defaultFileName ) {
		this.defaultFileName = defaultFileName;
	}

	@Override
	public void actionPerformed( ActionEvent av ) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setSelectedFile( new File( defaultFileName ) );
		fileChooser.setAcceptAllFileFilterUsed( false );
		FileFilter csvFilter = new FileNameExtensionFilter( "Text (Comma Delimited) (*.csv)", "csv" );
		FileFilter tsvFilter = new FileNameExtensionFilter( "Text (Tab Delimited) (*.txt)", "txt" );
		FileFilter xlsxFilter = new FileNameExtensionFilter( "Excel Workbook (*.xlsx)", "xlsx" );
		fileChooser.addChoosableFileFilter( csvFilter );
		fileChooser.addChoosableFileFilter( tsvFilter );
		fileChooser.addChoosableFileFilter( xlsxFilter );
		fileChooser.setFileFilter( xlsxFilter );

		int returnVal = fileChooser.showSaveDialog( null );

		if ( returnVal == JFileChooser.APPROVE_OPTION ) {
			File file = fileChooser.getSelectedFile();

			//Make sure that the file name has the selected extension:
			String fileName = file.getAbsolutePath();
			String extension = "." + ( (FileNameExtensionFilter) fileChooser.getFileFilter() ).getExtensions()[0];
			if ( !fileName.endsWith( extension ) ) {
				file = new File( fileName + extension );
			}
			if ( extension.equals( ".csv" ) ) {
				if ( saveInTxtFormat( file, "," ) ) {
					Utility.showExportMessage( null, "Text (Comma Delimited) file saved to " + file,
							"Save Success", file );
				}
				else {
					JOptionPane.showMessageDialog( null, "Text (Comma Delimited) file failed to save.", "Save Failure",
							JOptionPane.ERROR_MESSAGE );
				}
			}
			if ( extension.equals( ".txt" ) ) {
				if ( saveInTxtFormat( file, "\t" ) ) {
					Utility.showExportMessage( null, "Text (Tab Delimited) file saved to " + file,
							"Save Success", file );
				}
				else {
					JOptionPane.showMessageDialog( null, "Text (Tab Delimited) file failed to save.", "Save Failure",
							JOptionPane.ERROR_MESSAGE );
				}
			}
			if ( extension.equals( ".xlsx" ) ) {
				if ( saveInExcelFormat( file ) ) {
					Utility.showExportMessage( null, "Excel Workbook saved to " + file,
							"Save Success", file );
				}
				else {
					JOptionPane.showMessageDialog( null, "Excel Workbook failed to save.",
							"Save Failure", JOptionPane.ERROR_MESSAGE );
				}
			}
		}
	}

	/**
	 * Saves a "file" to disk as a text file, where each item is delimited by
	 * "delimiter", which is usually a tab. Note: If the delimiter passed in is
	 * "," and a column heading or a cell value contains "," then that heading or
	 * value is double-quoted. Also, note that a comma delimited file will have a
	 * ".csv" extension, which may show up in Windows as an Excel icon. Opening
	 * this file directly will use the windows default character set. In order to
	 * assure that the UTF-8 character set is used in Excel 365, start a new
	 * worksheet, navigate to the "Data" tab, select "From Text", and in the
	 * wizard, be sure to choose "Unicode (UTF-8)" under "File Origin".
	 *
	 * @param file -- (File) The data to save to disk.
	 * @param delimeter -- (String) How the data is delimited.
	 *
	 * @return saveInTxtFormat -- (Boolean) Whether the save to disk succeeded.
	 */
	private Boolean saveInTxtFormat( File file, String delimeter ) {
		Boolean boolReturnValue = true;
		PrintWriter pw = null;

		try {
			pw = new PrintWriter( file, "UTF-8" );
			for ( int col = 0; col < table.getColumnCount(); col++ ) {
				String header = table.getColumnName( col );
				String tempDelimeter = delimeter;
				if ( col == table.getColumnCount() - 1 ) {
					tempDelimeter = "";
				}
				if ( delimeter.equals( "," ) ) {
					if ( header.contains( "," ) ) {
						pw.print( "\"" + header + "\"" + tempDelimeter );
					}
					else {
						pw.print( header + tempDelimeter );
					}
				}
				else {
					pw.print( header + tempDelimeter );
				}
			}
			pw.println( "" );
			for ( int i = 0; i < table.getRowCount(); i++ ) {
				for ( int j = 0; j < table.getColumnCount(); j++ ) {
					String value = table.getValueAt( i, j ).toString();
					String tempDelimeter = delimeter;
					if ( j == table.getColumnCount() - 1 ) {
						tempDelimeter = "";
					}
					if ( delimeter.equals( "," ) ) {
						if ( value.contains( "," ) ) {
							pw.print( "\"" + value + "\"" + tempDelimeter );
						}
						else {
							pw.print( value + tempDelimeter );
						}
					}
					else {
						pw.print( value + tempDelimeter );
					}
				}
				pw.println( "" );
			}
			logger.debug( "TXT file saved ok." );

		}
		catch ( IOException e ) {
			boolReturnValue = false;
			logger.error( "TXT file save failed: " + e );
			//Mark heavy-weight objects for garbage collection:   
		}
		finally {
			if ( pw != null ) {
				pw.close();
				pw = null;
			}
		}
		return boolReturnValue;
	}

	/**
	 * Saves a "file" to disk as an Excel Workbook.
	 *
	 * @param file -- (File) The data to save to disk.
	 *
	 * @return saveInExcelFormat -- (Boolean) Whether the save to disk succeeded.
	 */
	private Boolean saveInExcelFormat( File file ) {
		Boolean boolReturnValue = true;
		XSSFWorkbook wb = null;
		FileOutputStream newExcelFile = null;

		try {
			wb = new XSSFWorkbook();
			XSSFSheet gridSheet = wb.createSheet( "Grid Data" );

			//Write column headings:
			XSSFRow RowHeader = gridSheet.createRow( 0 );
			for ( int col = 0; col < table.getColumnCount(); col++ ) {
				XSSFCell CellHeader = RowHeader.createCell( col );
				CellHeader.setCellValue( table.getColumnName( col ) );
			}
			//Write rows of data:
			for ( int i = 0; i < table.getRowCount(); i++ ) {
				XSSFRow RowBody = gridSheet.createRow( i + 1 );

				for ( int j = 0; j < table.getColumnCount(); j++ ) {
					XSSFCell CellBody = RowBody.createCell( j );
					CellBody.setCellValue( table.getValueAt( i, j ).toString() );
				}
			}
			//Create the Excel file:
			newExcelFile = new FileOutputStream( file.getAbsolutePath() );
			wb.write( newExcelFile );

		}
		catch ( Exception e ) {
			boolReturnValue = false;
			logger.error( e );
			//Mark heavy-weight objects for garbage collection:   
		}
		finally {
			wb = null;
			newExcelFile = null;
		}
		return boolReturnValue;
	}

}//End class, "SaveGridAction"

