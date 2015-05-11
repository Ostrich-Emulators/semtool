package gov.va.semoss.om;

import gov.va.semoss.util.Utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openjena.atlas.logging.Log;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

public class Insight {

	//ID of the question:
	URI id = null;
	//Name of the question:
	String label = null;
	//Query Parameters:
	Map<String, Map<String, String>> parameters = new HashMap<>();
	//Sparql for the question:
	String sparql = null;
	//Database id indicating Insight location.
	//This may be a URL in memory or a file:
	String databaseID = null;
	//Type of entity this insight has:
	String entityType = null;
	//The layout used to render this insight:
	String output = null;
	//A renderer class for the Insight (if standard playsheets aren't used):
	String rendererClass = null;
	//Whether the query uses legacy internal parameter specifications:
	boolean isLegacy = false;
	//Description of Insight:
	String description = null;
	//Author of Insight:
	String creator = null;
	//Date Created:
	String created = null;
	//Date Modified:
	String modified = null;
	//A URI string of the containing Perspective,
	//for use in the "toString()" method:
	private String perspective;

	HashMap<String, Integer> order = new HashMap<String, Integer>();
	
	//The default value of this Insight is a Sparql query in most cases.
	//Some Insights depend upon Java renderer classes, instead of queries.
	//For these cases, this value may be altered from within the InsightManager:
	boolean defautlValueIsQuery = true;
	
	//InsightParameters:
	Collection<Parameter> colInsightParameters = new ArrayList<Parameter>();

	public Insight() {
	}

	public Insight( String label ) {
		this.label = label;
	}

	public Insight( URI id, String label ) {
		this.id = id;
		this.label = label;
	}

	public URI getId() {
		return id;
	}

	public String getIdStr() {
		return id.toString();
	}

