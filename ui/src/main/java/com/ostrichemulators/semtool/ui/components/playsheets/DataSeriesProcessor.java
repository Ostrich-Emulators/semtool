package com.ostrichemulators.semtool.ui.components.playsheets;

import java.util.Collection;

public interface DataSeriesProcessor {

	/** Prepare the data (sorting, etc.) for import into a 
	 * Playsheet or other viewing object 
	 * @param undigestedData The unprocessed data
	 * @return A processed dataset
	 */
	public Object parseData(Object undigestedData);
}
