package gov.va.semoss.om;

import java.io.Serializable;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

/**   Holds a Parameter data for one Parameter of an Insight.
 * 
 * @author Thomas
 *
 */
public class Parameter implements Serializable{
	private static final long serialVersionUID = 5672795936332918133L;
    private URI uriId;
	private String strLabel;
    private String strVariable;
    private String strParameterType;
    private String strDefaultQuery;
    private String strParameterURI;
    
    public Parameter(){
    }    
    public Parameter(String strParameterURI, String strLabel, String strVariable, String strParameterType, String strDefaultValue, String strDefaultQuery){
    	this.strParameterURI = strParameterURI;
    	this.strLabel = strLabel;
    	this.strVariable = strVariable;
    	this.strParameterType = strParameterType;
    	this.strDefaultQuery = strDefaultQuery;
    }

    //Parameter URI:
    public URI getParameterId(){
    	return this.uriId;
    }
    public void setParameterId(URI uriId){
    	this.uriId = uriId;
    }    
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
    //Parameter type:
    public String getParameterType(){
    	return this.strParameterType;
    }
    public void setParameterType(String strParameterType){
    	this.strParameterType = strParameterType;
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
    	this.strParameterType = "";
    	this.strDefaultQuery = "";
    	
		Value ParameterURI_Value = resultSet.getValue("parameter");
		if(ParameterURI_Value != null){
			this.uriId = (URI) ParameterURI_Value;
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
		Value parameterTypeValue = resultSet.getValue("parameterValueType");
		if(parameterTypeValue != null){
			this.strParameterType = parameterTypeValue.stringValue();
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
