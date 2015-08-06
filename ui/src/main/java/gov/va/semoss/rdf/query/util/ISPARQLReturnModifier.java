package gov.va.semoss.rdf.query.util;

import java.util.Map;

public interface ISPARQLReturnModifier {
	
	//this interface API does not support two modifiers on one level
	public final static String MOD = "MODIFIER";
	
	public void setLowerLevelModifier(Map<String, ISPARQLReturnModifier> lowerMods);
	
	public Map<String, ISPARQLReturnModifier> getLowerLevelModifier();
	
	public String getModifierAsString();
	
	public void setModID(String id);
	
	public String getModID();
	
	public void setModType(SPARQLModifierConstant modConstant);
	
	public String getModType();
	
}
