/**
 * *****************************************************************************
 * Copyright 2013 SEMOSS.ORG
 *
 * This file is part of SEMOSS.
 *
 * SEMOSS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SEMOSS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SEMOSS. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package gov.va.semoss.om;

import gov.va.semoss.ui.components.models.ValueTableModel;
import gov.va.semoss.ui.helpers.TypeColorShapeTable;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.Utility;

import java.awt.Color;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Objects;
import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

/**
 * Variables are transient because this tells the json writer to ignore them
 */
public class SEMOSSVertex {

	private transient static Logger logger = Logger.getLogger( SEMOSSVertex.class );

	private transient Map<String, String> uriHash = new HashMap<>();
	private transient Map<String, SEMOSSVertex> edgeHash = new HashMap<>();
	private Map<URI, Object> propHash = new HashMap<>();

	private transient List<SEMOSSEdge> inEdge = new ArrayList<>();
	private transient List<SEMOSSEdge> outEdge = new ArrayList<>();

	private transient Color color;
	private transient Shape shape, shapeLegend;
	private String colorString, shapeString;
	private String id;

	/**
	 * Constructor for SEMOSSVertex.
	 *
	 * @param _uri String
	 */
	public SEMOSSVertex( String id ) {
		this( id, null, id );
	}

	public SEMOSSVertex( String id, URI type ) {
		this( id, type, id );
	}

	public SEMOSSVertex( String id, URI type, String name ) {
		this.id = id;
		propHash.put( RDF.SUBJECT, id );
		propHash.put( RDFS.LABEL, name );
		if( null != type ){
			propHash.put( RDF.TYPE, type );
		}
	}

	// this is the out vertex
	public void addInEdge( SEMOSSEdge edge ) {
		inEdge.add( edge );
		propHash.put( Constants.IN_EDGE_CNT, inEdge.size() );

		edgeHash.put(
				edge.getInVertex().getProperty( RDFS.LABEL ).toString(),
				edge.getInVertex() );
		addVertexCounter( edge.getOutVertex() );
	}

	/**
	 * Method addVertexCounter.
	 *
	 * @param outVertex SEMOSSVertex
	 */
	private void addVertexCounter( SEMOSSVertex outVertex ) {
		Integer vertTypeCount = 0;
		try {
			if ( propHash.containsKey( outVertex.getType() ) ) {
				vertTypeCount = (Integer) propHash.get( outVertex.getType() );
			}
			vertTypeCount++;
			propHash.put( new URIImpl( outVertex.getType() ), vertTypeCount );
		}
		catch ( Exception ignored ) {
		}
	}

	// this is the invertex
	public void addOutEdge( SEMOSSEdge edge ) {
		outEdge.add( edge );
		propHash.put( Constants.OUT_EDGE_CNT, outEdge.size() );

		edgeHash.put( edge.getOutVertex().getProperty( RDFS.LABEL ).toString(), 
				edge.getOutVertex() );
		addVertexCounter( edge.getInVertex() );
	}

	/**
	 * Method setProperty.
	 *
	 * @param propNameURI String
	 * @param propValue Object
	 */
	public void setProperty( String propNameURI, Object propValue ) {
		String instanceName = propNameURI;
		try {
			URI prop = new URIImpl( propNameURI );
			
			if( RDF.TYPE.equals( prop ) ){
				propHash.put( prop, propValue );
				TypeColorShapeTable.getInstance().initializeColor( this );
				TypeColorShapeTable.getInstance().initializeShape( this );
			}

			if ( propValue instanceof Literal ) {
				propHash.put( prop, ValueTableModel.getValueFromLiteral( (Literal) propValue ) );
			}
			else {
				propHash.put( prop, ValueTableModel.parseXMLDatatype( propValue.toString() ) );
			}
		}
		catch ( Exception e ) {
			logger.warn( "Could not parse " + propNameURI + " into a URI: " + e, e );
		}

		uriHash.put( instanceName, propNameURI );
	}

	public void setProperty( URI prop, Object val ) {
		setProperty( prop.stringValue(), val );
	}

	/**
	 * Sets a new label for this vertex. This function is really a convenience to
	 * {@link #putProperty(java.lang.String, java.lang.String)}, but doesn't go
	 * through the same error-checking. Any name is acceptable. We can always
	 * rename a label.
	 *
	 * @param label the new label to set
	 */
	public void setLabel( String label ) {
		putProperty( RDFS.LABEL, label );
	}

	public final void setColor( Color _color ) {
		color = _color;
	}

	public Color getColor() {
		return color;
	}

	public void setColorString( String _colorString ) {
		colorString = _colorString;
	}

	public String getColorString() {
		return colorString;
	}

	public void setShape( Shape _shape ) {
		shape = _shape;
	}

	public Shape getShape() {
		return shape;
	}

	public void setShapeString( String _shapeString ) {
		shapeString = _shapeString;
	}

	public String getShapeString() {
		return shapeString;
	}

	public void setShapeLegend( Shape _shapeLegend ) {
		shapeLegend = _shapeLegend;
	}

	public Shape getShapeLegend() {
		return shapeLegend;
	}

	/**
	 *
	 * @return @deprecated
	 */
	@Deprecated
	public Map<String, Object> getProperties() {
		Map<String, Object> map = new HashMap<>();
		for ( Map.Entry<URI, Object> en : propHash.entrySet() ) {
			map.put( en.getKey().stringValue(), en.getValue() );
		}
		return map;
	}

	public Map<URI, Object> getUriProperties() {
		return propHash;
	}

	public Object getProperty( URI arg0 ) {
		return propHash.get( arg0 );
	}

	public final void putProperty( URI propName, String propValue ) {
		propHash.put( propName, propValue );
	}

	public Collection<SEMOSSEdge> getInEdges() {
		return inEdge;
	}

	public Collection<SEMOSSEdge> getOutEdges() {
		return outEdge;
	}

	public String getURI() {
		return getProperty( RDF.SUBJECT ).toString();
	}

	public String getType() {
		return propHash.containsKey( RDF.TYPE ) ? getProperty( RDF.TYPE ).toString() : "";
	}

	public String getLabel() {
		return getProperty( RDFS.LABEL ).toString();
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 47 * hash + Objects.hashCode( this.id );
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
		final SEMOSSVertex other = (SEMOSSVertex) obj;
		if ( !Objects.equals( this.id, other.id ) ) {
			return false;
		}
		return true;
	}
}
