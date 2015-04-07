package gov.va.semoss.rdf.query.util;

import java.util.List;

public class SPARQLMathModifier extends SPARQLAbstractReturnModifier {

	List dataList;
	List<SPARQLModifierConstant> opList;

	public void createModifier(List dataList, List<SPARQLModifierConstant> opList) {
		this.dataList = dataList;
		this.opList = opList;
		for (Object ls:dataList)
		{
			if (!(ls instanceof ISPARQLReturnModifier) && !(ls instanceof TriplePart) && !(ls instanceof Double) && !(ls instanceof Integer))
			{
				throw new IllegalArgumentException("Only integers, doubles, tripleparts, and SPARQLReturnModifiers can be part of a SPARQLMathModifier");
			}
			else if (!(dataList.size()-1==opList.size()))
			{
				throw new IllegalArgumentException("Number of variables has to be ONE more than number of operators, think \"x * y\"");
			}
		}

	}
	
	@Override
	public String getModifierAsString() {
		
		String modString = "";
		for (int enIdx = 0; enIdx<dataList.size(); enIdx++)
		{
			if(dataList.get(enIdx) instanceof ISPARQLReturnModifier)
			{
				//don't need to fill really use the ID here because there is only one modifier
				ISPARQLReturnModifier mod = (ISPARQLReturnModifier)dataList.get(enIdx);
				modString = modString + "("+mod.getModifierAsString()+")";
			}
			else if (dataList.get(enIdx) instanceof TriplePart)
			{
				TriplePart part= (TriplePart)dataList.get(enIdx);
				modString = modString+SPARQLQueryHelper.createComponentString(part);
			}
			else if (dataList.get(enIdx) instanceof Double || dataList.get(enIdx) instanceof Integer)
			{
				modString = modString + dataList.get(enIdx)+"";
			}
			//add operator if it's not last element
			if(enIdx <dataList.size()-1)
			{
				modString = modString + " " + opList.get(enIdx).getConstant() + " ";
			}
		}
		return modString;
	}
}
