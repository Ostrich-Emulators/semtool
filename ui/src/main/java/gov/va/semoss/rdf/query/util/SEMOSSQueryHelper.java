package gov.va.semoss.rdf.query.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gov.va.semoss.rdf.query.util.TriplePart.TriplePartConstant;
import gov.va.semoss.util.Constants;

public abstract class SEMOSSQueryHelper {

	public static void addSingleReturnVarToQuery(String varString, SEMOSSQuery seQuery) 
	{
		TriplePart var = new TriplePart(varString, TriplePartConstant.VARIABLE);
		seQuery.addSingleReturnVariable(var);
	}

	public static void addSingleReturnVarToQuery(String varString, ISPARQLReturnModifier modifier, SEMOSSQuery seQuery) 
	{
		TriplePart var = new TriplePart(varString, TriplePartConstant.VARIABLE);
		seQuery.addSingleReturnVariable(var, modifier);
	}

	public static ISPARQLReturnModifier createReturnModifier(ISPARQLReturnModifier modifier, 
      SPARQLModifierConstant modConst ) {		
		SPARQLAbstractReturnModifier newModifier = new SPARQLAbstractReturnModifier();

		Set<SPARQLModifierConstant> acceptables = new HashSet<>(
        Arrays.asList( SPARQLModifierConstant.SUM,  SPARQLModifierConstant.COUNT,
            SPARQLModifierConstant.DISTINCT ) );
    if( !acceptables.contains( modConst ) ){      
			throw new IllegalArgumentException("Modifiers include only SUM, COUNT, or DISTINCT");
    }

    newModifier.createModifier(modifier, modConst);
		return newModifier;
	}
	
	public static ISPARQLReturnModifier createReturnModifier(String varString, 
      SPARQLModifierConstant modConst) {		
		SPARQLAbstractReturnModifier newModifier = new SPARQLAbstractReturnModifier();
		TriplePart var = new TriplePart(varString, TriplePartConstant.VARIABLE);
		Set<SPARQLModifierConstant> acceptables = new HashSet<>(
        Arrays.asList( SPARQLModifierConstant.SUM,  SPARQLModifierConstant.COUNT,
            SPARQLModifierConstant.DISTINCT ) );
    if( !acceptables.contains( modConst ) ){      
			throw new IllegalArgumentException("Modifiers include only SUM, COUNT, or DISTINCT");
    }
		
		newModifier.createModifier(var, modConst);
		return newModifier;
	}
	
	public static ISPARQLReturnModifier createReturnModifier(List<Object> dataList, 
      List<String> opList) {		
		for (int enIdx = 0; enIdx<dataList.size(); enIdx++)
		{
			if(dataList.get(enIdx) instanceof String)
			{
				TriplePart var = new TriplePart(dataList.get(enIdx), TriplePartConstant.VARIABLE);
				dataList.remove(enIdx);
				dataList.add(enIdx, var);
			}
		}
		List<SPARQLModifierConstant> operatorList = new ArrayList<>();
    Set<SPARQLModifierConstant> acceptables = new HashSet<>(
        Arrays.asList( SPARQLModifierConstant.ADD, SPARQLModifierConstant.SUBTRACT,
            SPARQLModifierConstant.MULTIPLY, SPARQLModifierConstant.DIVIDE));
		for (int opIdx = 0; opIdx<opList.size(); opIdx++)	{
			String opString = opList.get(opIdx);
			SPARQLModifierConstant modConst = SPARQLModifierConstant.fromString( opString );
      if( !acceptables.contains( modConst)){
        throw new IllegalArgumentException("Math operators currently include only +, -, *, /");
      }
      
			operatorList.add(opIdx, modConst);
		}
		SPARQLMathModifier modifier = new SPARQLMathModifier();
		modifier.createModifier( dataList, operatorList);
		return modifier;
	}

