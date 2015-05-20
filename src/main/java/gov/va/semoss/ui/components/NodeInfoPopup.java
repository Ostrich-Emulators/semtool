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
package gov.va.semoss.ui.components;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.apache.log4j.Logger;

import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.util.Constants;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JInternalFrame;
import org.openrdf.model.vocabulary.RDF;

/**
 * This class is used to display information about a node in a popup window.
 */
public class NodeInfoPopup extends AbstractAction {

	GraphPlaySheet ps = null;
	SEMOSSVertex[] pickedVertex = null;
	private static final Logger logger = Logger.getLogger( NodeInfoPopup.class );
	GridFilterData gfd = new GridFilterData();
	JTable table = null;
	private final JDesktopPane pane;

	public NodeInfoPopup( GraphPlaySheet p, SEMOSSVertex[] picked,
			JDesktopPane pane ) {
		super( "Show Selected Node Information" );
		this.putValue( Action.SHORT_DESCRIPTION,
				"To select nodes press Shift and click on nodes" );
		ps = p;
		pickedVertex = picked;
		this.pane = pane;
	}

	@Override
	public void actionPerformed( ActionEvent ae ) {
		List<Object[]> listData = setListData();
		setTableData( listData );
		createTable();
	}

	/**
	 * Sets the list data.
	 *
	 * @return ArrayList<Object[]> List of data.
	 */
	public List<Object[]> setListData() {
		List<Object[]> retList = new ArrayList();
		retList = addVertTypeCounts( retList );
		retList = addTotalVertCount( retList );
		return retList;
	}

	/**
	 * Process the data into a hashtable and count the number of each vertex type.
	 *
	 * @param list List of vertex types.
	 *
	 * @return ArrayList List of vertex types with counts of each one.
	 */
	private List<Object[]> addVertTypeCounts( List<Object[]> list ) {
		//first process the data into a hashtable
		Map<String, Integer> typeCounts = new HashMap<>();
		for ( SEMOSSVertex v : pickedVertex ) {
			String vType = v.getProperty( RDF.TYPE ).toString();
			//if the hashtable already contains the type, add to the count
			if ( typeCounts.containsKey( vType ) ) {
				int count = typeCounts.get( vType );
				count = count + 1;
				typeCounts.put( vType, count );
			}
			//if the hashtable does not contain the type, put the type in with count 1
			else {
				int count = 1;
				typeCounts.put( vType, count );
			}
		}

		//use the hashtable to put it in table format
		Iterator hashIt = typeCounts.keySet().iterator();
		while ( hashIt.hasNext() ) {
			String type = hashIt.next() + "";
			int count = typeCounts.get( type );
			Object[] row = new Object[2];
			row[0] = type;
			row[1] = count;
			list.add( row );
		}
		return list;
	}

	/**
	 * Returns the count of the total number of vertices.
	 *
	 * @param list List of vertices.
	 *
	 * @return ArrayList	List with count of total vertices.
	 */
	private List<Object[]> addTotalVertCount( List<Object[]> list ) {
		Object[] row = new Object[2];
		row[0] = "Total Vertex Count";
		row[1] = pickedVertex.length;
		list.add( row );
		return list;
	}

	/**
	 * Creates the JPanel and adds the table to it.
	 */
	public void createTable() {

		try {
			JPanel mainPanel = new JPanel();

			logger.debug( "Created the table" );
			logger.debug( "Added the internal frame listener " );
			table.setAutoCreateRowSorter( true );
			table.setPreferredScrollableViewportSize( new Dimension( 250, 200 ) );
			//this.add(new JButton("Yo"));
			GridBagLayout gbl_mainPanel = new GridBagLayout();
			gbl_mainPanel.columnWidths = new int[]{ 0, 0 };
			gbl_mainPanel.rowHeights = new int[]{ 0, 0 };
			gbl_mainPanel.columnWeights = new double[]{ 1.0, Double.MIN_VALUE };
			gbl_mainPanel.rowWeights = new double[]{ 1.0, Double.MIN_VALUE };
			mainPanel.setLayout( gbl_mainPanel );

			JScrollPane scrollPane = new JScrollPane( table );
			scrollPane.setAutoscrolls( true );

			GridBagConstraints gbc_scrollPane = new GridBagConstraints();
			gbc_scrollPane.fill = GridBagConstraints.BOTH;
			gbc_scrollPane.gridx = 0;
			gbc_scrollPane.gridy = 0;
			gbc_scrollPane.gridwidth = 100;
			gbc_scrollPane.gridheight = 100;
			mainPanel.add( scrollPane, gbc_scrollPane );

			mainPanel.setBounds( 100, 100, 100, 75 );

			JInternalFrame frame = new JInternalFrame();
			frame.setContentPane( mainPanel );

			frame.setTitle( "Selected Node Information" );
			frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
			frame.setResizable( true );
			frame.setClosable( true );
			frame.setBounds( 100, 100, 200, 250 );
			frame.setLocation( 0, pane.getHeight() - frame.getHeight() );

			pane.add( frame );

			frame.pack();
			frame.setVisible( true );
			frame.setSelected( false );
			frame.setSelected( true );
			logger.debug( "Added the main pane" );

		}
		catch ( PropertyVetoException e ) {
			logger.error( e, e );
		}
	}

	/**
	 * Sets the table data for the grid table model.
	 *
	 * @param list List of table data.
	 */
	public void setTableData( List<Object[]> list ) {
		gfd.setColumnNames( new String[]{ "Property Name", "Value" } );
		gfd.setDataList( list );
		table = new JTable( gfd );
	}
}
