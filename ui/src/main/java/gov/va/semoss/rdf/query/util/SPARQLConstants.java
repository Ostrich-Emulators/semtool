package gov.va.semoss.rdf.query.util;

import org.openrdf.model.vocabulary.XMLSchema;

public abstract class SPARQLConstants {

  public final static String BIND = "BIND";
  public final static String SELECT = "SELECT";
  public final static String CONSTRUCT = "CONSTRUCT";
  public final static String BINDINGS = "BINDINGS";
  public final static String LIT_DOUBLE_URI = XMLSchema.DOUBLE.toString();
  public final static String LIT_INTEGER_URI = XMLSchema.INTEGER.toString();
  public final static String LIT_DATE_URI = XMLSchema.DATETIME.toString();
  public final static String UNION = "UNION";
}
