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
package gov.va.semoss.ui.components.playsheets;

import java.awt.BorderLayout;

import javax.swing.JButton;

import gov.va.semoss.om.SEMOSSVertex;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used in chart listeners to create the appropriate browser and
 * pull the appropriate data for a playsheet.
 */
public class BrowserTabSheet3 extends BrowserPlaySheet2 implements ActionListener {
	private static final long serialVersionUID = 5944414296343639772L;
	private GraphPlaySheet gps;

	/**
	 * Constructor for BrowserTabSheet3.
	 *
	 * @param fileName File name to be navigated to in the browser.
	 * @param ps	Playsheet being called.
	 */
	public BrowserTabSheet3( String fileName, GraphPlaySheet ps ) {
		super( fileName );
		this.gps = ps;

		setLayout( new BorderLayout() );
		JButton pullData = new JButton( "Pull New Data" );
		pullData.addActionListener( this );

		add( pullData, BorderLayout.NORTH );
		add( browser.getView().getComponent(), BorderLayout.CENTER );
	}

	@Override
	public void actionPerformed( ActionEvent ae ) {
		pullData();
	}

	public void pullData() {
		Map<String, List<SEMOSSVertex>> nodeHash = gps.getFilterData().getTypeHash();
		Map<String, Object> newHash = new HashMap<>();
		newHash.put( "Nodes", nodeHash );
		addDataHash( newHash );
		createView();
	}
}