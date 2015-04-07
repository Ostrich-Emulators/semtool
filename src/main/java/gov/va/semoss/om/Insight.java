package gov.va.semoss.om;

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
	Map<String, Map<String, Value>> parameters = new HashMap<>();
	//Sparql for the question:
	String sparql = null;
	//Database id indicating Insight location.
	//This may be a URL in memory or a file:
	String databaseID = null;
	//Type of entity this insight has:
	String entityType = null;
	//The layout used to render this insight:
	String output = null;
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

	HashMap<String, Integer> order = new HashMap<String, Integer>();
	boolean defautlValueIsQuery = false;
	
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

	public void setParameter( String variable, URI type, Value defaultValue ) {
		Map<String, Value> attributes = new HashMap<>();
		attributes.put( "type", type );
		if ( defaultValue != null ) {
			attributes.put( "defaultValue", defaultValue );
		}

		parameters.put( variable, attributes );
	}

	public Map<String, Map<String, Value>> getParameters() {
		return this.parameters;
	}

	public Set<String> getParametersKeySet() {
		return this.parameters.keySet();
	}

	public URI getParameterType( String parameterName ) {
		return (URI) this.parameters.get( parameterName ).get( "type" );
	}

	// presently the default value is always a query string, but this could change
	public Value getParameterDefaultValue( String parameterName ) {
		return this.parameters.get( parameterName ).get( "defaultValue" );
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
			Value parameterDefaultValue;
			if ( resultSet.getValue( "defaultValueQuery" ) == null ) {
				parameterDefaultValue = resultSet.getValue( "parameterDefaultValueQuery" );
			}
			else {
				parameterDefaultValue = resultSet.getValue( "defaultValueQuery" );
				setDefaultValueIsQuery( true );
			}

			setParameter( resultSet.getValue( "parameterVariable" ).stringValue(), (URI) resultSet.getValue( "parameterValueType" ), parameterDefaultValue );
		}

		Value isLegacyValue = resultSet.getValue( "isLegacy" );
		if ( isLegacyValue != null ) {
			setIsLegacy( Boolean.parseBoolean( isLegacyValue.stringValue() ) );
		}
		// an insight order is always with respect to some perspective
		Value ordr = resultSet.getValue( "order" );
		if ( ordr != null ) {
			Value perspective = resultSet.getValue( "perspective" );
			setOrder( perspective.stringValue(), Integer.parseInt( ordr.stringValue() ) );
		}
	}

	@Override
	public String toString() {
		return "Insight [id: " + getId()
				+ ", databaseID: " + getDatabaseID()
				+ ", entityType: " + getEntityType()
				+ ", label: " + getLabel()
				+ ", sparql: " + getSparql()
				+ ", output: " + getOutput() + "]";
	}

}
