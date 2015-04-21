package gov.va.semoss.ui.components.playsheets;

import gov.va.semoss.rdf.engine.api.IEngine;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;
import org.openrdf.model.Value;

public class PieChartPlaySheet extends BrowserPlaySheet2 {

	private static final Logger logger = Logger.getLogger( PieChartPlaySheet.class );
	private static final long serialVersionUID = 187661L;
	private static final int MAX_DISPLAY_LENGTH = 20;

	public PieChartPlaySheet() {
		super( "/html/RDFSemossCharts/app/piechart.html" );
	}

	@Override
	public void create( List<Value[]> data, List<String> headers, IEngine engine ) {
		setHeaders( headers );
		float valuesTotal = 0L;

		convertUrisToLabels( data, getPlaySheetFrame().getEngine() );

		List<HashMap<String, Object>> dataSeries = new ArrayList<>();
		for ( Value[] thisResult : data ) {
			HashMap<String, Object> thisMap = new HashMap<>();

			String name = "", fullname = "";
			if ( thisResult[0] != null ) {
				fullname = thisResult[0].stringValue().replace( "_", " " );

				name = fullname;
				if ( name.length() > MAX_DISPLAY_LENGTH ) {
					name = name.substring( 0, MAX_DISPLAY_LENGTH ) + "...";
				}
			}

			String valueString = "";
			Float value = 0F;
			if ( thisResult[1] != null ) {
				valueString = thisResult[1].stringValue();
				try {
					value = Float.parseFloat( valueString );
				}
				catch ( NumberFormatException e ) {
					logger.warn( "Could not parse float out of " + valueString + ": " + e, e );
				}
			}
			valuesTotal += value;

			thisMap.put( "name", name );
			thisMap.put( "fullname", fullname );
			thisMap.put( "value", valueString );
			dataSeries.add( thisMap );
		}

		for ( HashMap<String, Object> thisMap : dataSeries ) {
			Float percent = 100F * new Float( thisMap.get( "value" ).toString() ) / valuesTotal;
			thisMap.put( "y", percent );
		}

		Map<String, Object> columnChartHash = new HashMap<>();
		columnChartHash.put( "title", getTitle() );
		columnChartHash.put( "dataSeries", dataSeries );

		addDataHash( columnChartHash );
		createView();
	}
}
