package gov.va.semoss.om;

import gov.va.semoss.ui.components.playsheets.PlaySheetCentralComponent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

public class Insight implements Serializable {

	private static final long serialVersionUID = 5192674160082789840L;
	private static final Logger log = Logger.getLogger( Insight.class );
	//ID of the question:
	URI id = null;
	//Name of the question:
	String label = "";
	//Query Parameters:
	Map<String, Map<String, String>> parameters = new HashMap<>();
	//Sparql for the question:
	String sparql = "";
	//Database id indicating Insight location.
	//This may be a URL in memory or a file:
	String databaseID = "";
	//Type of entity this insight has:
	String entityType = "";
	//The layout used to render this insight:
	String output = "";
	//Whether the query uses legacy internal parameter specifications:
	boolean isLegacy = false;
	//Description of Insight:
	String description = "";
	//Author of Insight:
	String creator = "";
	//Date Created:
	String created = "";
	//Date Modified:
	String modified = "";

	//The default value of this Insight is a Sparql query in most cases.
	//Some Insights depend upon Java renderer classes, instead of queries.
	//For these cases, this value may be altered from within the InsightManager:
	boolean defautlValueIsQuery = true;

	//InsightParameters:
	List<Parameter> colInsightParameters = new ArrayList<>();

	public Insight() {
	}

	public Insight( String label, String sparql,
			Class<? extends PlaySheetCentralComponent> output ) {
		this.label = label;
		this.output = output.getCanonicalName();
		this.sparql = sparql;
		this.description = "";
	}

	public Insight( String label ) {
		this.label = label;
	}

	public Insight( URI id, String label ) {
		this.id = id;
		this.label = label;
	}

	public Insight( Insight i ) {
		label = i.getLabel();
		sparql = i.getSparql();

		output = i.getOutput();
		created = i.getCreated();
		modified = i.modified;
		creator = i.getCreator();
		description = i.getDescription();

		databaseID = i.getDatabaseID();
		parameters.putAll( i.parameters );

		entityType = i.entityType;
		isLegacy = i.isLegacy;

		defautlValueIsQuery = i.defautlValueIsQuery;

		for ( Parameter p : i.colInsightParameters ) {
			colInsightParameters.add( new Parameter( p ) );
		}
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
	public void setInsightParameters( Collection<Parameter> colInsightParameters ) {
		this.colInsightParameters.addAll( colInsightParameters );
	}

	public Collection<Parameter> getInsightParameters() {
		return this.colInsightParameters;
	}

	public void setParameter( String variable, String label, String type, String defaultQuery ) {
		Map<String, String> attributes = new HashMap<>();
		attributes.put( "parameterLabel", label );
		attributes.put( "parameterValueType", type );
		if ( defaultQuery != null ) {
			attributes.put( "parameterQuery", defaultQuery );
		}
		parameters.put( variable, attributes );
	}

	public void setParameters( Collection<Parameter> params ) {
		colInsightParameters.clear();
		colInsightParameters.addAll( params );
	}

	public Map<String, Map<String, String>> getParameters() {
		return this.parameters;
	}

	public Set<String> getParametersKeySet() {
		return this.parameters.keySet();
	}

	public String getParameterLabel( String parameterVariableName ) {
		return this.parameters.get( parameterVariableName ).get( "parameterLabel" );
	}

	public String getParameterType( String parameterVariableName ) {
		if ( parameters.containsKey( parameterVariableName ) ) {
			return this.parameters.get( parameterVariableName ).get( "parameterValueType" );
		}

		for ( Parameter p : colInsightParameters ) {
			if ( p.getLabel().equals( parameterVariableName ) ) {
				return p.getParameterType();
			}
		}
		return null;
	}

	public String getParameterQuery( String parameterVariableName ) {
		return this.parameters.get( parameterVariableName ).get( "parameterQuery" );
	}

	public void setLegacy( boolean isLegacy ) {
		this.isLegacy = isLegacy;
	}

	public boolean isLegacy() {
		return this.isLegacy;
	}

	public void setDefaultValueIsQuery( boolean defaultValueIsQuery ) {
		this.defautlValueIsQuery = defaultValueIsQuery;
	}

	public boolean getDefaultValueIsQuery() {
		return this.defautlValueIsQuery;
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
			String parameterVariable = resultSet.getValue( "parameterVariable" ).stringValue();
			String parameterLabel;
			if ( resultSet.getValue( "parameterLabel" ) != null ) {
				parameterLabel = resultSet.getValue( "parameterLabel" ).stringValue();
			}
			else {
				parameterLabel = parameterVariable;
			}
			String parameterType = resultSet.getValue( "parameterValueType" ).stringValue();
			String parameterQuery;
			if ( resultSet.getValue( "parameterQuery" ) != null ) {
				parameterQuery = resultSet.getValue( "parameterQuery" ).stringValue();
			}
			else {
				parameterQuery = "";
			}
			setParameter( parameterVariable, parameterLabel, parameterType, parameterQuery );
		}

		Value isLegacyValue = resultSet.getValue( "isLegacy" );
		if ( isLegacyValue != null ) {
			setLegacy( Boolean.parseBoolean( isLegacyValue.stringValue() ) );
		}
	}

	@Override
	public String toString() {
		return label;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + Objects.hashCode( this.id );
		return hash;
	}

	@Override
	public boolean equals( Object obj ) {
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		final Insight other = (Insight) obj;
		if ( !Objects.equals( this.id, other.id ) ) {
			return false;
		}
		return true;
	}

}
