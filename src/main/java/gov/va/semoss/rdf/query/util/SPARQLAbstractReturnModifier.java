package gov.va.semoss.rdf.query.util;

import java.util.HashMap;
import java.util.Map;
import gov.va.semoss.rdf.query.util.TriplePart.TriplePartConstant;

public class SPARQLAbstractReturnModifier implements ISPARQLReturnModifier{
	TriplePart triplePart= null;
	String id;
	String type;
	String customModString;
	//hash table for lower mods because generically speaking, you can have multiple mods inside one mod
	//however the generic case, it can only have one modifier 
	public Map<String, ISPARQLReturnModifier> lowerMods = null;
	
	public void createModifier(Object entity, SPARQLModifierConstant type) {
		// TODO Auto-generated method stub
		Map<String, ISPARQLReturnModifier> lowMods = new HashMap<>();
		if (entity instanceof ISPARQLReturnModifier)
		{
			//always size 0 in that hashtable, so replace with 0
			//generic modifier (eg. COUNT, SUM, DISTINCT) only one type of modifier
			((ISPARQLReturnModifier)entity).setModID(MOD+"0");
			lowMods.put(MOD+"0", (ISPARQLReturnModifier)entity);
			setLowerLevelModifier(lowMods);
		}
		else if (entity instanceof TriplePart)
		{
			this.triplePart = (TriplePart) entity;
		}
		else
		{
			throw new IllegalArgumentException("Can only process another SPARQLReturnModifier or another TriplePart");
		}
		setModType(type);
	}
	
	@Override
	public void setLowerLevelModifier(Map<String, ISPARQLReturnModifier> lowerMods) {
		this.lowerMods = lowerMods;
	}

	@Override
	public Map<String, ISPARQLReturnModifier> getLowerLevelModifier() {
		// TODO Auto-generated method stub
		return lowerMods;
	}

	//this class is for generic modifiers only, math modifiers extend this and will override this function
	@Override
	public String getModifierAsString() {
		String modString = "";
		if(lowerMods!=null)
		{
			//don't need to fill really use the ID here because there is only one modifier
			ISPARQLReturnModifier mod = lowerMods.get(MOD+"0");
			modString = type+"("+mod.getModifierAsString()+")";
		}
		else if (triplePart!=null && triplePart.getType().equals(TriplePartConstant.VARIABLE))
		{
			modString = type+"("+SPARQLQueryHelper.createComponentString(triplePart)+")";
		}
		return modString;
	}
	
	public String parameterizeMod(String mod) {
		return "@"+mod+"@";
	}

	@Override
	public void setModID(String id) {
		this.id = id;
	}

	@Override
	public String getModID() {
		return id;
	}

	@Override
	public void setModType(SPARQLModifierConstant modConstant) {
		this.type = modConstant.getConstant();
	}

	@Override
	public String getModType() {
		return type;
	}
	
	
}
