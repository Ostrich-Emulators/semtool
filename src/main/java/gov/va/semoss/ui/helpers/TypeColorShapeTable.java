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
package gov.va.semoss.ui.helpers;

import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;

import java.awt.Color;
import java.awt.Shape;
import java.util.Hashtable;
import java.util.Random;

/**
 * This is the table that stores all the shapes and colors for nodes on the graph play sheet.
 */
public class TypeColorShapeTable {
	private static TypeColorShapeTable instance = new TypeColorShapeTable();

	private Hashtable<String, Shape>        shapeHash = new Hashtable<String, Shape>();
	private Hashtable<String, Shape>       shapeHashL = new Hashtable<String, Shape>();
	private Hashtable<String, String> shapeStringHash = new Hashtable<String, String>();

	private Hashtable<String, Color>        colorHash = new Hashtable<String, Color>();
	private Hashtable<String, String> colorStringHash = new Hashtable<String, String>();

	private static String[] shapes, colors;
	
	/**
	 * Constructor for TypeColorShapeTable, only called internally.
	 */
	protected TypeColorShapeTable() {
		shapes = new String[7];
		shapes[0] = Constants.TRIANGLE;
		shapes[1] = Constants.CIRCLE;
		shapes[2] = Constants.SQUARE;
		shapes[3] = Constants.DIAMOND;
		shapes[4] = Constants.STAR;
		shapes[5] = Constants.PENTAGON;
		shapes[6] = Constants.HEXAGON;
		
		colors = new String[10];
		colors[0] = Constants.BLUE;
		colors[1] = Constants.GREEN;
		colors[2] = Constants.RED;
		colors[3] = Constants.BROWN;
		colors[4] = Constants.MAGENTA;
		colors[5] = Constants.ORANGE;
		colors[6] = Constants.YELLOW;
		colors[7] = Constants.AQUA;
		colors[8] = Constants.PURPLE;
		colors[9] = Constants.TRANSPARENT;
	}
	
	/**
	 * Method getInstance.  Gets an instance of a TypeColorShapeTable.
	
	 * @return TypeColorShapeTable */
	public static TypeColorShapeTable getInstance() {
		return instance;
	}
	
	/**
	 * Method clearAll.  Clears all information from the shape and color hashtables.
	 */
	public void clearAll() {
		shapeHash.clear();
		shapeHashL.clear();
		colorHash.clear();
		shapeStringHash.clear();
		colorStringHash.clear();
	}
	
	/**
	 * Method getAllShapes. Gets all the shapes from the instance of the shapes array.
	 * @return String[] - An array containing all the shapes.*/
	public static String[] getAllShapes() {
		return shapes;
	}

	/**
	 * Method getAllColors.  Gets all the colors from the instance of the shapes array.
	 * @return String[] - An array containing all the colors.*/
	public static String[] getAllColors() {
		return colors;
	}
	
	/**
	 * Method addShapeToHashes. Adds a shape to the local Hashtable of shapes.  Gets the shape from DI Helper.
	 * @param type String - The type of the vertex (serves as a key in the hashtable)
	 * @param shapeString String - the shape itself.
	 */
	public void addShapeToHashes(String vertexType, String shapeString) {
		shapeStringHash.put(vertexType, shapeString);
		      shapeHash.put(vertexType, DIHelper.getShape(shapeString) );
		     shapeHashL.put(vertexType, DIHelper.getShape(shapeString + Constants.LEGEND) );
	}
	
	/**
	 * Method setShape. Adds a shape to the local Hashtable of shapes.  Gets the shape from DI Helper.
	 * @param type String - The type of the vertex (serves as a key in the hashtable)
	 * @param shapeString String - the shape itself.
	 */
	public boolean setShape(String shapeString, SEMOSSVertex vertex) {
		if (shapeString!=null)
			addShapeToHashes(vertex.getType(), shapeString);
		vertex.setShape( shapeHash.get(vertex.getType()) );
		vertex.setShapeString( shapeStringHash.get(vertex.getType()) );
		vertex.setShapeLegend( shapeHashL.get(vertex.getType()) );
		return true;
	}

	/**
	 * Method addColorToHashes.  Adds a color to the local Hashtable of colors.  Gets the color from DI Helper.
	 * @param key String - the type of the vertex
	 * @param colorString String - the color of the shape
	 */
	public void addColorToHashes(String key, String colorString) {
		colorHash.put(key, DIHelper.getColor(colorString));
		colorStringHash.put(key, colorString);
	}
	
	/**
	 * Method setColor. Setes the color for a vertex
	 * @param colorString String - the color itself
	 * @param SEMOSSVertex vertex - the vertex whose color we are setting
	 */
	public boolean setColor(String colorString, SEMOSSVertex vertex) {
		if (colorString!=null)
			addColorToHashes(vertex.getType(), colorString);
		vertex.setColor( colorHash.get(vertex.getType()) );
		vertex.setColorString( colorStringHash.get(vertex.getType()) );
		return true;
	}


	/**
	 * Method initializeShape.  Setting the initial shape of a node based on the shape type
	 * @param vertex - the vertex for whom we are setting the shape
	 */
	public boolean initializeShape(SEMOSSVertex vertex) {
		//first check to see if we've seen this type before
		if(shapeHash.containsKey(vertex.getType())) {
			return setShape(null, vertex);
		}
		
		// next check if it is specified in the properties file
		String shapeStringSetInRDF_MapPropFile = DIHelper.getInstance().getProperty(vertex.getType() + "_SHAPE");
		if(shapeStringSetInRDF_MapPropFile != null && DIHelper.getShape(shapeStringSetInRDF_MapPropFile) != null) {
			return setShape(shapeStringSetInRDF_MapPropFile, vertex);
		}

		// if the shape hasn't been set yet, use the first shape not yet in use
		for(String shapeString : shapes){
			if(!shapeStringHash.containsValue(shapeString)){
				return setShape(shapeString, vertex);
			}
		}

		//if all of the shapes have already been used, just grab a random shape
        Object[] keys = shapeHash.keySet().toArray();
        Object key = keys[new Random().nextInt(keys.length)];
        String shapeString = shapeStringHash.get(key);

		return setShape(shapeString, vertex);
	}

	/**
	 * Method initializeColor.  Gets the color based on the parameters.
	 * @param vertex
	 * @return Color - the color based on the type and vertex name*/
	public boolean initializeColor(SEMOSSVertex vertex) {
		// first check if we've seen the type before
		if(colorHash.containsKey(vertex.getType())) {
			return setColor(null, vertex);
		}
			
		// try to search the properties file for the first time
		String colorStringSetInRDF_MapPropFile = DIHelper.getInstance().getProperty(vertex.getType() + "_COLOR");
		if(colorStringSetInRDF_MapPropFile != null) {
			return setColor(colorStringSetInRDF_MapPropFile, vertex);
		}
		
		//find the first color that hasn't been used yet
		for(String colorString : colors){
			if(!colorStringHash.containsValue(colorString)){
				return setColor(colorString, vertex);
			}
		}
		
		//if all of the colors have already been used, just grab a random color
        Object[] keys = colorHash.keySet().toArray();
        Object key = keys[new Random().nextInt(keys.length)];
        
		return setColor(colorStringHash.get(key), vertex);
	}
}