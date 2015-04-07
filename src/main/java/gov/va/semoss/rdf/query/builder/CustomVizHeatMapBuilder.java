package gov.va.semoss.rdf.query.builder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

import gov.va.semoss.rdf.query.util.ISPARQLReturnModifier;
import gov.va.semoss.rdf.query.util.SEMOSSQueryHelper;
import gov.va.semoss.rdf.query.util.SPARQLConstants;
import gov.va.semoss.rdf.query.util.SPARQLCustomModifier;

import com.google.gson.internal.StringMap;
import java.util.List;
import gov.va.semoss.rdf.query.util.SPARQLModifierConstant;
import gov.va.semoss.rdf.query.util.TriplePart.TriplePartConstant;

public class CustomVizHeatMapBuilder extends AbstractCustomVizBuilder{
	static final String optionKey = "option";
	static final String MODE_COUNT = "Count";
	static final String MODE_EDGEPROP = "Edge Properties";
	static final String MODE_NODEPROP = "Node Properties";
	static final String MODE_CUSTOM = "Custom";
	static final String relArrayKey = "relTriples";
	static final String relVarArrayKey = "relVarTriples";
	static final String propArrayKey = "propRel";
	static final String propVarArrayKey = "propRelVar";
	static final String xAxisKey = "xAxisName";
	static final String yAxisKey = "yAxisName";
	static final String heatNameKey = "heatValueName";
	static final String operatorKey = "operators";
	static final String customHeatString = "customHeatQueryString";
	static final String uriKey = "uri";
	static final String tripleIdxKey = "tripleIndex";
	static final String edgePropKey = "edgeProps";
	static final String nodePropKey = "nodeProps";
	static final String nodePropName = "nodeName";
	static final String edgePropName = "edgeName";
	static final String customQuery = "heatQueryString";
	
	static final int subIdx = 0;
	static final int predIdx = 1;
	static final int objIdx = 2;
	static final int propIdx = 0;

	
	@Override
	public void buildQuery() {
		String option = (String) allJSONHash.get(optionKey);
		semossQuery.setQueryType(SPARQLConstants.SELECT);
		semossQuery.setDisctinct(true);
		buildQueryCommonParts();
    switch ( option ) {
      case MODE_COUNT:
        buildQueryForCount();
        break;
      case MODE_EDGEPROP:
        buildQueryForEdgeProp();
        break;
      case MODE_NODEPROP:
        buildQueryForNodeProp();
        break;
      case MODE_CUSTOM:
        buildQueryForCustom();
        break;
    }
			
		semossQuery.createQuery();
	}
	
	private void buildQueryCommonParts()
	{

		
		String xAxis = (String) allJSONHash.get(xAxisKey);
		String yAxis = (String) allJSONHash.get(yAxisKey);

		SEMOSSQueryHelper.addSingleReturnVarToQuery(xAxis, semossQuery);
		SEMOSSQueryHelper.addSingleReturnVarToQuery(yAxis, semossQuery);
		
		List<String> groupList  = new ArrayList<>();
		groupList.add(xAxis);
		groupList.add(yAxis);
		SEMOSSQueryHelper.addGroupByToQuery(groupList, semossQuery);
		

		//because of inferencing, we cannot use this..
//		for (int tripleIdx = 0; tripleIdx<tripleArray.size(); tripleIdx++)
//		{
//			String subjectName = tripleVarArray.get(tripleIdx).get(subIdx);
//			String predURIName = tripleVarArray.get(tripleIdx).get(predIdx);
//			String objURIName =	tripleVarArray.get(tripleIdx).get(objIdx);
//			String subjectURI = tripleArray.get(tripleIdx).get(subIdx);
//			String predURI = tripleArray.get(tripleIdx).get(predIdx);
//			String objURI = tripleArray.get(tripleIdx).get(objIdx);
//			SEMOSSQueryHelper.addConceptTypeTripleToQuery(subjectName, subjectURI, semossQuery);
//			SEMOSSQueryHelper.addConceptTypeTripleToQuery(objURIName, objURI, semossQuery);
//			SEMOSSQueryHelper.addRelationTypeTripleToQuery(predURIName, predURI, semossQuery);
//			SEMOSSQueryHelper.addRelationshipVarTripleToQuery(subjectName, predURIName, objURIName, semossQuery);
//		}
		
		//lets' use this version for now

		
	}
	