	public void setId( URI id ) {
		this.id = id;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType( String entityType ) {
		this.entityType = entityType;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput( String output ) {
		this.output = output;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel( String label ) {
		this.label = label;
	}

	public String getSparql() {
		return sparql;
	}

	public void setSparql( String sparql ) {
		this.sparql = sparql;
	}

	public String getDatabaseID() {
		return databaseID;
	}

	public void setDatabaseID( String databaseID ) {
		this.databaseID = databaseID;
	}
	//Collection of InsightParameters for this Insight:
	public void setInsightParameters(Collection<Parameter> colInsightParameters){
		this.colInsightParameters.addAll(colInsightParameters);
	}
	public Collection<Parameter> getInsightParameters(){
		return this.colInsightParameters;
	}

	public void setParameter( String variable, String label, String type, String defaultQuery ) {
		Map<String, String> attributes = new HashMap<>();
		attributes.put("parameterLabel", label);
		attributes.put("parameterValueType", type);
		if ( defaultQuery != null ) {
			attributes.put("parameterQuery", defaultQuery);
		}
		parameters.put(variable, attributes);
	}

	public Map<String, Map<String, String>> getParameters() {
		return this.parameters;
	}

	public Set<String> getParametersKeySet() {
		return this.parameters.keySet();
	}
	
	public String getParameterLabel( String parameterVariableName ) {
		return this.parameters.get(parameterVariableName).get("parameterLabel");
	}

	public String getParameterType( String parameterVariableName ) {
		return this.parameters.get(parameterVariableName).get("parameterValueType");
	}

	public String getParameterQuery( String parameterVariableName ) {
		return this.parameters.get( parameterVariableName ).get("parameterQuery");
	}

	public void setRendererClass( String rendererClass ) {
		this.rendererClass = rendererClass;
	}

	public String getRendererClass() {
		return this.rendererClass;
	}

	public void setIsLegacy( boolean isLegacy ) {
		this.isLegacy = isLegacy;
	}

	public boolean getIsLegacy() {
		return this.isLegacy;
	}

	public void setDefaultValueIsQuery( boolean defaultValueIsQuery ) {
		this.defautlValueIsQuery = defaultValueIsQuery;
	}

	public boolean getDefaultValueIsQuery() {
		return this.defautlValueIsQuery;
	}

	public void setOrder( String perpsective, int index ) {
		this.order.put( perpsective, index );
	}

	public int getOrder( URI perspectiveURI ) {
		return this.order.get( perspectiveURI.stringValue() );
	}

	// this works for the general case and will be suitable for the 2015.01 release:
	public String getOrderedLabel() {
		if ( this.order.size() > 1 ) {
			Log.warn( getClass(), "Insight: " + this.label + " belongs to more than one perspective.  Order returned may be invalid." );
		}
		Set<String> keySet = this.order.keySet();
		String[] perspectives = keySet.toArray( new String[keySet.size()] );

		// String[] perspectives = (String[]) this.order.keySet().toArray();
		return this.order.get( perspectives[0] ) + ". " + this.label;
	}

	public String getOrderedLabel( URI perspectiveURI ) {
		return getOrder( perspectiveURI ) + ". " + this.label;
	}

	//Description of Insight:
	public String getDescription() {
		return description;
	}

	public void setDescription( String description ) {
		this.description = description;
	}

	//Author of Insight:
	public String getCreator() {
		return creator;
	}

	public void setCreator( String creator ) {
		this.creator = creator;
	}

	//Date Created:
	public String getCreated() {
		return created;
	}

	public void setCreated( String created ) {
		this.created = created;
	}

	//Date Modified:
	public String getModified() {
		return modified;
	}

	public void setModified( String modified ) {
		this.modified = modified;
	}

	public void setFromResultSet( BindingSet resultSet ) {
		Value insightLabelValue = resultSet.getValue( "insightLabel" );
		if ( insightLabelValue != null ) {
			setLabel( insightLabelValue.stringValue() );
		}
		Value sparqlValue = resultSet.getValue( "sparql" );
		if ( sparqlValue != null ) {
			setSparql( sparqlValue.stringValue().replace( "\"", "" ) );
		}
		Value viewClassValue = resultSet.getValue( "viewClass" );
		if ( viewClassValue != null ) {
			setOutput( viewClassValue.stringValue() );
		}
		Value descriptionValue = resultSet.getValue( "description" );
		if ( descriptionValue != null ) {
			setDescription( descriptionValue.stringValue() );
		}
		Value creatorValue = resultSet.getValue( "creator" );
		if ( creatorValue != null ) {
			setCreator( creatorValue.stringValue() );
		}
		Value createdValue = resultSet.getValue( "created" );
		if ( createdValue != null ) {
			setCreated( createdValue.stringValue() );
		}
		Value modifiedValue = resultSet.getValue( "modified" );
		if ( modifiedValue != null ) {
			setModified( modifiedValue.stringValue() );
		}

		if ( resultSet.getValue( "parameterVariable" ) != null ) {
			String parameterVariable = resultSet.getValue( "parameterVariable").stringValue();
			String parameterLabel;
			if(resultSet.getValue("parameterLabel") != null){
			   parameterLabel = resultSet.getValue( "parameterLabel" ).stringValue();
			}else{
			   parameterLabel = parameterVariable;
			}
			String parameterType = resultSet.getValue( "parameterValueType").stringValue();
			String parameterQuery;
			if(resultSet.getValue("parameterQuery") != null){
			   parameterQuery = resultSet.getValue("parameterQuery").stringValue();
			}else{
			   parameterQuery = "";
			}
			setParameter( parameterVariable, parameterLabel, parameterType, parameterQuery );
		}
		
		Value rendererClass = resultSet.getValue("rendererClass");
		if(rendererClass != null) {
			setRendererClass( rendererClass.stringValue());
		}

		Value isLegacyValue = resultSet.getValue( "isLegacy" );
		if ( isLegacyValue != null ) {
			setIsLegacy( Boolean.parseBoolean( isLegacyValue.stringValue() ) );
		}
		// an insight order is always with respect to some perspective
		Value ordr = resultSet.getValue( "order" );
		if ( ordr != null ) {
			perspective = resultSet.getValue( "perspective" ).stringValue();
			setOrder( perspective, Integer.parseInt( ordr.stringValue() ) );
		}
	}

//	@Override
//	public String toString() {
//		return "Insight [id: " + getId()
//				+ ", databaseID: " + getDatabaseID()
//				+ ", entityType: " + getEntityType()
//				+ ", label: " + getLabel()
//				+ ", sparql: " + getSparql()
//				+ ", output: " + getOutput() 
//				+ " (or renderer class: " + getRendererClass() + ")]";
//	}
	
	@Override
	public String toString() {
		String strReturnValue = "";
		if(perspective.contains("Detached-Insight-Perspective")){
			strReturnValue = label;
		}else{
			strReturnValue = getOrderedLabel();
		}
		return strReturnValue;
	}

}
