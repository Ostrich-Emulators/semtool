package gov.va.semoss.ui.components.playsheets.helpers;

import gov.va.semoss.ui.components.playsheets.helpers.DupeHeatMapSheet;

import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

public class DupeHeatMapFunctions {

	private static final Logger logger = Logger.getLogger( DupeHeatMapFunctions.class );

	private DupeHeatMapSheet dupeHeatMapSheet;

	public DupeHeatMapFunctions( DupeHeatMapSheet _dupeHeatMapSheet ) {
		dupeHeatMapSheet = _dupeHeatMapSheet;
	}

	public String refreshFunction( String categoryArray, String thresh ) {
		logger.debug( "refreshFunction called. \ncategoryArray: " + categoryArray + "\nthresh: " + thresh );
		Gson gson = new Gson();

		//get the appropriate parameters from JS
		String[] selectedParams = gson.fromJson( categoryArray, String[].class );
		Hashtable<String, Double> specifiedWeights = gson.fromJson( thresh, Hashtable.class );

		return dupeHeatMapSheet.refreshSimHeat( selectedParams, specifiedWeights ) + "";
	}

	public String barChartFunction( String categoryArray, String thresh ) {
		logger.debug( "barChartFunction called. \ncategoryArray: " + categoryArray + "\nthresh: " + thresh );
		Gson gson = new Gson();

		//get the appropriate parameters from JS
		String[] selectedParams = gson.fromJson( thresh, String[].class );
		Hashtable<String, Double> specifiedWeights = new Hashtable<String, Double>();

		return gson.toJson( dupeHeatMapSheet.getCellBarChartData( categoryArray, selectedParams, specifiedWeights ) ) + "";
	}
}
