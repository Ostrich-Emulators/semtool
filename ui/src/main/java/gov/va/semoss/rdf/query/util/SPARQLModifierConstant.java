package gov.va.semoss.rdf.query.util;

public enum SPARQLModifierConstant {

  ADD( "+" ), SUBTRACT( "-" ), MULTIPLY( "*" ), DIVIDE( "/" ),
  SUM( "SUM" ), DISTINCT( "DISTINCT" ), COUNT( "COUNT" );

  private final String constant;

  SPARQLModifierConstant( String sm ) {
    constant = sm;
  }

  public String getConstant() {
    return constant;
  }

  public static SPARQLModifierConstant fromString( String consta ) {
    for ( SPARQLModifierConstant c : values() ) {
      if ( c.getConstant().equals( consta ) ) {
        return c;
      }
    }
    throw new IllegalArgumentException( "Unknown constant: " + consta );
  }
}