	public ISPARQLReturnModifier createReturnModifier(String varString, ISPARQLReturnModifier modifier, SEMOSSQuery seQuery) 
	{
		TriplePart var = new TriplePart(varString, TriplePartConstant.VARIABLE);
		seQuery.addSingleReturnVariable(var, modifier);
		return modifier;
	}

	public static void addConceptTypeTripleToQuery(String variableName, String conceptURI, SEMOSSQuery seQuery)
	{
		TriplePart conceptVar = new TriplePart(variableName, TriplePartConstant.VARIABLE);
		TriplePart typeURI = new TriplePart(Constants.RDFTYPE_URI, TriplePartConstant.URI);
		TriplePart conceptTypeURI = new TriplePart(conceptURI, TriplePartConstant.URI);
		seQuery.addTriple(conceptVar, typeURI, conceptTypeURI);
	}

	public static void addConceptTypeTripleToQuery(String variableName, String conceptURI, SEMOSSQuery seQuery, String clauseName)
	{
		TriplePart conceptVar = new TriplePart(variableName, TriplePartConstant.VARIABLE);
		TriplePart typeURI = new TriplePart(Constants.RDFTYPE_URI, TriplePartConstant.URI);
		TriplePart conceptTypeURI = new TriplePart(conceptURI, TriplePartConstant.URI);
		seQuery.addTriple(conceptVar, typeURI, conceptTypeURI, clauseName);
	}

	public static void addRelationTypeTripleToQuery(String variableName, String relationURI, SEMOSSQuery seQuery)
	{
		TriplePart relationVar = new TriplePart(variableName, TriplePartConstant.VARIABLE);
		TriplePart subPropURI = new TriplePart(Constants.SUBPROPERTY_URI, TriplePartConstant.URI);
		TriplePart relationTypeURI = new TriplePart(relationURI, TriplePartConstant.URI);
		seQuery.addTriple(relationVar, subPropURI, relationTypeURI);
	}

	public static void addRelationTypeTripleToQuery(String variableName, String relationURI, SEMOSSQuery seQuery, String clauseName)
	{
		TriplePart relationVar = new TriplePart(variableName, TriplePartConstant.VARIABLE);
		TriplePart subPropURI = new TriplePart(Constants.SUBPROPERTY_URI, TriplePartConstant.URI);
		TriplePart relationTypeURI = new TriplePart(relationURI, TriplePartConstant.URI);
		seQuery.addTriple(relationVar, subPropURI, relationTypeURI, clauseName);
	}

	public static void addRelationshipVarTripleToQuery(String subject, String predicate, String object, SEMOSSQuery seQuery)
	{
		TriplePart subjectVar = new TriplePart(subject, TriplePartConstant.VARIABLE);
		TriplePart predicateVar = new TriplePart(predicate, TriplePartConstant.VARIABLE);
		TriplePart objectVar = new TriplePart(object, TriplePartConstant.VARIABLE);
		seQuery.addTriple(subjectVar, predicateVar, objectVar);
	}

	public static void addRelationshipVarTripleToQuery(String subject, String predicate, String object, SEMOSSQuery seQuery, String clauseName)
	{
		TriplePart subjectVar = new TriplePart(subject, TriplePartConstant.VARIABLE);
		TriplePart predicateVar = new TriplePart(predicate, TriplePartConstant.VARIABLE);
		TriplePart objectVar = new TriplePart(object, TriplePartConstant.VARIABLE);
		seQuery.addTriple(subjectVar, predicateVar, objectVar, clauseName);
	}

	public static void addGenericTriple(String subject, TriplePartConstant subjectType, String predicate, TriplePartConstant predicateType, String object, TriplePartConstant objectType, SEMOSSQuery seQuery)
	{
		if (subjectType.equals(TriplePartConstant.LITERAL))
			throw new IllegalArgumentException("Subject cannot be a literal");
		else if (predicateType.equals(TriplePartConstant.LITERAL))
			throw new IllegalArgumentException("Predicate cannot be a literal");
		addTriplesToQueryFromGenericCall(subject, subjectType, predicate, predicateType, object, objectType, seQuery);
	}