	private void buildQueryForCount()
	{
		ArrayList<ArrayList<String>> tripleArray = (ArrayList<ArrayList<String>>) allJSONHash.get(relArrayKey);
		ArrayList<ArrayList<String>> tripleVarArray = (ArrayList<ArrayList<String>>) allJSONHash.get(relVarArrayKey);
		for (int tripleIdx = 0; tripleIdx<tripleArray.size(); tripleIdx++)
		{
			String subjectName = tripleVarArray.get(tripleIdx).get(subIdx);
			String objURIName =	tripleVarArray.get(tripleIdx).get(objIdx);
			String subjectURI = tripleArray.get(tripleIdx).get(subIdx);
			String predURI = tripleArray.get(tripleIdx).get(predIdx);
			String objURI = tripleArray.get(tripleIdx).get(objIdx);
			SEMOSSQueryHelper.addConceptTypeTripleToQuery(subjectName, subjectURI, semossQuery);
			SEMOSSQueryHelper.addConceptTypeTripleToQuery(objURIName, objURI, semossQuery);
			//add relationship triples with the relationURI
			SEMOSSQueryHelper.addGenericTriple(subjectName, TriplePartConstant.VARIABLE, 
          predURI, TriplePartConstant.URI, objURIName, TriplePartConstant.VARIABLE, semossQuery);
		}
		String heatValue;
		if((String)allJSONHash.get(heatNameKey)!=null && !((String)allJSONHash.get(heatNameKey)).isEmpty())
		{
			heatValue = (String)allJSONHash.get(heatNameKey);
		}
		else
			heatValue = "HeatValue";
		String yAxis = (String) allJSONHash.get(yAxisKey);
		ISPARQLReturnModifier mod;
		mod = SEMOSSQueryHelper.createReturnModifier(yAxis, SPARQLModifierConstant.COUNT);
		SEMOSSQueryHelper.addSingleReturnVarToQuery(heatValue, mod, semossQuery);
	}
	
	private void buildQueryForEdgeProp()
	{
		List<ArrayList<String>> tripleArray 
        = (List<ArrayList<String>>) allJSONHash.get(relArrayKey);
		List<ArrayList<String>> tripleVarArray 
        = (List<ArrayList<String>>) allJSONHash.get(relVarArrayKey);
		StringMap edgePropMap =(StringMap)allJSONHash.get(edgePropKey);
		List<Object> propList = new ArrayList<>();
		
		for (int tripleIdx = 0; tripleIdx<tripleArray.size(); tripleIdx++)
		{
			String subjectName = tripleVarArray.get(tripleIdx).get(subIdx);
			String predURIName = tripleVarArray.get(tripleIdx).get(predIdx);
			String objURIName =	tripleVarArray.get(tripleIdx).get(objIdx);
			String subjectURI = tripleArray.get(tripleIdx).get(subIdx);
			String predURI = tripleArray.get(tripleIdx).get(predIdx);
			String objURI = tripleArray.get(tripleIdx).get(objIdx);
			SEMOSSQueryHelper.addConceptTypeTripleToQuery(subjectName, subjectURI, semossQuery);
			SEMOSSQueryHelper.addConceptTypeTripleToQuery(objURIName, objURI, semossQuery);
			SEMOSSQueryHelper.addRelationTypeTripleToQuery(predURIName, predURI, semossQuery);
			SEMOSSQueryHelper.addRelationshipVarTripleToQuery(subjectName, predURIName, objURIName, semossQuery);
		}
		Iterator it = edgePropMap.entrySet().iterator();
	    while(it.hasNext()){
	    	Entry pairs = (Entry)it.next();
			StringMap propHash = (StringMap) pairs.getValue();
			int pIdx =  ((Double)propHash.get(tripleIdxKey)).intValue();
			String propURI = (String) propHash.get(uriKey);
			String predVar = tripleVarArray.get(pIdx).get(predIdx);
			SEMOSSQueryHelper.addGenericTriple(predVar, TriplePartConstant.VARIABLE, 
          propURI, TriplePartConstant.URI, pairs.getKey()+"",
          TriplePartConstant.VARIABLE, semossQuery);
			propList.add(pIdx, pairs.getKey());
	    }
		ISPARQLReturnModifier mod;
		ArrayList<String> opList = (ArrayList<String>) allJSONHash.get(operatorKey);
		//first multiply the weights
		mod = SEMOSSQueryHelper.createReturnModifier(propList, opList);
		mod = SEMOSSQueryHelper.createReturnModifier(mod, SPARQLModifierConstant.SUM);
		
		
		String heatValue;
		if((String)allJSONHash.get(heatNameKey)!=null && !((String)allJSONHash.get(heatNameKey)).isEmpty())
		{
			heatValue = (String)allJSONHash.get(heatNameKey);
		}
		else
		{
			heatValue = "HeatValue";
		}
		SEMOSSQueryHelper.addSingleReturnVarToQuery(heatValue, mod, semossQuery);
	}

