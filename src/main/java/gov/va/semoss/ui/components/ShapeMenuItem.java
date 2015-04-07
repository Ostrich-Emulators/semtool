/*******************************************************************************
 * Copyright 2013 SEMOSS.ORG
 * 
 * This file is part of SEMOSS.
 * 
 * SEMOSS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * SEMOSS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SEMOSS.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package gov.va.semoss.ui.components;


import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.util.Utility;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;


/**
 * This class is used to create a menu item for different shapes.
 */
public class ShapeMenuItem extends AbstractAction {
	private static final long serialVersionUID = -7336693731725987817L;
	private SEMOSSVertex[] pickedVertex;
	private String shape;
	private GraphPlaySheet gps;

	/**
	 * Constructor for ShapeMenuItem.
	 * @param _shape String
	 * @param ps the graph play sheet
	 * @param _pickedVertex vertices
	 */
	public ShapeMenuItem(String _shape, GraphPlaySheet ps, SEMOSSVertex[] _pickedVertex) {
		super(_shape);
		gps = ps;
		shape = _shape;
		pickedVertex = _pickedVertex;
	}

	@Override
	public void actionPerformed( ActionEvent ae ) {
		GraphPlaySheet playSheet = Utility.getActiveGraphPlaysheet();
		for(int i = 0; i < pickedVertex.length; i++) {
			playSheet.getColorShapeData().setShape(pickedVertex[i], shape);
		}
		
		gps.repaint();
	}
}
