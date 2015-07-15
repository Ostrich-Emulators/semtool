package gov.va.semoss.util;

import gov.va.semoss.om.AbstractNodeEdgeBase;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

public class NodeOrEdgePropertyPersistenceUtility {
	private static final Logger log = Logger.getLogger( NodeOrEdgePropertyPersistenceUtility.class );
	
	public NodeOrEdgePropertyPersistenceUtility() {
		
	}

	public void deleteNodeProperty(SEMOSSVertex node, URI name) {

	}

	public void saveNodePropertyValue(SEMOSSVertex node, URI name, String datatype, Value value) {

	}

	public void updatePropertyValue(AbstractNodeEdgeBase nodeOrEdge,
			URI name, String datatype, Value value) {
		deletePropertyValue(nodeOrEdge, name);
		savePropertyValue(nodeOrEdge, name, datatype, value);
	}

	public void savePropertyValue(AbstractNodeEdgeBase nodeOrEdge,
			URI name, String datatype, Value value) {
		if (nodeOrEdge instanceof SEMOSSVertex) {
			saveNodePropertyValue((SEMOSSVertex)nodeOrEdge, name, datatype, value);
		} else {
			saveEdgePropertyValue((SEMOSSEdge)nodeOrEdge, name, datatype, value);
		}
	}

	public void deletePropertyValue(AbstractNodeEdgeBase nodeOrEdge,
			URI name) {
		if (nodeOrEdge instanceof SEMOSSVertex) {
			deleteNodeProperty((SEMOSSVertex)nodeOrEdge, name);
		} else {
			deleteEdgeProperty((SEMOSSEdge)nodeOrEdge, name);
		}
	}

	public void saveEdgePropertyValue(SEMOSSEdge edge, URI name,
			String datatype, Value value) {
		log.warn("This method not yet implemented.");
		throw new UnsupportedOperationException();
	}

	public void deleteEdgeProperty(SEMOSSEdge edge, URI name) {
		log.warn("This method not yet implemented.");
		throw new UnsupportedOperationException();
	}
}