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

	/**
	 * Method clearAll. Clears all information from the shape and color
	 * hashtables.
	 */
	public void clearAll() {
		shapeHash.clear();
		shapeHashL.clear();
		colorHash.clear();
		shapeStringHash.clear();
		colorStringHash.clear();
	}

	/**
	 * Method getAllShapes. Gets all the shapes from the instance of the shapes
	 * array.
	 *
	 * @return String[] - An array containing all the shapes.
	 */
	public static String[] getAllShapes() {
		return shapes;
	}

	/**
	 * Method getAllColors. Gets all the colors from the instance of the shapes
	 * array.
	 *
	 * @return String[] - An array containing all the colors.
	 */
	public static String[] getAllColors() {
		return colors;
	}

	/**
	 * Method addShapeToHashes. Adds a shape to the local Hashtable of shapes.
	 * Gets the shape from DI Helper.
	 *
	 * @param type String - The type of the vertex (serves as a key in the
	 * hashtable)
	 * @param shapeString String - the shape itself.
	 */
	public void addShapeToHashes( URI vertexType, String shapeString ) {
		shapeStringHash.put( vertexType, shapeString );
		shapeHash.put( vertexType, DIHelper.getShape( shapeString ) );
		shapeHashL.put( vertexType, DIHelper.getShape( shapeString + Constants.LEGEND ) );
	}

	/**
	 * Method setShape. Adds a shape to the local Hashtable of shapes. Gets the
	 * shape from DI Helper.
	 *
	 * @param type String - The type of the vertex (serves as a key in the
	 * hashtable)
	 * @param shapeString String - the shape itself.
	 */
	public boolean setShape( String shapeString, SEMOSSVertex vertex ) {
		if ( shapeString != null ) {
			addShapeToHashes( vertex.getType(), shapeString );
		}
		vertex.setShape( shapeHash.get( vertex.getType() ) );
		vertex.setShapeString( shapeStringHash.get( vertex.getType() ) );
		vertex.setShapeLegend( shapeHashL.get( vertex.getType() ) );
		return true;
	}

	/**
	 * Method addColorToHashes. Adds a color to the local Hashtable of colors.
	 * Gets the color from DI Helper.
	 *
	 * @param key String - the type of the vertex
	 * @param colorString String - the color of the shape
	 */
	public void addColorToHashes( URI key, String colorString ) {
		colorHash.put( key, DIHelper.getColor( colorString ) );
		colorStringHash.put( key, colorString );
	}

	public Shape getShape( URI type ) {
		//first check to see if we've seen this type before

		if ( shapeHash.containsKey( type ) ) {
			return shapeHash.get( type );
		}

		// next check if it is specified in the properties file
		String shapeStringSetInRDF_MapPropFile
				= DIHelper.getInstance().getProperty( type.getLocalName() + "_SHAPE" );
		if ( shapeStringSetInRDF_MapPropFile != null
				&& DIHelper.getShape( shapeStringSetInRDF_MapPropFile ) != null ) {
			return DIHelper.getShape( shapeStringSetInRDF_MapPropFile );
		}
		// if the shape hasn't been set yet, use the first shape not yet in use
		for ( String shapeString : shapes ) {
			if ( !shapeStringHash.containsValue( shapeString ) ) {
				
				return DIHelper.getShape( shapeString );
			}
		}
		
		//if all of the shapes have already been used, just grab a random shape
		List<String> strings = new ArrayList<>( shapeStringHash.values() );
		Collections.shuffle( strings );
		return DIHelper.getShape( strings.get( 0 ) );
	
	}


}
