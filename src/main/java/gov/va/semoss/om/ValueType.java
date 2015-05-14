package gov.va.semoss.om;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

/**    Holds main KB Concept Value data for populating a collection for the Insight Manager's 
 * "Value Type..." combo-box.
 * 
 * @author Thomas
 *
 */
public class ValueType {
    private String strValueLabel;
    private String strValueClass;
    
    public ValueType(){   	
    }
    
    public ValueType(String strValueLabel, String strValueClass){   	
    	this.strValueLabel = strValueLabel.replaceAll("\"", "");
    	this.strValueClass = strValueClass.replaceAll("\"", "");
    }
    //Value label (for combo-box):
    public String getValueLabel(){
    	return this.strValueLabel;
    }
    public void setValueLabel(String strLabel){
    	this.strValueLabel = strLabel;
    }
    //Value class:
    public String getValueClass(){
    	return this.strValueClass;
    }
    public void setValueClass(String strConceptClass){
    	this.strValueClass = strConceptClass;
    }
    
	@Override
	public String toString() {
		return strValueLabel;
	}
   
}//End ValueType class.
