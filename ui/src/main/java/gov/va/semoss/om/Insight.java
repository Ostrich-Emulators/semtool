package gov.va.semoss.om;

import gov.va.semoss.model.vocabulary.SP;
import gov.va.semoss.model.vocabulary.UI;
import gov.va.semoss.model.vocabulary.VAS;
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
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDFS;
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
	//A renderer class for the Insight (if standard playsheets aren't used):
	String rendererClass = "";
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
	//A URI string of the containing Perspective,
	//for use in the "toString()" method:
	private String perspective = "";
	//This Insight's Order under its Perspective.
	//(Assuming that an Insight can belong to only one Perspective):
	private int order = 0;

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
		return this.parameters.get( parameterVariableName ).get( "parameterValueType" );
	}

	public String getParameterQuery( String parameterVariableName ) {
		return this.parameters.get( parameterVariableName ).get( "parameterQuery" );
	}

	public void setRendererClass( String rendererClass ) {
		this.rendererClass = rendererClass;
	}

	public String getRendererClass() {
		return this.rendererClass;
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

	public void setOrder( int order ) {
		this.order = order;
	}

	public int getOrder() {
		return this.order;
	}

	public String getOrderedLabel() {
		return this.order + ". " + this.label;
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

		Value rendererClass = resultSet.getValue( "rendererClass" );
		if ( rendererClass != null ) {
			setRendererClass( rendererClass.stringValue() );
		}

		Value isLegacyValue = resultSet.getValue( "isLegacy" );
		if ( isLegacyValue != null ) {
			setLegacy( Boolean.parseBoolean( isLegacyValue.stringValue() ) );
		}

		Value ordr = resultSet.getValue( "order" );
		if ( ordr != null ) {
			perspective = resultSet.getValue( "perspective" ).stringValue();
			setOrder( Integer.parseInt( ordr.stringValue() ) );
		}
	}

	public void setFromStatements( Collection<Statement> stmts ) {
//		String isp = "SELECT ?insightLabel ?sparql ?viewClass  ?parameterVariable ?parameterLabel ?parameterValueType ?parameterQuery ?rendererClass ?isLegacy ?perspective ?description ?creator ?created ?modified ?order WHERE { "
//				+ "?insightUriString rdfs:label ?insightLabel ; ui:dataView [ ui:viewClass ?viewClass ] . "
//				+ "OPTIONAL{ ?insightUriString spin:body [ sp:text ?sparql ] } "
//				+ "OPTIONAL{ ?insightUriString spin:constraint ?parameter . "
//				+ "?parameter spl:valueType ?parameterValueType ; rdfs:label ?parameterLabel ; spl:predicate [ rdfs:label ?parameterVariable ] . OPTIONAL{?parameter sp:query [ sp:text ?parameterQuery ] }} "
//				+ "OPTIONAL{ ?insightUriString vas:rendererClass ?rendererClass } "
//				+ "OPTIONAL{ ?insightUriString vas:isLegacy ?isLegacy } "
//				+ "OPTIONAL{ ?insightUriString dcterms:description ?description } "
//				+ "OPTIONAL{ ?insightUriString dcterms:creator ?creator } "
//				+ "OPTIONAL{ ?insightUriString dcterms:created ?created } "
//				+ "OPTIONAL{ ?insightUriString dcterms:modified ?modified } "
//				+ "OPTIONAL{ ?perspective olo:slot [ olo:item ?insightUriString; olo:index ?order ] } "
//				+ "}";
		for ( Statement stmt : stmts ) {
			URI pred = stmt.getPredicate();
			Value val = stmt.getObject();
			if ( val instanceof Literal ) {
				Literal obj = Literal.class.cast( val );

				if ( RDFS.LABEL.equals( pred ) ) {
					setLabel( obj.stringValue() );
				}
				else if ( VAS.isLegacy.equals( pred ) ) {
					setLegacy( obj.booleanValue() );
				}
				else if ( DCTERMS.CREATOR.equals( pred ) ) {
					setCreator( obj.stringValue() );
				}
				else if ( DCTERMS.CREATED.equals( pred ) ) {
					setCreated( obj.stringValue() );
				}
				else if ( DCTERMS.MODIFIED.equals( pred ) ) {
					setModified( obj.stringValue() );
				}
				else if ( DCTERMS.DESCRIPTION.equals( pred ) ) {
					setDescription( obj.stringValue() );
				}
				else if( SP.text.equals( pred ) ){
					setSparql( obj.stringValue() );
				}
				else if( UI.viewClass.equals( pred ) ){
					setOutput( obj.stringValue() );
				}
			}
		}
	}

	@Override
	public String toString() {
		String strReturnValue = "";
		if ( perspective != null && perspective.contains( "Detached-Insight-Perspective" ) ) {
			strReturnValue = label;
		}
		else {
			strReturnValue = getOrderedLabel();
		}
		return strReturnValue;
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
