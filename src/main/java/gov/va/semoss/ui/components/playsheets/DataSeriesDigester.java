package gov.va.semoss.ui.components.playsheets;

import java.util.Collection;
import java.util.HashMap;

/**
 * The Data Series Digester is responsible for determining which "Processor" is
 * appropriate to pre-process a data series (based on the HTML file path and name)
 * for subsequent display - based on a Factory design pattern.
 * @author Wayne Warren
 *
 */
public class DataSeriesDigester {
	/** The singleton instance */
	private static DataSeriesDigester instance;
	/** The set of parsers, looked up a file name key */
	private static HashMap<String, DataSeriesProcessor> parsers = new HashMap<String, DataSeriesProcessor>();

	/**
	 * The singleton constructor
	 */
	private DataSeriesDigester(){
		parsers.put(ColumnPlotDataSeriesProcessor.HTML_FILE_NAME, new ColumnPlotDataSeriesProcessor());
	}

	/**
	 * Get the singleton instance
	 * @return The singleton instance of this class
	 */
	public static DataSeriesDigester instance(){
		if (instance == null){
			instance = new DataSeriesDigester();
		}
		return instance;
	}
	
	/**
	 * Select the correct Processor type and process the data series for subsequent
	 * display
	 * @param undigestedData The unprocessed data series
	 * @param fileName The file path and name of the HTML file used to render the Play Sheet
	 * @return A processed data series
	 */
	public Object digestData(Object undigestedData, String fileName){
		// Get the parser associated with that file name
		DataSeriesProcessor parser = parsers.get(fileName);
		// If there is no parser, return the data series untouched
		if (parser == null){
			return undigestedData;
		}
		// Otherwise, let the parser handle the data, and return the result
		return parser.parseData(undigestedData);
	}
	
	
}
