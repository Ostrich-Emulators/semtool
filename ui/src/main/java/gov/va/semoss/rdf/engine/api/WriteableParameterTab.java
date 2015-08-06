package gov.va.semoss.rdf.engine.api;

import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Parameter;

public interface WriteableParameterTab {
	
	  /**   Saves the current Parameter to the database.
	   * 
       * @param insight -- (Insight) Insight containing the Parameter to save.
	   * @param parameter -- (Parameter) Parameter to be saved
	   * 
	   * @return saveParameter -- (boolean) Whether the Parameter was saved ok.
	   */
	  public boolean saveParameter(Insight insight, Parameter parameter);

}
