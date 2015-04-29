package gov.va.semoss.util;

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

	public static final Map<String, ImageIcon> defaultIcons = new HashMap<>();
	public static final Set<Class<?>> knownClasses = new LinkedHashSet<>();
	public static final Icon blank;

	static {
		defaultIcons.put( "(Heat Map)",
				new ImageIcon( Utility.loadImage( "icons16/questions_heat_map3_16.png" ) ) );
		defaultIcons.put( HeatMapPlaySheet.class.getName(),
				new ImageIcon( Utility.loadImage( "icons16/questions_heat_map3_16.png" ) ) );

		defaultIcons.put( "(Grid)",
				new ImageIcon( Utility.loadImage( "icons16/questions_grid2_16.png" ) ) );
		defaultIcons.put( GridPlaySheet.class.getName(),
				new ImageIcon( Utility.loadImage( "icons16/questions_grid2_16.png" ) ) );

		defaultIcons.put( "(Raw Grid)",
				new ImageIcon( Utility.loadImage( "icons16/questions_raw_grid2_16.png" ) ) );
		defaultIcons.put( GridRAWPlaySheet.class.getName(),
				new ImageIcon( Utility.loadImage( "icons16/questions_raw_grid2_16.png" ) ) );

		defaultIcons.put( "(Grid Scatter)",
				new ImageIcon( Utility.loadImage( "icons16/questions_grid_scatter1_16.png" ) ) );
		defaultIcons.put( GridScatterSheet.class.getName(),
				new ImageIcon( Utility.loadImage( "icons16/questions_grid_scatter1_16.png" ) ) );

		defaultIcons.put( "(Graph)",
				new ImageIcon( Utility.loadImage( "icons16/questions_graph_16.png" ) ) );
		defaultIcons.put( GraphPlaySheet.class.getName(),
				new ImageIcon( Utility.loadImage( "icons16/questions_graph_16.png" ) ) );

		defaultIcons.put( "(Metamodel Graph)",
				new ImageIcon( Utility.loadImage( "icons16/questions_metamodel1_16.png" ) ) );
		defaultIcons.put( MetamodelGraphPlaySheet.class.getName(),
				new ImageIcon( Utility.loadImage( "icons16/questions_metamodel1_16.png" ) ) );

		defaultIcons.put( "(Pie Chart)",
				new ImageIcon( Utility.loadImage( "icons16/questions_pie_chart1_16.png" ) ) );
		defaultIcons.put( PieChartPlaySheet.class.getName(),
				new ImageIcon( Utility.loadImage( "icons16/questions_pie_chart1_16.png" ) ) );

		defaultIcons.put( "(Column Chart)",
				new ImageIcon( Utility.loadImage( "icons16/questions_bar_chart1_16.png" ) ) );
		defaultIcons.put( ColumnChartPlaySheet.class.getName(),
				new ImageIcon( Utility.loadImage( "icons16/questions_bar_chart1_16.png" ) ) );

		defaultIcons.put( "(Bar Chart)",
				new ImageIcon( Utility.loadImage( "icons16/questions_bar_chart1_16.png" ) ) );

		defaultIcons.put( "(Parallel Coordinates)",
				new ImageIcon( Utility.loadImage( "icons16/questions_parcoords6_16.png" ) ) );
		defaultIcons.put( ParallelCoordinatesPlaySheet.class.getName(),
				new ImageIcon( Utility.loadImage( "icons16/questions_parcoords6_16.png" ) ) );

		defaultIcons.put( "(Dendrogram)",
				new ImageIcon( Utility.loadImage( "icons16/questions_dendrogram1_16.png" ) ) );
		defaultIcons.put( DendrogramPlaySheet.class.getName(),
				new ImageIcon( Utility.loadImage( "icons16/questions_dendrogram1_16.png" ) ) );

		defaultIcons.put( "(Sankey Diagram)",
				new ImageIcon( Utility.loadImage( "icons16/questions_sankey2_16.png" ) ) );
		defaultIcons.put( SankeyPlaySheet.class.getName(),
				new ImageIcon( Utility.loadImage( "icons16/questions_sankey2_16.png" ) ) );

		defaultIcons.put( "(World Heat Map)",
				new ImageIcon( Utility.loadImage( "icons16/questions_world_heat_map3_16.png" ) ) );
		defaultIcons.put( WorldHeatMapPlaySheet.class.getName(),
				new ImageIcon( Utility.loadImage( "icons16/questions_world_heat_map3_16.png" ) ) );

		defaultIcons.put( "(US Heat Map)",
				new ImageIcon( Utility.loadImage( "icons16/questions_us_heat_map1_16.png" ) ) );
		defaultIcons.put( USHeatMapPlaySheet.class.getName(),
				new ImageIcon( Utility.loadImage( "icons16/questions_us_heat_map1_16.png" ) ) );

		defaultIcons.put( "(Update Query)",
				new ImageIcon( Utility.loadImage( "icons16/questions_update2_16.png" ) ) );

		defaultIcons.put( LoadingPlaySheetBase.class.getName(),
				new ImageIcon( Utility.loadImage( "icons16/import_data_review_16.png" ) ) );

		blank = new ImageIcon( Utility.loadImage( "icons16/blank_16.png" ) );

		// when adding new known classes, you must add the subclass before the 
		// superclass, as the list is checked in order
		knownClasses.add( LoadingPlaySheetBase.class );
		knownClasses.add( HeatMapPlaySheet.class );
		knownClasses.add( GridPlaySheet.class );
		knownClasses.add( GridRAWPlaySheet.class );
		knownClasses.add( GridScatterSheet.class );
		knownClasses.add( GraphPlaySheet.class );
		knownClasses.add( MetamodelGraphPlaySheet.class );
		knownClasses.add( PieChartPlaySheet.class );
		knownClasses.add( ColumnChartPlaySheet.class );
		knownClasses.add( ParallelCoordinatesPlaySheet.class );
		knownClasses.add( DendrogramPlaySheet.class );
		knownClasses.add( SankeyPlaySheet.class );
		knownClasses.add( WorldHeatMapPlaySheet.class );
		knownClasses.add( USHeatMapPlaySheet.class );
	}

	public static void setDefaultIcon( Class<?> k, String imgloc ) {
		setDefaultIcon( k.getName(), imgloc );
		knownClasses.add( k );
	}

	public static void setDefaultIcon( String key, String imgloc ) {
		defaultIcons.put( key, new ImageIcon( Utility.loadImage( imgloc ) ) );
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
}
