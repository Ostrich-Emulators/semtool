/*******************************************************************************
 * Copyright 2013 SEMOSS.ORG
 * 
 * This file is part of SEMOSS.
 * 
 * SEMOSS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * SEMOSS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SEMOSS.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package gov.va.semoss.om;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.log4j.Logger;
import org.openrdf.model.Literal;

import gov.va.semoss.util.Constants;
import gov.va.semoss.util.Utility;

/**
 * 
 * @author pkapaleeswaran
 * Something that expresses the edge
 * @version $Revision: 1.0 $
 */
public class SEMOSSEdge implements Comparable<SEMOSSEdge>{
	private static final Logger logger = Logger.getLogger(SEMOSSEdge.class);
	
	private final SEMOSSVertex inVertex;
	private final SEMOSSVertex outVertex;
	private final Map<String, Object> propertyHash = new HashMap<>();

	/**
	 * @param _outVertex
	 * @param _inVertex
	 * @param _uri
	 *  Vertex1 (OutVertex) -------> Vertex2 (InVertex)
	 *  (OutEdge)					(InEdge) 
	 */
	public SEMOSSEdge(SEMOSSVertex _outVertex, SEMOSSVertex _inVertex, String _uri) {
		inVertex = _inVertex;
		outVertex = _outVertex;
		
		inVertex.addInEdge(this);
		outVertex.addOutEdge(this);

		setURI(_uri);
		setEdgeType(Utility.getClassName(_uri));
		setName(Utility.getInstanceName(_uri));
	}
	
	public SEMOSSVertex getInVertex() {
		return inVertex;
	}
	
	public SEMOSSVertex getOutVertex() {
		return outVertex;
	}
	
	public String getEdgeType() {
		return getProperty(Constants.EDGE_TYPE) + "";
	}
	
	public void setEdgeType(String _edgeType) {
		putProperty(Constants.EDGE_TYPE, _edgeType);
	}
	
	public String getName() {
		return getProperty(Constants.EDGE_NAME) + "";
	}
	
	public void setName(String _name) {
		putProperty(Constants.EDGE_NAME, _name);
	}
	
	public String getURI() {
		return getProperty(Constants.URI_KEY) + "";
	}
	
	public final void setURI(String _uri) {
		putProperty(Constants.URI_KEY, _uri);
	}

	/**
	 * Method getProperty.
	 * @param arg0 String
	
	 * @return Object */
	public Object getProperty(String _key) {
		return propertyHash.get(_key);
	}
	
	/**
	 * Method setProperty.
	 * @param propNameURI String
	 * @param propValue Object
	 */
	public void setProperty(String propNameURI, Object propValue) {
		String instanceName = Utility.getInstanceName(propNameURI);
		propertyHash.put(instanceName, propValue);
		logger.debug(getURI() + "<>" + instanceName + "<>" + propValue);
		
		try {
			if(propValue instanceof Literal)
				propertyHash.put(instanceName, ((Literal)propValue).doubleValue());
		} catch(Exception ex) {
			logger.debug(ex);
		}
	}

	/**
	 * Method putProperty.
	 * @param propName String
	 * @param propValue String
	 */
	public final void putProperty(String propName, String propValue){
		propertyHash.put(propName, propValue);
	}
  
	/**
	 * Method getProperties
	
	 * @return Hashtable<String,Object> */
	public Map<String,Object> getProperties() {
		return propertyHash;
	}

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 23 * hash + Objects.hashCode( getURI() );
    hash = 23 * hash + Objects.hashCode( outVertex.getURI() );
    hash = 23 * hash + Objects.hashCode( inVertex.getURI() );
    return hash;
  }

  @Override
  public boolean equals( Object obj ) {
    if ( obj == null || getClass() != obj.getClass() ) {
      return false;
    }
    
    SEMOSSEdge otherEdge = (SEMOSSEdge) obj;
    if ( !Objects.equals( getURI(), otherEdge.getURI() ) ) {
      return false;
    }
    if ( !Objects.equals( outVertex.getURI(), otherEdge.getOutVertex().getURI() ) ) {
      return false;
    }
    if ( !Objects.equals( inVertex.getURI(), otherEdge.getInVertex().getURI() ) ) {
      return false;
    }
    
    return true;
  }

  @Override
  public int compareTo( SEMOSSEdge t ) {
    return toString().compareTo( t.toString() );
  }
  
  @Override
  public String toString(){
    StringBuilder sb = new StringBuilder();
    if( null != outVertex.getURI() ){
      sb.append( outVertex.getURI() );
    }
    
    if( null != getURI() ){
      sb.append( "->").append( getURI() );
    }
    
    if( null != inVertex.getURI() ){
      sb.append( "->").append( inVertex.getURI() );
    }

    return sb.toString();
  }
}
