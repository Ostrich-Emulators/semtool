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

import gov.va.semoss.ui.helpers.TypeColorShapeTable;
import gov.va.semoss.util.Constants;

import java.awt.Color;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

/**
 * Variables are transient because this tells the json writer to ignore them
 */
public class SEMOSSVertex extends AbstractNodeEdgeBase {

	private transient static Logger logger = Logger.getLogger( SEMOSSVertex.class );

	private transient Map<String, String> uriHash = new HashMap<>();
	private transient Map<String, SEMOSSVertex> edgeHash = new HashMap<>();

	private transient List<SEMOSSEdge> inEdge = new ArrayList<>();
	private transient List<SEMOSSEdge> outEdge = new ArrayList<>();

	private transient Color color;
	private transient Shape shape, shapeLegend;
	private String colorString, shapeString;

	public SEMOSSVertex( URI id ) {
		this( id, null, id.getLocalName() );
	}

	public SEMOSSVertex( URI id, URI type, String label ) {
		super( id, type, label );

		TypeColorShapeTable.getInstance().initializeColor( this );
		TypeColorShapeTable.getInstance().initializeShape( this );
	}

	public void setProperty( URI prop, Object val ) {
		super.setProperty( prop, val );
		if ( RDF.TYPE.equals( prop ) ) {
			TypeColorShapeTable.getInstance().initializeColor( this );
			TypeColorShapeTable.getInstance().initializeShape( this );
		}
	}

	// this is the out vertex
	public void addInEdge( SEMOSSEdge edge ) {
		inEdge.add( edge );
		setProperty( Constants.IN_EDGE_CNT, inEdge.size() );

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
			if ( hasProperty( outVertex.getType() ) ) {
				vertTypeCount = (Integer) getProperty( outVertex.getType() );
			}
			vertTypeCount++;
			setProperty( outVertex.getType(), vertTypeCount );
		}
		catch ( Exception ignored ) {
		}
	}

	// this is the invertex
	public void addOutEdge( SEMOSSEdge edge ) {
		outEdge.add( edge );
		setProperty( Constants.OUT_EDGE_CNT, outEdge.size() );

		edgeHash.put( edge.getOutVertex().getProperty( RDFS.LABEL ).toString(),
				edge.getOutVertex() );
		addVertexCounter( edge.getInVertex() );
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

	public Collection<SEMOSSEdge> getInEdges() {
		return inEdge;
	}

	public Collection<SEMOSSEdge> getOutEdges() {
		return outEdge;
	}
}
