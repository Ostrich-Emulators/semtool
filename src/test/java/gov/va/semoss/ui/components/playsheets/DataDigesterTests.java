package gov.va.semoss.ui.components.playsheets;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Iterator;

import org.junit.Test;

public class DataDigesterTests {

	@Test
	public void testDigestData() {
		HashMap<String,Integer> data = new HashMap<String,Integer>();
		data.put("Alpha", 123);
		data.put("Charlie", 789);
		data.put("Beta", 456);
		
		HashMap<?,?> digestedData = (HashMap<?,?>)DataSeriesDigester.instance().digestData(data, 
				ColumnPlotDataSeriesProcessor.HTML_FILE_NAME);
		if (digestedData == null){
			fail("Transformed data was return null");
		}
		@SuppressWarnings("unchecked")
		Iterator<String> iterator = (Iterator<String>) digestedData.keySet().iterator();
		int counter = 0;
		while (iterator.hasNext()){
			String key = iterator.next();
			switch (counter){
			case 0: {
				if (!key.equals("Alpha")){
					fail("Keys returned out of order");
				}
			}
			case 1: {
				if (!key.equals("Beta")){
					fail("Keys returned out of order");
				}
			}
			case 2: {
				if (!key.equals("Charlie")){
					fail("Keys returned out of order");
				}
			}
			}
			counter++;
		}
		fail("Not yet implemented");
	}

}
