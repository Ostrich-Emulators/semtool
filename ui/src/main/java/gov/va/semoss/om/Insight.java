package gov.va.semoss.om;

import gov.va.semoss.ui.components.playsheets.PlaySheetCentralComponent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;

public class Insight implements Serializable {

	private static final long serialVersionUID = 5192674160082789840L;
	private static final Logger log = Logger.getLogger( Insight.class );
	//ID of the question:
	private URI id = null;
	//Name of the question:
	private String label = "";
	//Sparql for the question:
	private String sparql = "";
	//Type of entity this insight has:
	private String entityType = "";
	//The layout used to render this insight:
	private String output = "";
	//Description of Insight:
	private String description = "";
	//Author of Insight:
	private String creator = "";
	//Date Created:
	private Date created;
	//Date Modified:
	private Date modified;

	//The default value of this Insight is a Sparql query in most cases.
	//Some Insights depend upon Java renderer classes, instead of queries.
	//For these cases, this value may be altered from within the InsightManager:
	private boolean defautlValueIsQuery = true;

	//InsightParameters:
	private final List<Parameter> parameters = new ArrayList<>();
	private InsightOutputType type;

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
		id = i.getId();
		label = i.getLabel();
		sparql = i.getSparql();

		output = i.getOutput();
		created = i.getCreated();
		modified = i.modified;
		creator = i.getCreator();
		description = i.getDescription();

		entityType = i.entityType;

		defautlValueIsQuery = i.defautlValueIsQuery;

		for ( Parameter p : i.parameters ) {
			parameters.add( new Parameter( p ) );
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

	public InsightOutputType getOutputType() {
		return type;
	}

	public void setOutputType( InsightOutputType type ) {
		this.type = type;
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

	public Collection<Parameter> getInsightParameters() {
		return this.parameters;
	}

	public void setParameters( Collection<Parameter> params ) {
		parameters.clear();
		parameters.addAll( params );
	}

	public boolean hasParameters(){
		return !parameters.isEmpty();
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
	public Date getCreated() {
		return created;
	}

	public void setCreated( Date created ) {
		this.created = created;
	}

	//Date Modified:
	public Date getModified() {
		return modified;
	}

	public void setModified( Date modified ) {
		this.modified = modified;
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
		if ( !Objects.equals( this.label, other.label ) ) {
			return false;
		}
		return true;
	}
}
