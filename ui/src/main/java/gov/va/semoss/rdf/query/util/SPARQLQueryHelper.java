package gov.va.semoss.rdf.query.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import gov.va.semoss.rdf.query.util.TriplePart.TriplePartConstant;

public abstract class SPARQLQueryHelper {
	
	public static String createComponentString(TriplePart triplePart)
	{
		String retString="";
		if(triplePart.getType().equals(TriplePartConstant.VARIABLE))
		{
			retString= createVariableString(triplePart);
		}
		else if(triplePart.getType().equals(TriplePartConstant.URI))
		{
			retString= createURIString(triplePart);
		}
		else if(triplePart.getType().equals(TriplePartConstant.LITERAL))
		{
			retString= createLiteralString(triplePart);
		}
		return retString;
	}

	public static String createVariableString(TriplePart triplePart)
	{
		if(!triplePart.getType().equals(TriplePartConstant.VARIABLE))
		{
			throw new IllegalArgumentException("TriplePart is not set as a variable");
		}
		return "?"+triplePart.getValue();
	}
	
	public static String createURIString(TriplePart triplePart)
	{
		if(!triplePart.getType().equals(TriplePartConstant.URI))
		{
			throw new IllegalArgumentException("TriplePart is not set as a URI");
		}
		return "<"+triplePart.getValue()+">";
	}
	
	public static String createLiteralString(TriplePart triplePart)
	{

		if(!triplePart.getType().equals(TriplePartConstant.LITERAL))
		{
			throw new IllegalArgumentException("TriplePart is not set as a Literal");
		}
		if(triplePart.getValue() instanceof String)
			return "'"+triplePart.getValue()+"'";
		else if(triplePart.getValue() instanceof Double)
			//looks something like "1.0"^^<http://www.w3.org/2001/XMLSchema#double>
			return "\"" +((Double)triplePart.getValue()).toString() + "\""+"^^<"+SPARQLConstants.LIT_DOUBLE_URI+">";
		else if(triplePart.getValue() instanceof Integer)
			//looks something like "1"^^http://www.w3.org/2001/XMLSchema#integer
			return "\"" +((Integer)triplePart.getValue()).toString() + "\""+"^^<"+SPARQLConstants.LIT_INTEGER_URI+">";
		else if(triplePart.getValue() instanceof Date)
		{
			Date value = (Date)triplePart.getValue();
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			String date = df.format(value);
			//looks something like "1"^^http://www.w3.org/2001/XMLSchema#integer
			return "\"" +date + "\""+"^^<"+SPARQLConstants.LIT_DATE_URI+">";
		}

		return "'"+triplePart.getValue()+"'";
	}
}