package gov.va.semoss.om;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

/**   Holds a Parameter data for one Parameter of an Insight.
 * 
 * @author Thomas
 *
 */
public class Parameter {
    private String strLabel;
    private String strVariable;
    private String strValueType;
    private String strDefaultQuery;
    private String strParameterURI;
    
    public Parameter(){
    }    
    public Parameter(String strParameterURI, String strLabel, String strVariable, String strValueType, String strDefaultValue, String strDefaultQuery){
    	this.strParameterURI = strParameterURI;
    	this.strLabel = strLabel;
    	this.strVariable = strVariable;
    	this.strValueType = strValueType;
    	this.strDefaultQuery = strDefaultQuery;
    }

    //Parameter URI:
    public String getParameterURI(){
    	return this.strParameterURI;
    }
    public void setParameterURI(String strParameterURI){
    	this.strParameterURI = strParameterURI;
    }    
    //Parameter label:
    public String getLabel(){
    	return this.strLabel;
    }
    public void setLabel(String strLabel){
    	this.strLabel = strLabel;
    }
    //Parameter variable:
    public String getVariable(){
    	return this.strVariable;
    }
    public void setVariable(String strVariable){
    	this.strVariable = strVariable;
    }
    //Parameter value type:
    public String getValueType(){
    	return this.strValueType;
    }
    public void setValueType(String strValueType){
    	this.strValueType = strValueType;
    }
    //Parameter default query:
    public String getDefaultQuery(){
    	return this.strDefaultQuery;
    }
    public void setDefaultQuery(String strDefaultQuery){
    	this.strDefaultQuery = strDefaultQuery;
    }

    /**   Populates Parameter instance variables from the results of a database fetch.
     * 
     * @param resultSet -- (BindingSet) A row of data corresponding to one Parameter.
     */
    public void setFromResultSet( BindingSet resultSet ) {
    	this.strParameterURI = "";
    	this.strLabel = "";
    	this.strVariable = "";
    	this.strValueType = "";
    	this.strDefaultQuery = "";
    	
		Value ParameterURI_Value = resultSet.getValue("parameter");
		if(ParameterURI_Value != null){
			this.strParameterURI = ParameterURI_Value.stringValue();
		}
		Value labelValue = resultSet.getValue("parameterLabel");
		if(labelValue != null){
			this.strLabel = labelValue.stringValue();
		}
		Value variableValue = resultSet.getValue("parameterVariable");
		if(variableValue != null){
			//A complete URI is loaded for the variable name. We only want the 
			//user to modify the actual name, so only that should be displayed:
			String[] aryVariable = variableValue.stringValue().split("\\#");
			if(aryVariable.length > 1){
			   this.strVariable = aryVariable[1];
			}else{
			   this.strVariable = aryVariable[0];
			}
		}
		Value valueTypeValue = resultSet.getValue("parameterValueType");
		if(valueTypeValue != null){
			this.strValueType = valueTypeValue.stringValue();
		}
		Value defaultQueryValue = resultSet.getValue("parameterQuery");
		if(defaultQueryValue != null){
			this.strDefaultQuery = defaultQueryValue.stringValue();
		}
	}

	@Override
	public String toString() {
		return strLabel;
	}

}//End "Parameter" class.
