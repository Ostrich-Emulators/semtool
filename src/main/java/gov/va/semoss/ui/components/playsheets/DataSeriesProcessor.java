package gov.va.semoss.ui.components.playsheets;

import java.util.HashMap;

public interface DataSeriesProcessor {

	/** Prepare the data (sorting, etc.) for import into a 
	 * Playsheet or other viewing object 
	 * @param undigestedData The unprocessed data
	 * @return A processed dataset
	 */
	public HashMap<?,?> parseData(HashMap<?,?> undigestedData);
}
