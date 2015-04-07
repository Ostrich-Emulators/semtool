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

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * This class is used to keep track of specific properties for a table.
 */
public class ControlDataTable {
	private Object[][] rows;
	private Hashtable<String, ArrayList<String>> selectedList = new Hashtable<String, ArrayList<String>>();
	private Hashtable<String, String> unselectedList = new Hashtable<String, String>();
	private Hashtable<String, String> propOn  = new Hashtable<String, String>();

	public ControlDataTable(Hashtable<String, String> _propOn) {
		propOn = _propOn;
	}

	public void generate(int rowIndex, String type, String prop, boolean firstRow) {
		boolean foundProp = findIfPropSelected(selectedList, type, prop);
		
		rows[rowIndex][0] = "";
		rows[rowIndex][1] = prop;
		rows[rowIndex][2] = new Boolean(foundProp);
		rows[rowIndex][3] = type;

		if (firstRow)
			rows[rowIndex][0] = type;

		if (propOn.containsKey(prop) && !unselectedList.containsKey(type) && !foundProp) {
			rows[rowIndex][2] = new Boolean(true);
			
			ArrayList<String> typePropList = new ArrayList<String>();
			if (selectedList.containsKey(type))
				typePropList = selectedList.get(type);
			
			typePropList.add(prop);
			selectedList.put(type, typePropList);
		}
	}

	/**
	 * Checks if property for a certain type is selected.
	 * 
	 * @param list
	 *            List of properties.
	 * @param type
	 *            Property type.
	 * @param prop
	 *            Property.
	 * 
	 * @return boolean True if a property is selected.
	 */
	private boolean findIfPropSelected(Hashtable<String, ArrayList<String>> list,
			String type, String prop) {
		if (!list.containsKey(type)) 
			return false;
		
		for (String thisProp:list.get(type)) {
			if (thisProp!= null && thisProp.equalsIgnoreCase(prop))
				return true;
		}

		return false;
	}

	/**
	 * Sets value at a particular row and column location.
	 * 
	 * @param val
	 *            Label value.
	 * @param row
	 *            Row number.
	 * @param column
	 *            Column number.
	 */
	public void setValueAt(Object val, int row, int column) {
		// check if it is the header row--select all
		if (row == 0) {
			for (int i=1; i<rows.length; i++)
				setValueAt(val, i, 2);
		}
		
		String type = rows[row][3] + "";
		ArrayList<String> typePropList = new ArrayList<String>();
		if (selectedList.containsKey(type))
			typePropList = selectedList.get(type);
		
		if (val instanceof Boolean) {
			if ((Boolean) val)
				typePropList.add(rows[row][1] + "");
			else {
				typePropList.remove(rows[row][1] + "");
				unselectedList.put(type, type);
			}
		}
		
		rows[row][column] = val;
		selectedList.put(type, typePropList);
	}

	public void initRows(int rowCount) {
		// columns are - Type - Property - Boolean
		rows = new Object[rowCount + 1][4];
		rows[0][0] = "SELECT ALL";
		rows[0][1] = "";
		rows[0][2] = new Boolean(true);
		rows[0][3] = "SELECT ALL";
	}

	/**
	 * Gets label value from a particular row and column location.
	 * 
	 * @param row
	 *            Row number.
	 * @param column
	 *            Column number.
	 * 
	 * @return Object Label value.
	 */
	public Object getCell(int row, int column) {
		return rows[row][column];
	}

	/**
	 * Gets properties of a specific type.
	 * 
	 * @param type
	 *            Type of property to retrieve.
	 * 
	 * @return Vector<String> List of properties.
	 */
	public ArrayList<String> getSelectedProperties(String type) {
		return selectedList.get(type);
	}
}