	private void buildQueryForNodeProp() {
		List<ArrayList<String>> tripleArray 
        = (List<ArrayList<String>>) allJSONHash.get(relArrayKey);
		List<ArrayList<String>> tripleVarArray 
        = (List<ArrayList<String>>) allJSONHash.get(relVarArrayKey);
		StringMap nodePropMap =(StringMap)allJSONHash.get(nodePropKey);
		List<Object> propList = new ArrayList<>();
		
		for (int tripleIdx = 0; tripleIdx<tripleArray.size(); tripleIdx++)
		{
			String subjectName = tripleVarArray.get(tripleIdx).get(subIdx);
			String predURIName = tripleVarArray.get(tripleIdx).get(predIdx);
			String objURIName =	tripleVarArray.get(tripleIdx).get(objIdx);
			String subjectURI = tripleArray.get(tripleIdx).get(subIdx);
			String predURI = tripleArray.get(tripleIdx).get(predIdx);
			String objURI = tripleArray.get(tripleIdx).get(objIdx);
			SEMOSSQueryHelper.addConceptTypeTripleToQuery(subjectName, subjectURI, semossQuery);
			SEMOSSQueryHelper.addConceptTypeTripleToQuery(objURIName, objURI, semossQuery);
			SEMOSSQueryHelper.addRelationTypeTripleToQuery(predURIName, predURI, semossQuery);
			SEMOSSQueryHelper.addRelationshipVarTripleToQuery(subjectName, predURIName, objURIName, semossQuery);
		}
		Iterator it = nodePropMap.entrySet().iterator();
	    while(it.hasNext()){
	    	Entry pairs = (Entry)it.next();
			StringMap propHash = (StringMap) pairs.getValue();
			int pIdx = ((Double) propHash.get(tripleIdxKey)).intValue();
			String propURI = (String) propHash.get(uriKey);
			String nodeVar = (String) propHash.get(nodePropName);
			SEMOSSQueryHelper.addGenericTriple(nodeVar, 
          TriplePartConstant.VARIABLE, propURI, 
          TriplePartConstant.URI, pairs.getKey()+"", 
          TriplePartConstant.VARIABLE, semossQuery);
			propList.add(pIdx, pairs.getKey());
	    }
		ISPARQLReturnModifier mod;
		ArrayList<String> opList = (ArrayList<String>) allJSONHash.get(operatorKey);
		//first multiply the weights
		mod = SEMOSSQueryHelper.createReturnModifier(propList, opList);
		mod = SEMOSSQueryHelper.createReturnModifier(mod, SPARQLModifierConstant.SUM);
		
		
		String heatValue;
		if((String)allJSONHash.get(heatNameKey)!=null && !((String)allJSONHash.get(heatNameKey)).isEmpty())
		{
			heatValue = (String)allJSONHash.get(heatNameKey);
		}
		else
		{
			heatValue = "HeatValue";
		}
		SEMOSSQueryHelper.addSingleReturnVarToQuery(heatValue, mod, semossQuery);
	}