	public static void addGenericTriple(String subject, TriplePartConstant subjectType, String predicate, TriplePartConstant predicateType, int object, TriplePartConstant objectType, SEMOSSQuery seQuery)
	{
		if (subjectType.equals(TriplePartConstant.LITERAL))
			throw new IllegalArgumentException("Subject cannot be a literal");
		else if (predicateType.equals(TriplePartConstant.LITERAL))
			throw new IllegalArgumentException("Predicate cannot be a literal");
		else if (objectType.equals(TriplePartConstant.VARIABLE) || objectType.equals(TriplePartConstant.URI))
			throw new IllegalArgumentException("Cannot put integer as variable or URI, use String");
		addTriplesToQueryFromGenericCall(subject, subjectType, predicate, predicateType, object, objectType, seQuery);
	}

	public static void addGenericTriple(String subject, TriplePartConstant subjectType, String predicate, TriplePartConstant predicateType, double object, TriplePartConstant objectType, SEMOSSQuery seQuery)
	{
		if (subjectType.equals(TriplePartConstant.LITERAL))
			throw new IllegalArgumentException("Subject cannot be a literal");
		else if (predicateType.equals(TriplePartConstant.LITERAL))
			throw new IllegalArgumentException("Predicate cannot be a literal");
		else if (objectType.equals(TriplePartConstant.VARIABLE) || objectType.equals(TriplePartConstant.URI))
			throw new IllegalArgumentException("Cannot put double as variable or URI, use String");
		addTriplesToQueryFromGenericCall(subject, subjectType, predicate, predicateType, object, objectType, seQuery);
	}

	public static void addGenericTriple(String subject, TriplePartConstant subjectType, String predicate, TriplePartConstant predicateType, Date object, TriplePartConstant objectType, SEMOSSQuery seQuery)
	{
		if (subjectType.equals(TriplePartConstant.LITERAL))
			throw new IllegalArgumentException("Subject cannot be a literal");
		else if (predicateType.equals(TriplePartConstant.LITERAL))
			throw new IllegalArgumentException("Predicate cannot be a literal");
		else if (objectType.equals(TriplePartConstant.VARIABLE) || objectType.equals(TriplePartConstant.URI))
			throw new IllegalArgumentException("Cannot put date as variable or URI, use String");
		addTriplesToQueryFromGenericCall(subject, subjectType, predicate, predicateType, object, objectType, seQuery);
	}

	public static void addGenericTriple(String subject, TriplePartConstant subjectType, String predicate, TriplePartConstant predicateType, String object, TriplePartConstant objectType, SEMOSSQuery seQuery, String clauseName)
	{
		if (subjectType.equals(TriplePartConstant.LITERAL))
			throw new IllegalArgumentException("Subject cannot be a literal");
		else if (predicateType.equals(TriplePartConstant.LITERAL))
			throw new IllegalArgumentException("Predicate cannot be a literal");
		addTriplesToQueryFromGenericCall(subject, subjectType, predicate, predicateType, object, objectType, seQuery, clauseName);
	}

	public static void addGenericTriple(String subject, TriplePartConstant subjectType, String predicate, TriplePartConstant predicateType, int object, TriplePartConstant objectType, SEMOSSQuery seQuery, String clauseName)
	{
		if (subjectType.equals(TriplePartConstant.LITERAL))
			throw new IllegalArgumentException("Subject cannot be a literal");
		else if (predicateType.equals(TriplePartConstant.LITERAL))
			throw new IllegalArgumentException("Predicate cannot be a literal");
		else if (objectType.equals(TriplePartConstant.VARIABLE) || objectType.equals(TriplePartConstant.URI))
			throw new IllegalArgumentException("Cannot put integer as variable or URI, use String");
		addTriplesToQueryFromGenericCall(subject, subjectType, predicate, predicateType, object, objectType, seQuery, clauseName);
	}

