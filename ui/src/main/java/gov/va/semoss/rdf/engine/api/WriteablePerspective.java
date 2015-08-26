package gov.va.semoss.rdf.engine.api;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

public interface WriteablePerspective {	
	 /**   Loops through each Perspective of the Insight Manager's TreeView, and persists
	  * data (Perspectives, Insights, and Parameters) to the Insights KB on disk. This method
	  * first deletes all Perspectives, Insights, and Parameters from the KB. Then it attempts
	  * to Insert the TreeView data. Should this method encounter an object that cannot be 
	  * persisted, false is returned, and further processing is halted. Otherwise, true is returned.
	  * 
	  * @param olstPerspective -- (ObservableList<TreeItem<Object>>) Children of the root of
	  *    the TreeView.
	  */
     public boolean persistenceWrapper(ObservableList<TreeItem<Object>> olstPerspectives);

	 /**   Prepares a string for use in a dynamic Sparql query, where " and ' are
	  * delimiters. The double-quote, ", is changed to ', and existing single-quotes
	  * are left alone. This utility is also used thoughout the Insight Manager,
	  * where user-editable RDF strings are persisted.
	  *
	  * @param quotedString -- (String) The string containing double and single
	  * quotes.
	  *
	  * @return legalizeQuotes -- (String) The cleaned string, as described above.
	  */
	  public String legalizeQuotes(String quotedString);
	  

}
