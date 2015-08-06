package gov.va.semoss.rdf.query.util;

import java.util.Date;

public class TriplePart {
  public static enum TriplePartConstant { VARIABLE, URI, LITERAL };
  
	TriplePartConstant type;
	Object value;
	public TriplePart (Object value, TriplePartConstant type)
	{	
		if (type == TriplePartConstant.VARIABLE || type == TriplePartConstant.URI || type == TriplePartConstant.LITERAL)
		{
			this.type = type;
			this.value = value;
		}
		if (!(value instanceof String) && (type ==TriplePartConstant.VARIABLE || type ==TriplePartConstant.URI))
		{
			throw new IllegalArgumentException("Non-String values cannot be used as a variable part or URI part");
		}
		if (!(value instanceof String) && !(value instanceof Integer) && !(value instanceof Double)&& !(value instanceof Date))
		{
			throw new IllegalArgumentException("Value can only be String, Integer, Double or Date at this moment");
		}
	}
	
	public Object getValue()
	{
		return value;
	}
	
	public TriplePartConstant getType()
	{
		return type;
	}
	
	public String getTypeString()
	{
		return type.toString();
	}
	
}
