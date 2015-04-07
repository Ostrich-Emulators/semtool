package gov.va.semoss.rdf.query.util;

import java.util.ArrayList;
import gov.va.semoss.rdf.query.util.TriplePart.TriplePartConstant;



public class SPARQLGroupBy {
	ArrayList<TriplePart> vars;
	public SPARQLGroupBy (ArrayList<TriplePart> vars)
	{
    for ( TriplePart part : vars ) {
      if (!part.getType().equals(TriplePartConstant.VARIABLE))
      {
        throw new IllegalArgumentException("One or more tripleParts is not type variable");
      }
    }
		setVariables(vars);
	}
	
	public ArrayList<TriplePart> getVariables()
	{
		return vars;
	}
	
	public final void setVariables(ArrayList<TriplePart> vars)
	{
		this.vars = vars;
	}
	
	public String getString()
	{
		String retString = "GROUP BY";
    for ( TriplePart var : vars ) {
      retString = retString + " " + SPARQLQueryHelper.createComponentString( var );
    }
		return retString; 
	}
	

}
