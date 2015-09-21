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
package gov.va.semoss.om;

/**
 * Defines types of output for an Insight. It's not currently used for anything,
 * but will partially replace the PlaySheetEnum in the future
 */
public enum InsightOutputType {

	GRID, GRID_RAW, GRID_SCATTER, COLUMN_CHART, DENDROGRAM, GRAPH,
	PARALLEL_COORDS, PIE_CHART, SANKEY, HEATMAP, HEATMAP_US, HEATMAP_WORLD,
	HEATMAP_APPDUPE
}
