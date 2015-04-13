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
    private String strDefaultValue;
    private String strDefaultQuery;
    private String strParameterURI;
    
    public Parameter(){
    }    
    public Parameter(String strParameterURI, String strLabel, String strVariable, String strValueType, String strDefaultValue, String strDefaultQuery){
    	this.strParameterURI = strParameterURI;
    	this.strLabel = strLabel;
    	this.strVariable = strVariable;
    	this.strValueType = strValueType;
    	this.strDefaultValue = strDefaultValue;
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
    //Parameter default value:
    public String getDefaultValue(){
    	return this.strDefaultValue;
    }
    public void setDefaultValue(String strDefaultValue){
    	this.strDefaultValue = strDefaultValue;
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
    	this.strDefaultValue = "";
    	this.strDefaultQuery = "";
    	
		Value ParameterURI_Value = resultSet.getValue("parameter");
		if(ParameterURI_Value != null){
			this.strParameterURI = ParameterURI_Value.stringValue();
		}
		Value labelValue = resultSet.getValue("label");
		if(labelValue != null){
			this.strLabel = labelValue.stringValue();
		}
		Value variableValue = resultSet.getValue("variable");
		if(variableValue != null){
			this.strVariable = variableValue.stringValue();
		}
		Value valueTypeValue = resultSet.getValue("valueType");
		if(valueTypeValue != null){
			this.strValueType = valueTypeValue.stringValue();
		}
		Value defaultValueValue = resultSet.getValue("defaultValue");
		if(defaultValueValue != null){
			this.strDefaultValue = defaultValueValue.stringValue();
		}
		Value defaultQueryValue = resultSet.getValue("defaultQuery");
		if(defaultQueryValue != null){
			this.strDefaultQuery = defaultQueryValue.stringValue();
		}
	}

	@Override
	public String toString() {
		return "Parameter [parameterURI: " + this.strParameterURI
				+ ", label: " + this.strLabel
				+ ", variable: " + this.strVariable
				+ ", valueType: " + this.strValueType
				+ ", defaultValue: " + this.strDefaultValue
				+ ", defaultQuery: " + this.strDefaultQuery + "]";
	}

}//End "Parameter" class.
