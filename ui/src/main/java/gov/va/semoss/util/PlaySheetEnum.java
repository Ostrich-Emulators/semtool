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
package gov.va.semoss.util;

import gov.va.semoss.om.Insight;
import gov.va.semoss.ui.components.api.IPlaySheet;
import gov.va.semoss.ui.components.playsheets.AppDupeHeatMapSheet;
import gov.va.semoss.ui.components.playsheets.ColumnChartPlaySheet;
import gov.va.semoss.ui.components.playsheets.DendrogramPlaySheet;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.ui.components.playsheets.GridPlaySheet;
import gov.va.semoss.ui.components.playsheets.GridRAWPlaySheet;
import gov.va.semoss.ui.components.playsheets.GridScatterSheet;
import gov.va.semoss.ui.components.playsheets.HeatMapPlaySheet;
import gov.va.semoss.ui.components.playsheets.MetamodelGraphPlaySheet;
import gov.va.semoss.ui.components.playsheets.ParallelCoordinatesPlaySheet;
import gov.va.semoss.ui.components.playsheets.PieChartPlaySheet;
import gov.va.semoss.ui.components.playsheets.SankeyPlaySheet;
import gov.va.semoss.ui.components.playsheets.USHeatMapPlaySheet;
import gov.va.semoss.ui.components.playsheets.WorldHeatMapPlaySheet;
import gov.va.semoss.ui.components.playsheets.helpers.DupeHeatMapSheet;
import java.util.EnumSet;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 * Enables a variable to be a set of predefined constants. This class defines
 * constants for all types of playsheets, which includes their names, source
 * file location, and the hint for a SPARQL query associated with each.
 */
public enum PlaySheetEnum {

	Grid( "Grid", GridPlaySheet.class, "GridPlaySheet Hint: SELECT ?x1 ?x2 ?x3 WHERE{ ... }" ),
	Raw_Grid( "Raw Grid", GridRAWPlaySheet.class, "GridRAWPlaySheet Hint: SELECT ?x1 ?x2 ?x3 WHERE{ ... }" ),
	Column_Chart( "Column Chart", ColumnChartPlaySheet.class, "ColumnChartPlaySheet Hint: SELECT ?xAxis ?yAxis1 (OPTIONAL) ?yAxis2 ?yAxis3 ... (where all yAxis values are numbers) WHERE { ... }" ),
	Dendrogram( "Dendrogram", DendrogramPlaySheet.class, "DendrogramPlaySheet Hint: SELECT DISTINCT ?var-1 ?var-2 ?var-3 ... (where ?var-2 contains children of ?var-1, ?var-3 of ?var-2, etc.) WHERE { ... }" ),
	Graph( "Graph", GraphPlaySheet.class, "GraphPlaySheet Hint: CONSTRUCT {?subject ?predicate ?object} WHERE{ ... }" ),
	Grid_Scatter( "Grid Scatter", GridScatterSheet.class, "GridScatterSheet Hint: SELECT ?elementName ?xAxisValues ?yAxisValues (OPTIONAL)?zAxisValues WHERE{ ... }" ),
	Heat_Map( "Heat Map", HeatMapPlaySheet.class, "HeatMapPlaySheet Hint: SELECT ?xAxisList ?yAxisList ?numericHeatValue WHERE{ ... } GROUP BY ?xAxisList ?yAxisList" ),
	Parallel_Coordinates( "Parallel Coordinates", ParallelCoordinatesPlaySheet.class, "ParallelCoordinatesPlaySheet Hint: SELECT ?axis1 ?axis2 ?axis3 WHERE{ ... }" ),
	Pie_Chart( "Pie Chart", PieChartPlaySheet.class, "PieChartPlaySheet Hint: SELECT ?wedgeName ?wedgeValue WHERE { ... }" ),
	Sankey_Diagram( "Sankey Diagram", SankeyPlaySheet.class, "SankeyPlaySheet Hint: SELECT ?source ?target ?value ?target2 ?value2 ?target3 ?value3...etc  Note: ?target is the source for ?target2 and ?target2 is the source for ?target3...etc WHERE{ ... }" ),
	US_Heat_Map( "US Heat Map", USHeatMapPlaySheet.class, "USHeatMapPlaySheet Hint: SELECT ?state ?numericHeatValue WHERE{ ... }" ),
	World_Heat_Map( "World Heat Map", WorldHeatMapPlaySheet.class, "WorldHeatMapPlaySheet Hint: SELECT ?country ?numericHeatValue WHERE{ ... }" ),
	Metamodel_Graph( "Metamodel Graph", MetamodelGraphPlaySheet.class, "MetamodelGraphPlaySheet Hint: SELECT DISTINCT ?source ?relation ?target WHERE{ ... }" ),
	AppDupeHeatMap( "Application Duplication Heat Map", AppDupeHeatMapSheet.class, "AppDupeHeatMapPlaySheet Hint: SELECT ?xAxisList ?yAxisList ?numericHeatValue WHERE{ ... } GROUP BY ?xAxisList ?yAxisList" ),
	DupeHeatMap( "Duplication Heat Map", DupeHeatMapSheet.class, "DupeHeatMapPlaySheet Hint: SELECT ?xAxisList ?yAxisList ?numericHeatValue WHERE{ ... } GROUP BY ?xAxisList ?yAxisList" ),
	Update_Query( "Update Query", null, "UpdateQuery Hint: Try a SPARQL query that INSERTs data, DELETEs data, or does both." );

	private final String sheetName;
	private final Class<? extends IPlaySheet> sheetClass;
	private final String sheetHint;

	PlaySheetEnum( String displayName, Class<? extends IPlaySheet> playSheetClass,
			String playSheetHint ) {
		this.sheetName = displayName;
		this.sheetClass = playSheetClass;
		this.sheetHint = playSheetHint;
	}

	public Class<? extends IPlaySheet> getSheetClass() {
		return this.sheetClass;
	}

	public IPlaySheet getSheetInstance() {
		try {
			return getSheetClass().newInstance();
		}
		catch ( InstantiationException | IllegalAccessException e ) {
			Logger.getLogger( getClass() ).warn( "cannot instantiate playsheet class", e );
			return new GridPlaySheet();
		}
	}

	public String getDisplayName() {
		return this.sheetName;
	}

	public String getSheetHint() {
		return this.sheetHint;
	}

	public boolean needsSparql() {
		return !AppDupeHeatMapSheet.class.equals( sheetClass );
	}

	/**
	 * Gets a PlaySheetEnum for the given Insight. If this insight's
	 * {@link Insight#getOutput()} returns an unknown playsheet, this function
	 * returns {@link PlaySheetEnum#Grid}
	 *
	 *
	 * @param ins
	 * @return
	 */
	public static PlaySheetEnum valueFor( Insight ins ) {
		if ( null == ins || null == ins.getOutput() ) {
			return PlaySheetEnum.Update_Query;
		}

		String output = ins.getOutput();
		for ( PlaySheetEnum pse : valuesNoUpdate() ) {
			if ( output.equals( pse.getSheetClass().getCanonicalName() ) ) {
				return pse;
			}
		}

		Logger.getLogger( PlaySheetEnum.class ).warn( "Unknown PSE for output: "
				+ output + " (using Grid instead)" );
		return PlaySheetEnum.Grid;
	}

	public static PlaySheetEnum[] valuesNoUpdate() {
		Set<PlaySheetEnum> pses = EnumSet.allOf( PlaySheetEnum.class );
		pses.remove( PlaySheetEnum.Update_Query );
		return pses.toArray( new PlaySheetEnum[0] );
	}
}
