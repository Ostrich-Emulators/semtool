package gov.va.semoss.om;

import java.io.Serializable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 * Holds a Parameter data for one Parameter of an Insight.
 *
 * @author Thomas
 *
 */
public class Parameter implements Serializable {

	private static final long serialVersionUID = 5672795936332918133L;
	private URI uriId = null;
	private String strLabel = "";
	private String strParameterType = "";
	private String strDefaultQuery = "";
	private static final Pattern FIRSTVAR = Pattern.compile( "^[^?]+\\?(\\w+).*\\{.*" );

	public Parameter() {
	}

	public Parameter( String label ) {
		strLabel = label;
	}

	public Parameter( String strParameterURI, String strLabel, 
			String strParameterType, String strDefaultQuery ) {
		uriId = new URIImpl( strParameterURI );
		this.strLabel = strLabel;
		this.strParameterType = strParameterType;
		this.strDefaultQuery = strDefaultQuery;
	}

	public Parameter( String label, String query ) {
		this.strLabel = label;
		this.strDefaultQuery = query;

		computeVariableAndTypeFromQuery();
	}

	public Parameter( Parameter p ) {
		this( p.getParameterURI(), p.strLabel, p.strParameterType, p.strDefaultQuery );
	}

	//Parameter URI:
	public URI getId() {
		return this.uriId;
	}

	public void setId( URI uriId ) {
		this.uriId = uriId;
	}

	public void setParameterId( String uriId ) {
		setId( new URIImpl( uriId ) );
	}

	public String getParameterURI() {
		return this.uriId.stringValue();
	}

	//Parameter label:
	public String getLabel() {
		return this.strLabel;
	}

	public void setLabel( String strLabel ) {
		this.strLabel = strLabel;
	}

	//Parameter variable:
	public String getVariable() {
		return getVariableFromSparql( strDefaultQuery.replaceAll( "\n", " " ) );
	}

	//Parameter type:
	public String getParameterType() {
		return this.strParameterType;
	}

	public void setParameterType( String strParameterType ) {
		this.strParameterType = strParameterType;
	}

	//Parameter default query:
	public String getDefaultQuery() {
		return this.strDefaultQuery;
	}

	public void setDefaultQuery( String strDefaultQuery ) {
		this.strDefaultQuery = strDefaultQuery;
		computeVariableAndTypeFromQuery();
	}

	private void computeVariableAndTypeFromQuery() {
		// our parameter variable is the first variable returned in the query
		String nospaces = strDefaultQuery.replaceAll( "\n", " " );
		String strVariable = getVariableFromSparql( nospaces );
		if ( null != strVariable ) {
			Pattern TYPER = Pattern.compile( "\\?" + strVariable
					+ "\\s+(?:a|RDF:TYPE|RDFS:SUBCLASSOF)\\s+([^\\s]+)", Pattern.CASE_INSENSITIVE );
			Matcher t = TYPER.matcher( nospaces );
			if ( t.find() ) {
				strParameterType = t.group( 1 ).replaceAll( "(<|>)", "" );
			}
		}
	}

	/**
	 * Gets the first variable from this sparql (the parameter variable)
	 *
	 * @param query
	 * @return
	 */
	public static String getVariableFromSparql( String query ) {
		String nospaces = query.replaceAll( "\n", " " );
		Matcher m = FIRSTVAR.matcher( nospaces );
		return ( m.matches() ? m.group( 1 ) : null );
	}

	@Override
	public String toString() {
		return strLabel;
	}
}//End "Parameter" class.
