package gov.va.semoss.om;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

/**    Holds main KB Parameter Type data for populating a collection for the Insight Manager's 
 * "Parameter Type..." combo-box.
 * 
 * @author Thomas
 *
 */
public class ParameterType {
    private String strParameterLabel;
    private String strParameterClass;
    
    public ParameterType(){   	
    }
    
    public ParameterType(String strParameterLabel, String strParameterClass){   	
    	this.strParameterLabel = strParameterLabel.replaceAll("\"", "");
    	this.strParameterClass = strParameterClass.replaceAll("\"", "");
    }
    //Parameter label (for combo-box):
    public String getParameterLabel(){
    	return this.strParameterLabel;
    }
    public void setParameterLabel(String strLabel){
    	this.strParameterLabel = strLabel;
    }
    //Value class:
    public String getParameterClass(){
    	return this.strParameterClass;
    }
    public void setParameterClass(String strConceptClass){
    	this.strParameterClass = strConceptClass;
    }
    
	@Override
	public String toString() {
		return strParameterLabel;
	}
   
}//End ParameterType class.
