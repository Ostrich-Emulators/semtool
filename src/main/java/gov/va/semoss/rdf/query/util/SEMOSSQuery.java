package gov.va.semoss.rdf.query.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import gov.va.semoss.rdf.query.util.TriplePart.TriplePartConstant;

public class SEMOSSQuery {
	private static final String main = "Main";
	private final List<SPARQLTriple> triples = new ArrayList<>();
	private final List<TriplePart> retVars = new ArrayList<>();
	private final Map<TriplePart, ISPARQLReturnModifier> retModifyPhrase = new HashMap<>();
	private final Map<String, SPARQLPatternClause> clauseHash = new HashMap<>();
	private SPARQLGroupBy groupBy = null;
	private String retVarString, wherePatternString, postWhereString;
	private String customQueryStructure ="";
	private String queryType;
	private boolean distinct = false;
	private String query;
	private String queryID;
	
	public SEMOSSQuery()
	{
	}
	
	public SEMOSSQuery(String queryID)
	{
		this.queryID = queryID;
	}
	
	public void setQueryID (String queryID)
	{
		this.queryID = queryID;
	}
	
	public String getQueryID()
	{
		return this.queryID;
	}
	
	public void createQuery()
	{
		query = queryType + " ";
		if(distinct)
			query= query+ "DISTINCT ";
		createReturnVariableString();
		createPatternClauses();
		createPostPatternString();
		query=query+retVarString + wherePatternString +postWhereString;
		
	}
	
	public void addSingleReturnVariable(TriplePart var) 
	{
		if(var.getType().equals(TriplePartConstant.VARIABLE))
		{
			retVars.add(var);
		}
		else
		{
			throw new IllegalArgumentException("Cannot add non-variable parts to the return var string");
		}
	}
	
	public void addSingleReturnVariable(TriplePart var, ISPARQLReturnModifier mod) 
	{
		if(var.getType().equals(TriplePartConstant.VARIABLE))
		{
			retVars.add(var);
			retModifyPhrase.put(var,  mod);
		}
		else
		{
			throw new IllegalArgumentException("Cannot add non-variable parts to the return var string");
		}
	}
	

	
	public void createPatternClauses()
	{
		if(customQueryStructure.equals(""))	{
			wherePatternString= "";
			for( SPARQLPatternClause clause : clauseHash.values() ){
				wherePatternString = wherePatternString + clause.getClauseString();
			}
			wherePatternString = "WHERE { " + wherePatternString + " } ";
		}
		else
		{
      
			wherePatternString= "WHERE { " + customQueryStructure + " } ";
      for( String key : clauseHash.keySet() ){
				SPARQLPatternClause clause = (SPARQLPatternClause) clauseHash.get(key);
				wherePatternString = wherePatternString.replaceAll(key, clause.getClauseString());
			}
		}
	}
	
	public void createReturnVariableString()
	{
		retVarString = "";
    for ( TriplePart retVar : retVars ) {
      //space out the variables
      if ( retModifyPhrase.containsKey( retVar ) ) {
        ISPARQLReturnModifier mod = retModifyPhrase.get( retVar );
        retVarString = retVarString + createRetStringWithMod( retVar, mod ) + " ";
      }
      else {
        retVarString = retVarString + SPARQLQueryHelper.createComponentString( retVar ) + " ";
      }
    }
	}
	
	
	public void createPostPatternString()
	{
		
		if(groupBy != null)
		{
			postWhereString = groupBy.getString();
		}
		else
		{
			postWhereString = "";
		}
	}
	
	public void addTriple(TriplePart subject, TriplePart predicate, TriplePart object)
	{
		SPARQLPatternClause clause;
		if(clauseHash.containsKey(main))
		{
			clause = clauseHash.get(main);
		}
		else
		{
			clause = new SPARQLPatternClause();
		}
		SPARQLTriple newTriple = new SPARQLTriple(subject, predicate, object);
		if(!this.hasTriple(newTriple))
			clause.addTriple(newTriple);
		
		clauseHash.put(main,  clause);
	}
	
	public void addTriple(TriplePart subject, TriplePart predicate, TriplePart object, String clauseName)
	{
		SPARQLPatternClause clause;
		if(clauseHash.containsKey(clauseName))
		{
			clause = clauseHash.get(clauseName);
		}
		else
		{
			clause = new SPARQLPatternClause();
		}
		SPARQLTriple newTriple = new SPARQLTriple(subject, predicate, object);
		if(!this.hasTriple(newTriple))
			clause.addTriple(newTriple);
		
		clauseHash.put(clauseName,  clause);
	}
	
	public boolean hasTriple(SPARQLTriple triple)
	{
		boolean retBoolean = false;
		
		for(String clauseKey : clauseHash.keySet())
		{
			SPARQLPatternClause clause = clauseHash.get(clauseKey);
			if(clause.hasTriple(triple))
			{
				retBoolean = true;
				break;
			}
		}
		return retBoolean;
	}
	
	public void addBind(TriplePart bindSubject, TriplePart bindObject)
	{
		SPARQLPatternClause clause;
		if(clauseHash.containsKey(main))
		{
			clause = clauseHash.get(main);
		}
		else
		{
			clause = new SPARQLPatternClause();
		}
		SPARQLBind newBind = new SPARQLBind(bindSubject, bindObject);
		clause.addBind(newBind);
		clauseHash.put(main,  clause);
	}
	
	public void addBind(TriplePart bindSubject, TriplePart bindObject, String clauseName)
	{
		SPARQLPatternClause clause;
		if(clauseHash.containsKey(clauseName))
		{
			clause = clauseHash.get(clauseName);
		}
		else
		{
			clause = new SPARQLPatternClause();
		}
		SPARQLBind newBind = new SPARQLBind(bindSubject, bindObject);
		clause.addBind(newBind);
		clauseHash.put(clauseName,  clause);
	}
	
	public String createRetStringWithMod(TriplePart var, ISPARQLReturnModifier mod)
	{
		String retString = "("+mod.getModifierAsString()+ " AS " + SPARQLQueryHelper.createComponentString(var)+ ")";
		return retString;
	}
	

	public String getQuery()
	{
		return query;
	}
	
	public String getQueryType()
	{
		return queryType;
	}
	
	public void setQueryType(String queryType)
	{
		if( !(queryType.equals(SPARQLConstants.SELECT)) && !(queryType.equals(SPARQLConstants.CONSTRUCT)))
		{
			throw new IllegalArgumentException("SELECT or CONSTRUCT Queries only");
		}
		this.queryType = queryType;
	}
	
	public List<SPARQLTriple> getTriples()
	{
		return triples;
	}

	
	public List<TriplePart> getRetVars()
	{
		return retVars;
	}
	
	public void setCustomQueryStructure(String structure)
	{
		this.customQueryStructure= structure;
	}
	
	public void setDisctinct(boolean distinct)
	{
		this.distinct=distinct;
	}
	
	public void setGroupBy(SPARQLGroupBy groupBy)
	{
		this.groupBy = groupBy;
	}
	
	public SPARQLGroupBy getGroupBy()
	{
		return this.groupBy;
	}
	
}
