package gov.va.semoss.util;

import gov.va.semoss.ui.components.api.IPlaySheet;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import gov.va.semoss.ui.components.playsheets.ColumnChartPlaySheet;
import gov.va.semoss.ui.components.playsheets.GridPlaySheet;
import gov.va.semoss.ui.components.playsheets.GridRAWPlaySheet;
import gov.va.semoss.ui.components.playsheets.GridScatterSheet;
import gov.va.semoss.ui.components.playsheets.HeatMapPlaySheet;
import gov.va.semoss.ui.components.playsheets.MetamodelGraphPlaySheet;
import gov.va.semoss.ui.components.playsheets.ParallelCoordinatesPlaySheet;
import gov.va.semoss.ui.components.playsheets.PieChartPlaySheet;
import gov.va.semoss.ui.components.playsheets.SankeyPlaySheet;
import gov.va.semoss.ui.components.playsheets.WorldHeatMapPlaySheet;
import gov.va.semoss.ui.components.playsheets.USHeatMapPlaySheet;
import gov.va.semoss.ui.components.playsheets.DendrogramPlaySheet;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.ui.components.playsheets.LoadingPlaySheetBase;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Provides a HashMap of icons representing the various playsheets available.
 *
 * @author Thomas
 *
 */
public class DefaultPlaySheetIcons {

	private static final Set<Class<?>> knownClasses = new LinkedHashSet<>();
	public static final Map<String, ImageIcon> defaultIcons = new HashMap<>();
	public static final Icon blank;

	private DefaultPlaySheetIcons() {
	}

	static {
		// when adding new known classes, you must add the subclass before the 
		// superclass, as the list is checked in order
		blank = new ImageIcon( GuiUtility.loadImage( "icons16/blank_16.png" ) );

		setDefaultIcon( LoadingPlaySheetBase.class, "icons16/import_data_review_16.png" );

		setDefaultIcon( "(Heat Map)", "icons16/questions_heat_map3_16.png" );
		setDefaultIcon( HeatMapPlaySheet.class, "icons16/questions_heat_map3_16.png" );

		setDefaultIcon( "(Grid)", "icons16/questions_grid2_16.png" );
		setDefaultIcon( GridPlaySheet.class, "icons16/questions_grid2_16.png" );

		setDefaultIcon( "(Raw Grid)", "icons16/questions_raw_grid2_16.png" );
		setDefaultIcon( GridRAWPlaySheet.class, "icons16/questions_raw_grid2_16.png" );

		setDefaultIcon( "(Grid Scatter)", "icons16/questions_grid_scatter1_16.png" );
		setDefaultIcon( GridScatterSheet.class, "icons16/questions_grid_scatter1_16.png" );

		setDefaultIcon( "(Graph)", "icons16/questions_graph_16.png" );
		setDefaultIcon( GraphPlaySheet.class, "icons16/questions_graph_16.png" );

		setDefaultIcon( "(Metamodel Graph)", "icons16/questions_metamodel1_16.png" );
		setDefaultIcon( MetamodelGraphPlaySheet.class, "icons16/questions_metamodel1_16.png" );

		setDefaultIcon( "(Pie Chart)", "icons16/questions_pie_chart1_16.png" );
		setDefaultIcon( PieChartPlaySheet.class, "icons16/questions_pie_chart1_16.png" );

		setDefaultIcon( "(Column Chart)", "icons16/questions_bar_chart1_16.png" );
		setDefaultIcon( ColumnChartPlaySheet.class, "icons16/questions_bar_chart1_16.png" );

		setDefaultIcon( "(Bar Chart)", "icons16/questions_bar_chart1_16.png" );

		setDefaultIcon( "(Parallel Coordinates)", "icons16/questions_parcoords6_16.png" );
		setDefaultIcon( ParallelCoordinatesPlaySheet.class, "icons16/questions_parcoords6_16.png" );

		setDefaultIcon( "(Dendrogram)", "icons16/questions_dendrogram1_16.png" );
		setDefaultIcon( DendrogramPlaySheet.class, "icons16/questions_dendrogram1_16.png" );

		setDefaultIcon( "(Sankey Diagram)", "icons16/questions_sankey2_16.png" );
		setDefaultIcon( SankeyPlaySheet.class, "icons16/questions_sankey2_16.png" );

		setDefaultIcon( "(World Heat Map)", "icons16/questions_world_heat_map3_16.png" );
		setDefaultIcon( WorldHeatMapPlaySheet.class, "icons16/questions_world_heat_map3_16.png" );

		setDefaultIcon( "(US Heat Map)", "icons16/questions_us_heat_map1_16.png" );
		setDefaultIcon( USHeatMapPlaySheet.class, "icons16/questions_us_heat_map1_16.png" );

		setDefaultIcon( "(Update Query)", "icons16/questions_update2_16.png" );
	}

	public static void setDefaultIcon( Class<?> k, String imgloc ) {
		setDefaultIcon( k.getName(), imgloc );
		knownClasses.add( k );
	}

	public static void setDefaultIcon( String key, String imgloc ) {
		defaultIcons.put( key, GuiUtility.loadImageIcon( imgloc ) );
	}

	public static ImageIcon getDefaultIcon( Class<?> klass ) {
		if ( defaultIcons.containsKey( klass.getName() ) ) {
			return defaultIcons.get( klass.getName() );
		}

		// we didn't find an exact match, so see if we can find a match to a superclass
		for ( Class<?> k : knownClasses ) {
			if ( k.isAssignableFrom( klass ) ) {
				return defaultIcons.get( k.getName() );
			}
		}

		return null;
	}

	public static ImageIcon getDefaultIcon( PlaySheetEnum pse ) {
		Class<? extends IPlaySheet> klass = pse.getSheetClass();
		return ( null == klass
				? defaultIcons.get( "(Update Query)" ) : getDefaultIcon( klass ) );
	}
}
