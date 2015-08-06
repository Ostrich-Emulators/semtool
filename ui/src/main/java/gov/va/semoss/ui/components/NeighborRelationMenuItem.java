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

import org.apache.log4j.Logger;

import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * This class is used to create a menu item for composing relationships for the
 * neighborhood.
 */
public class NeighborRelationMenuItem extends AbstractAction {

	String predicateURI = null;
	String name = null;
	private final GraphPlaySheet gps;

	public NeighborRelationMenuItem( String name, GraphPlaySheet ps, String predicateURI ) {
		super( name );
		this.name = name;
		this.predicateURI = predicateURI;
		gps = ps;
	}

	@Override
	public void actionPerformed( ActionEvent ae ) {
		String predURI = DIHelper.getInstance().getProperty( Constants.PREDICATE_URI );
		predURI += ";" + name;
		DIHelper.getInstance().putProperty( Constants.PREDICATE_URI, predURI );
		gps.refineView();
	}
}