	private void buildQueryForCustom()
	{
		ArrayList<ArrayList<String>> tripleArray = (ArrayList<ArrayList<String>>) allJSONHash.get(relArrayKey);
		ArrayList<ArrayList<String>> tripleVarArray = (ArrayList<ArrayList<String>>) allJSONHash.get(relVarArrayKey);
		StringMap nodePropMap =(StringMap)allJSONHash.get(nodePropKey);
		StringMap edgePropMap =(StringMap)allJSONHash.get(edgePropKey);
		
		for (int tripleIdx = 0; tripleIdx<tripleArray.size(); tripleIdx++)
		{
			String subjectName = tripleVarArray.get(tripleIdx).get(subIdx);
			String predURIName = tripleVarArray.get(tripleIdx).get(predIdx);
			String objURIName =	tripleVarArray.get(tripleIdx).get(objIdx);
			String subjectURI = tripleArray.get(tripleIdx).get(subIdx);
			String predURI = tripleArray.get(tripleIdx).get(predIdx);
			String objURI = tripleArray.get(tripleIdx).get(objIdx);
			SEMOSSQueryHelper.addConceptTypeTripleToQuery(subjectName, subjectURI, semossQuery);
			SEMOSSQueryHelper.addConceptTypeTripleToQuery(objURIName, objURI, semossQuery);
			SEMOSSQueryHelper.addRelationTypeTripleToQuery(predURIName, predURI, semossQuery);
			SEMOSSQueryHelper.addRelationshipVarTripleToQuery(subjectName, predURIName, objURIName, semossQuery);
		}
		Iterator itNode = nodePropMap.entrySet().iterator();
	    while(itNode.hasNext()){
	    	Entry pairs = (Entry)itNode.next();
			StringMap propHash = (StringMap) pairs.getValue();
			String propURI = (String) propHash.get(uriKey);
			String nodeVar = (String) propHash.get(nodePropName);
			SEMOSSQueryHelper.addGenericTriple(nodeVar, 
          TriplePartConstant.VARIABLE, propURI, TriplePartConstant.URI, pairs.getKey()+"", 
          TriplePartConstant.VARIABLE, semossQuery);
	    }
	    Iterator itEdge = edgePropMap.entrySet().iterator();
	    while(itEdge.hasNext()){
	    	Entry pairs = (Entry)itEdge.next();
			StringMap propHash = (StringMap) pairs.getValue();
			String propURI = (String) propHash.get(uriKey);
			String edgeVar = (String) propHash.get(edgePropName);
			SEMOSSQueryHelper.addGenericTriple(edgeVar, 
          TriplePartConstant.VARIABLE, propURI, 
          TriplePartConstant.URI, pairs.getKey()+"", 
          TriplePartConstant.VARIABLE, semossQuery);
	    }
	    
	    String heatQuery = (String) allJSONHash.get(customQuery);
	    
	    SPARQLCustomModifier mod = new SPARQLCustomModifier();
		mod.setModString(heatQuery);
		
		String heatValue;
		if((String)allJSONHash.get(heatNameKey)!=null && !((String)allJSONHash.get(heatNameKey)).isEmpty())
		{
			heatValue = (String)allJSONHash.get(heatNameKey);
		}
		else
		{
			heatValue = "HeatValue";
		}
		SEMOSSQueryHelper.addSingleReturnVarToQuery(heatValue, mod, semossQuery);
	}
}
