package gov.va.semoss.ui.actions;

import gov.va.semoss.ui.components.FileBrowsePanel;
import gov.va.semoss.ui.components.PlaySheetFrame;
import gov.va.semoss.ui.components.playsheets.PlaySheetCentralComponent;
import gov.va.semoss.util.Utility;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.Icon;

/**
 * Action to save a Grid or Raw Grid to a CSV or tab-delimited text file, or to
 * Excel.
 *
 * @author Thomas
 *
 */
public class SaveAllGridAction extends AbstractAction {

	private static final Logger log = Logger.getLogger( SaveAllGridAction.class );
	private PlaySheetFrame psf;

	public SaveAllGridAction() {
		super( "Save All", DbAction.getIcon( "save_alldiskette1" ) );
		putValue( AbstractAction.SHORT_DESCRIPTION, "Save All Grids" );
	}

	public SaveAllGridAction( String name, Icon icon ) {
		super( name, icon );
	}

	public void setPlaySheetFrame( PlaySheetFrame p ) {
		psf = p;
	}

	@Override
	public void actionPerformed( ActionEvent av ) {
		Preferences prefs = Preferences.userNodeForPackage( getClass() );
		File dir = FileBrowsePanel.getLocationForEmptyPref( prefs, "lastsavedir" );
		File expfile = new File( dir, Utility.getSaveFilename( "Grids", "xlsx" ) );

		JFileChooser fileChooser = new JFileChooser( dir );
		fileChooser.setSelectedFile( expfile );
		FileFilter xlsxFilter
				= new FileNameExtensionFilter( "Excel Workbook (*.xlsx)", "xlsx" );
		fileChooser.addChoosableFileFilter( xlsxFilter );
		fileChooser.setFileFilter( xlsxFilter );

		int returnVal = fileChooser.showSaveDialog( psf );

		if ( JFileChooser.APPROVE_OPTION == returnVal ) {
			File file = fileChooser.getSelectedFile();
			prefs.put( "lastsavedir", file.getParent() );

			//Make sure that the file name has the selected extension:
			String fileName = file.getAbsolutePath();
			String extension = ".xlsx";
			if ( !fileName.endsWith( extension ) ) {
				file = new File( fileName + extension );
			}

			try {
				save( file );
				Utility.showExportMessage( null, "Excel Workbook saved to " + file,
						"Save Success", file );
			}
			catch ( IOException | HeadlessException e ) {
				JOptionPane.showMessageDialog( null, "Excel Workbook failed to save.",
						"Save Failure", JOptionPane.ERROR_MESSAGE );
			}
		}
	}

	/**
	 * Saves a "file" to disk as an Excel Workbook.
	 *
	 * @param file -- (File) The data to save to disk.
	 *
	 * @return saveInExcelFormat -- (Boolean) Whether the save to disk succeeded.
	 */
	private void save( File file ) throws IOException {

		try ( OutputStream os = new FileOutputStream( file ) ) {
			XSSFWorkbook wb = new XSSFWorkbook();

			for ( PlaySheetCentralComponent pscc : psf.getPlaySheets() ) {
				List<Object[]> table = pscc.getTabularData();

				if ( null != table ) {
					// we have tabular data to save
					Collection<String> heads = pscc.getHeaders();
					XSSFSheet gridSheet = wb.createSheet();

					int r = 0;
					int c = 0;

					//Write column headings
					XSSFRow RowHeader = gridSheet.createRow( r++ );
					for ( String h : heads ) {
						XSSFCell CellHeader = RowHeader.createCell( c++ );
						CellHeader.setCellValue( h );
					}

					//Write all the data
					for ( Object[] row : table ) {
						XSSFRow RowBody = gridSheet.createRow( r++ );
						c = 0;
						for ( Object o : row ) {
							XSSFCell CellBody = RowBody.createCell( c++ );
							CellBody.setCellValue( null == o ? null : o.toString() );
						}
					}
				}
			}

			wb.write( os );
		}
	}
}
