/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components;

import gov.va.semoss.ui.components.models.LogReadingTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import org.apache.log4j.Logger;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.ui.components.models.LogReadingTableModel.Level;

/**
 *
 * @author ryan
 */
public class LoggingPanel extends JPanel {

	private static final long serialVersionUID = 3532097592377920709L;
	private static final Logger log = Logger.getLogger( LoggingPanel.class );

	final LogReadingTableModel model = new LogReadingTableModel();
	final JTable table = new JTable( model );
	final Set<Level> ons = new HashSet<>();

	public LoggingPanel() {
		setLayout( new BorderLayout() );
		JPanel buttons = new JPanel();
		GridLayout grid = new GridLayout( 0, Level.values().length );
		buttons.setLayout( grid );

		ActionListener al = setupRowSorter();
		for ( Level lev : LogReadingTableModel.Level.values() ) {
			JToggleButton btn = new JToggleButton( lev.toString() );
			btn.setActionCommand( lev.toString() );
			buttons.add( btn );
			if ( Level.DEBUG != lev ) {
				btn.setSelected( true );
				ons.add( lev );
			}
			btn.addActionListener( al );
		}

		add( buttons, BorderLayout.NORTH );
		add( new JScrollPane( table ), BorderLayout.CENTER );
		table.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );

		JButton logButton = new JButton( "Refresh" );
		add( logButton, BorderLayout.SOUTH );

		File logfile
				= guessFileFromProps( DIHelper.getInstance().getProperty( "LOG4J" ) );
		model.setFile( logfile );

		logButton.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed( ActionEvent e ) {
				model.refresh();
			}
		} );

		final SimpleDateFormat SDF = new SimpleDateFormat( "MM/dd/yyyy hh:mm:ss" );
		table.setDefaultRenderer( Date.class, new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 2908354982706310508L;

			@Override
			public Component getTableCellRendererComponent( JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column ) {
				String str = ( null != value ? SDF.format( Date.class.cast( value ) )
						: "" );
				return super.getTableCellRendererComponent( table, str, isSelected,
						hasFocus, row, column );
			}
		} );

		table.setAutoResizeMode( JTable.AUTO_RESIZE_LAST_COLUMN );
		TableColumnModel tcmod = table.getColumnModel();
		tcmod.getColumn( 0 ).setPreferredWidth( 100 );
		tcmod.getColumn( 1 ).setPreferredWidth( 50 );
	}

	public static File guessFileFromProps( String log4jpropfile ) {
		File file = null;
		try ( InputStream stream = ( null == log4jpropfile
				? LoggingPanel.class.getResourceAsStream( "/log4j.properties" )
				: new FileInputStream( log4jpropfile ) ) ) {

			BufferedReader rdr = new BufferedReader( new InputStreamReader( stream ) );

			String line = null;
			while ( null != ( line = rdr.readLine() ) ) {
				line = line.replaceAll( "#.*", "" ); // remove comments

				int pos = line.indexOf( "=" );
				if ( pos > 0 ) {
					String var = line.substring( 0, pos ).trim();
					if ( var.toUpperCase().endsWith( ".FILE" ) ) {
						String val = line.substring( pos + 1 ).trim();
						file = new File( val );
					}
				}
			}
		}
		catch ( IOException ioe ) {
			log.warn( ioe );
		}

		return file;
	}

	public void refresh() {
		model.refresh();
		table.scrollRectToVisible( table.getCellRect( model.getRowCount() - 1, 0, true ) );
	}

	private ActionListener setupRowSorter() {
		final TableRowSorter<LogReadingTableModel> sorter = new TableRowSorter<>( model );
		table.setRowSorter( sorter );
		final RowFilter<LogReadingTableModel, Integer> filter
				= new RowFilter<LogReadingTableModel, Integer>() {

					@Override
					public boolean include( RowFilter.Entry<? extends LogReadingTableModel, ? extends Integer> entry ) {
						Level lev = Level.valueOf( entry.getStringValue( 1 ) ); // "Level" column in the model
						return ons.contains( lev );
					}
				};
		sorter.setRowFilter( filter );

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed( ActionEvent e ) {
				String cmd = e.getActionCommand();
				Level lev = Level.valueOf( cmd );
				if ( ons.contains( lev ) ) {
					ons.remove( lev );
				}
				else {
					ons.add( lev );
				}

				sorter.setRowFilter( filter );
			}
		};

		return al;
	}
}
