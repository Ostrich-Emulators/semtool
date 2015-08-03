package gov.va.semoss.ui.components.playsheets;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The Column Plot Processor sorts data for appropriate alpha-numeric ordered
 * display on the x-axis of the column graph
 * @author Wayne Warren
 *
 */
public class ColumnPlotDataSeriesProcessor implements DataSeriesProcessor {
	/** The file path of the Column Graph file in the HTML directory */
	public static final String HTML_FILEPATH = "";
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public HashMap<?, ?> parseData(HashMap<?, ?> undigestedData) {
		SortedSet keys = new TreeSet(undigestedData.keySet());
		LinkedHashMap sortedHash = new LinkedHashMap();
		Iterator keyIterator = keys.iterator();
		while (keyIterator.hasNext()){
			String key = (String)keyIterator.next();
			Object value = undigestedData.get(key);
			sortedHash.put(key, value);
		}
		return sortedHash;
	}
}
