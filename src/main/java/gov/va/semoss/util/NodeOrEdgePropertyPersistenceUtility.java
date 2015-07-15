package gov.va.semoss.util;

import gov.va.semoss.om.AbstractNodeEdgeBase;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.ui.components.OperationsProgress;
import gov.va.semoss.ui.components.PlayPane;
import gov.va.semoss.ui.components.ProgressTask;

import org.apache.log4j.Logger;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.StatementImpl;

public class NodeOrEdgePropertyPersistenceUtility {
	private static final Logger log = Logger.getLogger( NodeOrEdgePropertyPersistenceUtility.class );
	private IEngine engine;
	
	public NodeOrEdgePropertyPersistenceUtility(IEngine engine) {
		this.engine = engine;
	}
	
	public void deleteNodeProperty(SEMOSSVertex node, URI name, Value value) {
		ProgressTask pt = new ProgressTask( "Deleting Property from the Knowledge Base", new Runnable() {
			@Override
			public void run() {
				Value subject = node.getValue(Constants.URI_KEY);
				if (subject instanceof Resource) {
					Statement statement = new StatementImpl((Resource)subject, name, value);
					
					engine.removeOwlData(statement);
					engine.commit();
				} else {
					log.warn("Trying to save node property with name: " + name + " and value: " + value + ", but subject: " + subject + " is not an instanceof Resource.");
				}
			}
		} );
		OperationsProgress.getInstance( PlayPane.UIPROGRESS ).add( pt );
	}

	public void saveNodePropertyValue(SEMOSSVertex node, URI name, Value value) {
		ProgressTask pt = new ProgressTask( "Saving Property to the Knowledge Base", new Runnable() {
			@Override
			public void run() {
				Value subject = node.getValue(Constants.URI_KEY);
				if (subject instanceof Resource) {
					Statement statement = new StatementImpl((Resource)subject, name, value);
					
					engine.addOwlData(statement);
					engine.commit();
				} else {
					log.warn("Trying to save node property with name: " + name + " and value: " + value + ", but subject: " + subject + " is not an instanceof Resource.");
				}
			}
		} );
		OperationsProgress.getInstance( PlayPane.UIPROGRESS ).add( pt );
	}

	public void updatePropertyValue(AbstractNodeEdgeBase nodeOrEdge,
			URI name, Value oldValue, Value newValue) {
		deletePropertyValue(nodeOrEdge, name, oldValue);
		savePropertyValue(nodeOrEdge, name, newValue);
	}

	public void savePropertyValue(AbstractNodeEdgeBase nodeOrEdge,
			URI name, Value value) {
		if (nodeOrEdge instanceof SEMOSSVertex) {
			saveNodePropertyValue((SEMOSSVertex)nodeOrEdge, name, value);
		} else {
			saveEdgePropertyValue((SEMOSSEdge)nodeOrEdge, name, value);
		}
	}

	public void deletePropertyValue(AbstractNodeEdgeBase nodeOrEdge,
			URI name, Value oldValue) {
		if (nodeOrEdge instanceof SEMOSSVertex) {
			deleteNodeProperty((SEMOSSVertex)nodeOrEdge, name, oldValue);
		} else {
			deleteEdgeProperty((SEMOSSEdge)nodeOrEdge, name, oldValue);
		}
	}

	public void saveEdgePropertyValue(SEMOSSEdge edge, URI name, Value value) {
		log.warn("This method not yet implemented.");
		throw new UnsupportedOperationException();
	}

	public void deleteEdgeProperty(SEMOSSEdge edge, URI name, Value oldValue) {
		log.warn("This method not yet implemented.");
		throw new UnsupportedOperationException();
	}
}