package gov.va.semoss.ui.actions;

import gov.va.semoss.poi.main.XlsWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JTable;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.prefs.CsvPreference.Builder;

/**
 * Action to save a Grid or Raw Grid to a CSV or tab-delimited text file, or to
 * Excel.
 *
 * @author Thomas
 *
 */
public class SaveGridAction extends AbstractSavingAction {
	private static final long serialVersionUID = 3476209795433405175L;
	private JTable table = new JTable();

	public SaveGridAction( boolean issaveas ) {
		super( "Save", issaveas );
		setToolTip( "Save Current Grid" );
		setAppendDate( true );
	}

	/**
	 * Exposes a setter for the JTable to display Grid or Raw Grid data.
	 *
	 * @param table -- (JTable) Display for grid data.
	 */
	public void setTable( JTable table ) {
		this.table = table;
	}

	@Override
	protected void finishFileChooser( JFileChooser chsr ) {
		chsr.setAcceptAllFileFilterUsed( false );
		FileFilter csvFilter = new FileNameExtensionFilter( "Text (Comma Delimited) (*.csv)", "csv" );
		FileFilter tsvFilter = new FileNameExtensionFilter( "Text (Tab Delimited) (*.txt)", "txt" );
		FileFilter xlsxFilter = new FileNameExtensionFilter( "Excel Workbook (*.xlsx)", "xlsx" );
		chsr.addChoosableFileFilter( csvFilter );
		chsr.addChoosableFileFilter( tsvFilter );
		chsr.addChoosableFileFilter( xlsxFilter );
		chsr.setFileFilter( xlsxFilter );
	}

	@Override
	public void saveTo( File savefile ) throws IOException {
		String extension = FilenameUtils.getExtension( savefile.getName() );

		switch ( extension.toLowerCase() ) {
			case "csv":
				saveInTxtFormat( savefile, ',' );
				break;
			case "txt":
				saveInTxtFormat( savefile, '\t' );
				break;
			case "xlsx":
				saveInExcelFormat( savefile );
				break;
			default:
				throw new IOException( "Unknown file extension: " + savefile );
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
	private void saveInTxtFormat( File file, char delimiter ) throws IOException {
		String heads[] = headerArr();

		Builder prefb = new CsvPreference.Builder( (char) CsvPreference.STANDARD_PREFERENCE.getQuoteChar(),
				(int) delimiter, CsvPreference.STANDARD_PREFERENCE.getEndOfLineSymbols() );

		CsvMapWriter writer = new CsvMapWriter( new FileWriter( file ), prefb.build() );
		writer.writeHeader( heads );

		for ( int r = 0; r < table.getRowCount(); r++ ) {
			Map<String, Object> map = new HashMap<>();
			for ( int c = 0; c < heads.length; c++ ) {
				map.put( heads[c], table.getValueAt( r, c ).toString() );
			}
			writer.write( map, heads );
		}
	}

	/**
	 * Saves a "file" to disk as an Excel Workbook.
	 *
	 * @param file -- (File) The data to save to disk.
	 *
	 * @return saveInExcelFormat -- (Boolean) Whether the save to disk succeeded.
	 */
	private void saveInExcelFormat( File file ) throws IOException {
		String heads[] = headerArr();

		XlsWriter writer = new XlsWriter();
		writer.createWorkbook();
		writer.createTab( "Grid Data", heads );

		for ( int r = 0; r < table.getRowCount(); r++ ) {
			String[] row = new String[heads.length];
			for ( int c = 0; c < heads.length; c++ ) {
				row[c] = table.getValueAt( r, c ).toString();
			}
			writer.addRow( row );
		}

		writer.write( file );
	}

	private String[] headerArr() {
		String heads[] = new String[table.getColumnCount()];
		for ( int i = 0; i < heads.length; i++ ) {
			heads[i] = table.getColumnName( i );
		}
		return heads;
	}
}
