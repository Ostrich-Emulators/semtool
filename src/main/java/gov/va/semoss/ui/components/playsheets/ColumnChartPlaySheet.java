package gov.va.semoss.ui.components.playsheets;

import gov.va.semoss.rdf.engine.api.IEngine;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;

public class ColumnChartPlaySheet extends BrowserPlaySheet2 {
	private static final long serialVersionUID = 164953538466235737L;

	public ColumnChartPlaySheet() {
		super( "/html/RDFSemossCharts/app/columnchart.html" );
	}

	@Override
	public void create( List<Value[]> newdata, List<String> headers, IEngine engine ) {
		setHeaders( headers );
		convertUrisToLabels( newdata, getPlaySheetFrame().getEngine() );

		Map<String, List<Object>> data = new HashMap<>();
		for ( Value[] elemValues : newdata ) {
			List<Object> values = new ArrayList<>();
			for ( int j = 1; j < elemValues.length; j++ ) {
				Literal v = Literal.class.cast( elemValues[j] );
				double dbl = v.doubleValue();
				values.add( dbl );
			}
			data.put( elemValues[0].stringValue(), values );
		}

		Map<String, Object> columnChartHash = new HashMap<>();
		columnChartHash.put( "xAxis",
				headers.subList( 1, headers.size() ).toArray( new String[0] ) );
		columnChartHash.put( "type", "column" );
		columnChartHash.put( "title", getTitle() );
		columnChartHash.put( "dataSeries", data );

		addDataHash( columnChartHash );
		createView();
	}
}
