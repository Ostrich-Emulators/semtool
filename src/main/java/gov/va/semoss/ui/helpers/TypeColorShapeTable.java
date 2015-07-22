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
package gov.va.semoss.ui.helpers;

import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;

import java.awt.Color;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;

/**
 * This is the table that stores all the shapes and colors for nodes on the
 * graph play sheet.
 */
public class TypeColorShapeTable {

	private static final TypeColorShapeTable instance = new TypeColorShapeTable();

	private final Map<URI, Shape> shapeHash = new HashMap<>();
	private final Map<URI, Shape> shapeHashL = new HashMap<>();
	private final Map<URI, String> shapeStringHash = new HashMap<>();

	private final Map<URI, Color> colorHash = new HashMap<>();
	private final Map<URI, String> colorStringHash = new HashMap<>();

	private static final String[] shapes = {
		Constants.TRIANGLE,
		Constants.CIRCLE,
		Constants.SQUARE,
		Constants.DIAMOND,
		Constants.STAR,
		Constants.PENTAGON,
		Constants.HEXAGON };
	private static final String[] colors = {
		Constants.BLUE,
		Constants.GREEN,
		Constants.RED,
		Constants.BROWN,
		Constants.MAGENTA,
		Constants.ORANGE,
		Constants.YELLOW,
		Constants.AQUA,
		Constants.PURPLE
	};

	/**
	 * Constructor for TypeColorShapeTable, only called internally.
	 */
	private TypeColorShapeTable() {

	}

	/**
	 * Method getInstance. Gets an instance of a TypeColorShapeTable.
	 *
	 * @return TypeColorShapeTable
	 */
	public static TypeColorShapeTable getInstance() {
		return instance;
	}





}