	public static void addGenericTriple(String subject, TriplePartConstant subjectType, String predicate, TriplePartConstant predicateType, double object, TriplePartConstant objectType, SEMOSSQuery seQuery, String clauseName)
	{
		if (subjectType.equals(TriplePartConstant.LITERAL))
			throw new IllegalArgumentException("Subject cannot be a literal");
		else if (predicateType.equals(TriplePartConstant.LITERAL))
			throw new IllegalArgumentException("Predicate cannot be a literal");
		else if (objectType.equals(TriplePartConstant.VARIABLE) || objectType.equals(TriplePartConstant.URI))
			throw new IllegalArgumentException("Cannot put double as variable or URI, use String");
		addTriplesToQueryFromGenericCall(subject, subjectType, predicate, predicateType, object, objectType, seQuery, clauseName);
	}

	public static void addGenericTriple(String subject, TriplePartConstant subjectType, String predicate, TriplePartConstant predicateType, Date object, TriplePartConstant objectType, SEMOSSQuery seQuery, String clauseName)
	{
		if (subjectType.equals(TriplePartConstant.LITERAL))
			throw new IllegalArgumentException("Subject cannot be a literal");
		else if (predicateType.equals(TriplePartConstant.LITERAL))
			throw new IllegalArgumentException("Predicate cannot be a literal");
		else if (objectType.equals(TriplePartConstant.VARIABLE) || objectType.equals(TriplePartConstant.URI))
			throw new IllegalArgumentException("Cannot put date as variable or URI, use String");
		addTriplesToQueryFromGenericCall(subject, subjectType, predicate, predicateType, object, objectType, seQuery, clauseName);
	}


	private static void addTriplesToQueryFromGenericCall(String subject, TriplePartConstant subjectType, String predicate, TriplePartConstant predicateType, Object object, TriplePartConstant objectType, SEMOSSQuery seQuery)
	{
		TriplePart subjectPart = new TriplePart(subject, subjectType);
		TriplePart predicatePart = new TriplePart(predicate, predicateType);
		TriplePart objectPart = new TriplePart(object, objectType);
		seQuery.addTriple(subjectPart, predicatePart, objectPart);
	}

	private static void addTriplesToQueryFromGenericCall(String subject, TriplePartConstant subjectType, String predicate, TriplePartConstant predicateType, Object object, TriplePartConstant objectType, SEMOSSQuery seQuery, String clauseName)
	{
		TriplePart subjectPart = new TriplePart(subject, subjectType);
		TriplePart predicatePart = new TriplePart(predicate, predicateType);
		TriplePart objectPart = new TriplePart(object, objectType);
		seQuery.addTriple(subjectPart, predicatePart, objectPart, clauseName);
	}

	public static void addBindPhrase(String bindSubject, TriplePartConstant bindSubjectType, String bindObject, SEMOSSQuery seQuery)
	{
		addBindToQueryFromCall(bindSubject, bindSubjectType, bindObject, seQuery);
	}

	public static void addBindPhrase(int bindSubject, TriplePartConstant bindSubjectType, String bindObject, SEMOSSQuery seQuery)
	{
		if (bindSubjectType.equals(TriplePartConstant.VARIABLE) || bindSubjectType.equals(TriplePartConstant.URI))
			throw new IllegalArgumentException("Cannot put integer as variable or URI, use String");
		addBindToQueryFromCall(bindSubject, bindSubjectType, bindObject, seQuery);
	}

	public static void addBindPhrase(double bindSubject, TriplePartConstant bindSubjectType, String bindObject, SEMOSSQuery seQuery)
	{
		if (bindSubjectType.equals(TriplePartConstant.VARIABLE) || bindSubjectType.equals(TriplePartConstant.URI))
			throw new IllegalArgumentException("Cannot put double as variable or URI, use String");
		addBindToQueryFromCall(bindSubject, bindSubjectType, bindObject, seQuery);
	}

