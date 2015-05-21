package gov.va.semoss.om;

import org.apache.log4j.Logger;

import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import gov.va.semoss.rdf.query.util.impl.VoidQueryAdapter;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.Utility;
import org.openrdf.model.vocabulary.RDFS;

public class MetamodelGraphDataModel extends GraphDataModel {
	private static final Logger log = Logger.getLogger( MetamodelGraphDataModel.class );

	private DecimalFormat df = new DecimalFormat( "###,###,###,###" );
	private HashMap<String, Integer> conceptInstanceCounts;
	private HashMap<SEMOSSEdge,HashMap<String, Integer>> edgeCounts;
	
	public MetamodelGraphDataModel() {
		super();
		
		setTypeOrSubclass( RDFS.SUBCLASSOF );
		setFilterOutOwlData(false);
		getBaseFilterSet().add(DIHelper.getConceptURI().stringValue() );
		getBaseFilterSet().add(DIHelper.getConceptURI("ApplicationModule").stringValue() );
	}

	  /**
	   * Method fillStoresFromModel. This function requires the rc to be completely
	   * full it will use the rc to create edge and node properties and then nodes
	   * and edges.
	   */
	@Override
	public void fillStoresFromModel() {
		super.fillStoresFromModel();
		
		updateLabelsOfEdges();
		updateLabelsOfVertices();
	}
	
	private void updateLabelsOfVertices() {
		for(SEMOSSVertex vertex:vertStore.values()) {
			vertex.setLabel( vertex.getLabel() + getCountOfInstancesForVertex(vertex) );
		}
	}
	
	private void updateLabelsOfEdges() {
		HashMap<String,String> edgeTypeHash = new HashMap<String,String>();
		for(SEMOSSEdge edge:edgeStore.values()) {
			if (!edgeTypeHash.containsKey(edge.getName()))
				edgeTypeHash.put(edge.getName(), edge.getEdgeType() + getCountOfInstancesForEdge(edge));
			else
				edgeTypeHash.put(edge.getName(), edgeTypeHash.get(edge.getName()) + ", " + edge.getEdgeType() + getCountOfInstancesForEdge(edge));
		}

		for(SEMOSSEdge edge:edgeStore.values()) {
			edge.setName(edgeTypeHash.get(edge.getName()));
		}
	}

	private String getCountOfInstancesForVertex(SEMOSSVertex vertex) {
		if (conceptInstanceCounts==null)
			populateConceptInstanceCounts();
		
		Integer count = conceptInstanceCounts.get(vertex.getURI());
		if (count==null)
			return "";
		
		return " (" + df.format(count) + ")";
	}
	
	private String getCountOfInstancesForEdge(SEMOSSEdge edge) {
		if (edgeCounts==null)
			populateEdgeCounts();
		
		HashMap<String, Integer> hashMap = edgeCounts.get(edge);
		
		Integer count = hashMap.get(edge.getEdgeType());
		if (count==null)
			return "";

		return " (" + df.format(count) + ")";
	}
	
	private void populateConceptInstanceCounts() {
		String query = 
				"SELECT ?concept (COUNT(?instance) AS ?instances) WHERE {" +
				"  {?concept rdfs:subClassOf <" + DIHelper.getConceptURI().stringValue() + "> .}" +
				"  {?instance a ?concept}" +
				"} GROUP BY ?concept";
	        
		final Map<String, String> vals = new HashMap<>();
		VoidQueryAdapter vqa = new VoidQueryAdapter( query ){
			@Override
			public void handleTuple( BindingSet bs, ValueFactory vf ){
				vals.put( bs.getValue( "concept").stringValue(), 
						bs.getValue( "instances").stringValue() );        
			}
		};

		try {
			DIHelper.getInstance().getRdfEngine().query( vqa );
		} catch (Exception e) {
			log.error(e,e);
		}
		
		conceptInstanceCounts = new HashMap<String, Integer>();
		for (Map.Entry<String,String> entry: vals.entrySet()) {
			try {
				conceptInstanceCounts.put(
					entry.getKey(), 
					Integer.parseInt(entry.getValue())
				);
			} catch (NumberFormatException ignored) {}
		}
	}
	
	private void populateEdgeCounts() {
		edgeCounts = new HashMap<SEMOSSEdge,HashMap<String, Integer>>();
		
		int numQueriesRun = 1;
		for(SEMOSSEdge edge:edgeStore.values()) {
			log.debug("populateRelationshipInstanceCount run " + (numQueriesRun++) + " of " + edgeStore.size() + 
					" for subject " + edge.getOutVertex().getURI() + " and object " + edge.getInVertex().getURI());
			edgeCounts.put(edge, populateRelationshipInstanceCount(edge.getOutVertex().getURI().stringValue(),
					edge.getInVertex().getURI().stringValue()));
		}
	}
	
	private HashMap<String, Integer> populateRelationshipInstanceCount(String subjectClass, String objectClass) {
		String query = 
				"SELECT ?property (COUNT(?subPropery) AS ?instances) WHERE {" +
				"  {?property a owl:ObjectProperty .}" +
				"  {?subPropery rdfs:subPropertyOf ?property}" +
				"  {?subject ?subPropery ?object}" +
				"  {?subject a ?subjectClass}" +
				"  {?object  a ?objectClass }" +
				"  BIND( <" + subjectClass + "> AS ?subjectClass)" +
				"  BIND( <" + objectClass  + "> AS ?objectClass )" +
				"} GROUP BY ?property";
	        
		final Map<String, String> vals = new HashMap<>();
		VoidQueryAdapter vqa = new VoidQueryAdapter( query ){
			@Override
			public void handleTuple( BindingSet bs, ValueFactory vf ){
				vals.put( bs.getValue( "property").stringValue(), 
						bs.getValue( "instances").stringValue() );        
			}
		};
		
		try {
			DIHelper.getInstance().getRdfEngine().query( vqa );
		} catch (Exception e) {
			log.error(e,e);
		}
		
		HashMap<String, Integer> relationshipCounts = new HashMap<String, Integer>();
		for (Map.Entry<String,String> entry: vals.entrySet()) {
			try {
				relationshipCounts.put(
					Utility.getInstanceName(entry.getKey()), 
					Integer.parseInt(entry.getValue())
				);
			} catch (NumberFormatException ignored) {}
		}
		
		return relationshipCounts;
	}
}