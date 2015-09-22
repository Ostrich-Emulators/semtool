package gov.va.semoss.ui.components.playsheets.helpers;

import gov.va.semoss.ui.components.playsheets.DataSeriesProcessor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The Column Plot Processor sorts data for appropriate alpha-numeric ordered
 * display on the x-axis of the column graph
 * @author Wayne Warren
 *
 */
public class ColumnPlotDataSeriesProcessor implements DataSeriesProcessor {
	/** The file name of the Column Graph file in the HTML directory */
	public static final String HTML_FILE_NAME = "columnchart.html";
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object parseData(Object undigestedData) {
		if (undigestedData instanceof HashMap) {
			Set<?> keySet = ((HashMap<?, ?>) undigestedData).keySet();
			SortedSet keys = new TreeSet(keySet);
			LinkedHashMap sortedHash = new LinkedHashMap();
			Iterator keyIterator = keys.iterator();
			while (keyIterator.hasNext()) {
				String key = (String) keyIterator.next();
				Object value = ((HashMap<?,?>)undigestedData).get(key);
				sortedHash.put(key, value);
			}
			return sortedHash;
		} 
		else {
			return undigestedData;
		}
	}
}