	public static void addBindPhrase(Date bindSubject, TriplePartConstant bindSubjectType, String bindObject, SEMOSSQuery seQuery)
	{
		if (bindSubjectType.equals(TriplePartConstant.VARIABLE) || bindSubjectType.equals(TriplePartConstant.URI))
			throw new IllegalArgumentException("Cannot put Date as variable or URI, use String");
		addBindToQueryFromCall(bindSubject, bindSubjectType, bindObject, seQuery);
	}

	public static void addBindPhrase(String bindSubject, TriplePartConstant bindSubjectType, String bindObject, SEMOSSQuery seQuery, String clauseName)
	{
		addBindToQueryFromCall(bindSubject, bindSubjectType, bindObject, seQuery, clauseName);
	}

	public static void addBindPhrase(int bindSubject, TriplePartConstant bindSubjectType, String bindObject, SEMOSSQuery seQuery, String clauseName)
	{
		if (bindSubjectType.equals(TriplePartConstant.VARIABLE) || bindSubjectType.equals(TriplePartConstant.URI))
			throw new IllegalArgumentException("Cannot put integer as variable or URI, use String");
		addBindToQueryFromCall(bindSubject, bindSubjectType, bindObject, seQuery, clauseName);
	}

	public static void addBindPhrase(double bindSubject, TriplePartConstant bindSubjectType, String bindObject, SEMOSSQuery seQuery, String clauseName)
	{
		if (bindSubjectType.equals(TriplePartConstant.VARIABLE) || bindSubjectType.equals(TriplePartConstant.URI))
			throw new IllegalArgumentException("Cannot put double as variable or URI, use String");
		addBindToQueryFromCall(bindSubject, bindSubjectType, bindObject, seQuery, clauseName);
	}

	public static void addBindPhrase(Date bindSubject, TriplePartConstant bindSubjectType, String bindObject, SEMOSSQuery seQuery, String clauseName)
	{
		if (bindSubjectType.equals(TriplePartConstant.VARIABLE) || bindSubjectType.equals(TriplePartConstant.URI))
			throw new IllegalArgumentException("Cannot put Date as variable or URI, use String");
		addBindToQueryFromCall(bindSubject, bindSubjectType, bindObject, seQuery, clauseName);
	}

	private static void addBindToQueryFromCall(Object bindSubject, TriplePartConstant bindSubjectType, String bindObject, SEMOSSQuery seQuery)
	{
		TriplePart subjectBindPart = new TriplePart(bindSubject, bindSubjectType);
		TriplePart objectBindPart = new TriplePart(bindObject, TriplePartConstant.VARIABLE);
		seQuery.addBind(subjectBindPart, objectBindPart);
	}

	private static void addBindToQueryFromCall(Object bindSubject, TriplePartConstant bindSubjectType, String bindObject, SEMOSSQuery seQuery, String clauseName)
	{
		TriplePart subjectBindPart = new TriplePart(bindSubject, bindSubjectType);
		TriplePart objectBindPart = new TriplePart(bindObject, TriplePartConstant.VARIABLE);
		seQuery.addBind(subjectBindPart, objectBindPart, clauseName);
	}
	
	public static void addGroupByToQuery(List<String> list, SEMOSSQuery seQuery)
	{
		ArrayList<TriplePart> varsList = new ArrayList<>();
		for (int varIdx = 0; varIdx<list.size(); varIdx++)
		{
			TriplePart var = new TriplePart(list.get(varIdx), TriplePartConstant.VARIABLE);
			varsList.add(varIdx, var);
		}
		SPARQLGroupBy groupBy = new SPARQLGroupBy(varsList);
		seQuery.setGroupBy(groupBy);
	}
}
