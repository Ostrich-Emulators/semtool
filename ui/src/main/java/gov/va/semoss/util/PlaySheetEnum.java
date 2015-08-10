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
package gov.va.semoss.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Enables a variable to be a set of predefined constants.
 * This class defines constants for all types of playsheets, which includes their names, source file location, and the hint for a SPARQL query associated with each.
 */

public enum PlaySheetEnum {
	
	Grid("Grid", "gov.va.semoss.ui.components.playsheets.GridPlaySheet", "GridPlaySheet Hint: SELECT ?x1 ?x2 ?x3 WHERE{ ... }"),
	Raw_Grid("Raw Grid", "gov.va.semoss.ui.components.playsheets.GridRAWPlaySheet", "GridRAWPlaySheet Hint: SELECT ?x1 ?x2 ?x3 WHERE{ ... }"),
	Column_Chart("Column Chart", "gov.va.semoss.ui.components.playsheets.ColumnChartPlaySheet", "ColumnChartPlaySheet Hint: SELECT ?xAxis ?yAxis1 (OPTIONAL) ?yAxis2 ?yAxis3 ... (where all yAxis values are numbers) WHERE { ... }"),
    Dendrogram("Dendrogram", "gov.va.semoss.ui.components.playsheets.DendrogramPlaySheet", "DendrogramPlaySheet Hint: SELECT DISTINCT ?var-1 ?var-2 ?var-3 ... (where ?var-2 contains children of ?var-1, ?var-3 of ?var-2, etc.) WHERE { ... }"),
	Graph("Graph", "gov.va.semoss.ui.components.playsheets.GraphPlaySheet", "GraphPlaySheet Hint: CONSTRUCT {?subject ?predicate ?object} WHERE{ ... }"),
	Grid_Scatter("Grid Scatter", "gov.va.semoss.ui.components.playsheets.GridScatterSheet", "GridScatterSheet Hint: SELECT ?elementName ?xAxisValues ?yAxisValues (OPTIONAL)?zAxisValues WHERE{ ... }"),
	Heat_Map("Heat Map", "gov.va.semoss.ui.components.playsheets.HeatMapPlaySheet", "HeatMapPlaySheet Hint: SELECT ?xAxisList ?yAxisList ?numericHeatValue WHERE{ ... } GROUP BY ?xAxisList ?yAxisList"),
	Parallel_Coordinates("Parallel Coordinates", "gov.va.semoss.ui.components.playsheets.ParallelCoordinatesPlaySheet", "ParallelCoordinatesPlaySheet Hint: SELECT ?axis1 ?axis2 ?axis3 WHERE{ ... }"),
	Pie_Chart("Pie Chart", "gov.va.semoss.ui.components.playsheets.PieChartPlaySheet", "PieChartPlaySheet Hint: SELECT ?wedgeName ?wedgeValue WHERE { ... }"),
	Sankey_Diagram("Sankey Diagram","gov.va.semoss.ui.components.playsheets.SankeyPlaySheet", "SankeyPlaySheet Hint: SELECT ?source ?target ?value ?target2 ?value2 ?target3 ?value3...etc  Note: ?target is the source for ?target2 and ?target2 is the source for ?target3...etc WHERE{ ... }"),
	US_Heat_Map("US Heat Map","gov.va.semoss.ui.components.playsheets.USHeatMapPlaySheet", "USHeatMapPlaySheet Hint: SELECT ?state ?numericHeatValue WHERE{ ... }"),
	World_Heat_Map("World Heat Map","gov.va.semoss.ui.components.playsheets.WorldHeatMapPlaySheet", "WorldHeatMapPlaySheet Hint: SELECT ?country ?numericHeatValue WHERE{ ... }"),
	Metamodel_Graph("Metamodel Graph","gov.va.semoss.ui.components.playsheets.MetamodelGraphPlaySheet", "MetamodelGraphPlaySheet Hint: SELECT DISTINCT ?source ?relation ?target WHERE{ ... }"),
	Update_Query("Update Query","","UpdateQuery Hint: Try a SPARQL query that INSERTs data, DELETEs data, or does both.");

	private final String sheetName;
	private final String sheetClass;
	private final String sheetHint;
	
	PlaySheetEnum(String playSheetName, String playSheetClass, String playSheetHint) {
		this.sheetName = playSheetName;
		this.sheetClass = playSheetClass;
		this.sheetHint = playSheetHint;
	}
	
	public String getSheetClass(){
		return this.sheetClass;
	}
	
	public String getSheetName(){
		return this.sheetName;
	}
	
	public String getSheetHint(){
		return this.sheetHint;
	}
	
	public static List<String> getAllSheetNames(){
		List<String> list = new ArrayList<>();
		for (PlaySheetEnum e : PlaySheetEnum.values())
		{
			list.add(e.getSheetName());
		}
		return list;
	}
	
	public static List<String> getAllSheetClasses(){
		List<String> list = new ArrayList<>();
		for (PlaySheetEnum e : PlaySheetEnum.values())
		{
			list.add(e.getSheetClass());
		}
		return list;
	}
	
	public static String getClassFromName(String sheetName){
		String match = "";
		for(PlaySheetEnum e : PlaySheetEnum.values())
			if(e.getSheetName().equals(sheetName))
			{
				match = e.getSheetClass();
			}
		return match;
	}
	
	public static String getHintFromName(String sheetName){
		String match = "";
		for(PlaySheetEnum e : PlaySheetEnum.values())
			if(e.getSheetName().equals(sheetName))
			{
				match = e.getSheetHint();
			}
		return match;
	}
	
	public static String getNameFromClass(String sheetClass){
		String match = "";
		for(PlaySheetEnum e : PlaySheetEnum.values())
			if(e.getSheetClass().equals(sheetClass))
			{
				match = e.getSheetName();
			}
		return match;
	}
	
	public static PlaySheetEnum getEnumFromClass(String sheetClass){
		PlaySheetEnum match = null; //need to initialize as non-null value
		for(PlaySheetEnum e : PlaySheetEnum.values())
			if(e.getSheetClass().equals(sheetClass))
			{
				match = e;
			}
		return match;		
	}
}