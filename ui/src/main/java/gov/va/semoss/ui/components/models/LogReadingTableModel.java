/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class LogReadingTableModel extends AbstractTableModel {

	public static enum Level {

		FATAL, ERROR, WARN, INFO, DEBUG
	};
	private static final Logger log = Logger.
			getLogger( LogReadingTableModel.class );
	private static final String[] COLNAMES = { "Time", "Level", "Location", "Message" };
	private final List<LogRow> data = new ArrayList<>();
	private final Pattern pat = Pattern.compile( 
			"(.*) \\[([^\\]]+)\\] (.*) - (.*)" );
	private final SimpleDateFormat FMT = new SimpleDateFormat( 
			"yyyy-MMM-dd hh:mm:ss,SSS" );
	private File file = null;

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public int getColumnCount() {
		return COLNAMES.length;
	}

	@Override
	public String getColumnName( int columnIndex ) {
		return COLNAMES[columnIndex];
	}

	@Override
	public boolean isCellEditable( int rowIndex, int columnIndex ) {
		return false;
	}

	@Override
	public Object getValueAt( int rowIndex, int columnIndex ) {
		LogRow row = data.get( rowIndex );
		switch ( columnIndex ) {
			case 0:
				return row.date;
			case 1:
				return row.level.toString();
			case 2:
				return row.location;
			default:
				return row.message;
		}
	}

	@Override
	public Class<?> getColumnClass( int col ) {
		return ( 0 == col ? Date.class : String.class );
	}

	public void setFile( File logfile ) {
		file = logfile;
	}

	public void clear(){
		data.clear();
		fireTableDataChanged();
	}
	
	public void refresh() {
		clear();
		
		SwingWorker<Void, LogRow> sw = new SwingWorker<Void, LogRow>() {

			@Override
			protected Void doInBackground() throws Exception {
				try ( BufferedReader rdr = new BufferedReader( new FileReader( file ) ) ) {
					String line = null;
					while ( null != ( line = rdr.readLine() ) ) {
						// parse line into a LogRow 
						Matcher m = pat.matcher( line );
						if ( m.matches() ) {
							String datestr = m.group( 1 );
							String lev = m.group( 2 ).trim();
							String loc = m.group( 3 );
							String msg = m.group( 4 );
							Date date = null;
							try {
								date = FMT.parse( datestr );
							}
							catch ( Exception e ) {
								log.warn( "unparseable date: " + datestr );
							}

							this.publish( new LogRow( date, Level.valueOf( lev ), loc, msg ) );
						}
					}
				}
				catch ( IOException ioe ) {
					log.error( ioe );
				}
				return null;
			}

			@Override
			protected void done() {
				// LogReadingTableModel.this.fireTableDataChanged();
			}

			@Override
			protected void process( List<LogRow> chunks ) {
				int sz = data.size();
				data.addAll( chunks );
				LogReadingTableModel.this.fireTableRowsInserted( sz, data.size()-1 );
			}
		};
		
		sw.execute();
	}

	private class LogRow {

		Date date;
		Level level;
		String message;
		String location;

		public LogRow( Date date, Level level, String loc, String message ) {
			this.date = date;
			this.level = level;
			this.location = loc;
			this.message = message;
		}
	}
}
