package gov.va.semoss.ui.actions;

import gov.va.semoss.poi.main.XlsWriter;
import gov.va.semoss.ui.components.PlaySheetFrame;
import gov.va.semoss.ui.components.playsheets.PlaySheetCentralComponent;
import java.io.File;

import java.io.IOException;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;

/**
 * Action to save a Grid or Raw Grid to a CSV or tab-delimited text file, or to
 * Excel.
 *
 * @author Thomas
 *
 */
public class SaveAllGridAction extends AbstractSavingAction {

	private static final Logger log = Logger.getLogger( SaveAllGridAction.class );
	private PlaySheetFrame psf;

	public SaveAllGridAction() {
		super( "Save All", DbAction.getIcon( "save_alldiskette1" ), true );
		setToolTip( "Save All Grids" );
		setAppendDate( true );
		setDefaultFileName( "Grids" );
	}

	public void setPlaySheetFrame( PlaySheetFrame p ) {
		psf = p;
	}

	@Override
	protected void finishFileChooser( JFileChooser chsr ) {
		FileFilter xlsxFilter
				= new FileNameExtensionFilter( "Excel Workbook (*.xlsx)", "xlsx" );
		chsr.addChoosableFileFilter( xlsxFilter );
		chsr.setFileFilter( xlsxFilter );
	}

	/**
	 * Saves a "file" to disk as an Excel Workbook.
	 *
	 * @param file -- (File) The data to save to disk.
	 * @throws java.io.IOException
	 */
	@Override
	protected void saveTo( File file ) throws IOException {
		XlsWriter writer = new XlsWriter();
		writer.createWorkbook();
		
		for ( PlaySheetCentralComponent pscc : psf.getPlaySheets() ) {

			List<Object[]> table = pscc.getTabularData();

			if ( null != table ) {
				// we have tabular data to save
				String heads[] = pscc.getHeaders().toArray( new String[0]);
				writer.createTab( pscc.getTitle(), heads );
				
				for( Object[] row : table ){
					writer.addRow( row );
				}
			}
		}
		writer.write( file );
		
	}
}
