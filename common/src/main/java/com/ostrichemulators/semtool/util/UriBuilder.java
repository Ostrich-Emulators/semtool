/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * A class that helps to build IRIs.
 *
 * @author ryan
 */
public class UriBuilder {

  private static final Logger log = Logger.getLogger( UriBuilder.class );
  private static Class<? extends UriBuilder> bldrclass = UriBuilder.class;
  private static Class<? extends UriSanitizer> saniclass = DefaultSanitizer.class;
  private final ValueFactory vf = SimpleValueFactory.getInstance();
  private final StringBuilder content = new StringBuilder();
  private UriSanitizer sanitizer;
  private boolean lastIsConcatenator;
  private static final Pattern CONCAT_PAT = Pattern.compile( "(.*)([/#:])$" );
  private static final Pattern PUNCTUATION = Pattern.compile( "\\p{Punct}",
      Pattern.UNICODE_CHARACTER_CLASS );

  /**
   * Sets the class to use when a user calls
   * {@link #getBuilder(java.lang.String)} or
   * {@link #getBuilder(org.eclipse.rdf4j.model.IRI)}
   *
   * @param <T> the type of class (must extend or be UriBuilder)
   * @param klass the class to use
   */
  public static <T extends UriBuilder> void setFactoryClass( Class<T> klass ) {
    bldrclass = klass;
  }

  public static <T extends UriSanitizer> void setDefaultSanitizerClass( Class<T> klass ) {
    saniclass = klass;
  }

  public static <T extends UriBuilder, V extends UriSanitizer> void setFactoryClasses( Class<T> bldr, Class<V> sani ) {
    setFactoryClass( bldr );
    setDefaultSanitizerClass( sani );
  }

  /**
   * Creates an uninitialized instance
   */
  protected UriBuilder() {

  }

  /**
   * Calls {@link #setBase(java.lang.String)}, and throws the same exceptions
   *
   * @param owl
   */
  protected UriBuilder( String owl ) {
    setBase( owl );
  }

  /**
   * Sets the sanitizer to use, assuming it is non-null
   *
   * @param s the sanitizer to use
   */
  public final void setSanitizer( UriSanitizer s ) {
    if ( null != s ) {
      sanitizer = s;
    }
  }

  /**
   * Sets the base IRI from which to build other IRIs
   *
   * @param owl
   *
   * @throws IllegalArgumentException if <code>owl</code> would lead to invalid
   * IRIs
   */
  protected final void setBase( String owl ) {
    if ( null == owl || owl.isEmpty() ) {
      throw new IllegalArgumentException( "argument cannot be null or empty" );
    }

    // do a check to see that we have a valid IRI to start with
    try {
      IRI impl = vf.createIRI( owl );
      content.append( impl.stringValue() );
    }
    catch ( Exception e ) {
      throw new IllegalArgumentException( "invalid IRI component", e );
    }

    lastIsConcatenator = ( owl.endsWith( "/" )
        || owl.endsWith( "#" ) || owl.endsWith( ":" ) );
  }

  /**
   * Converts the "starter" IRI into an actual IRI. If the working IRI ends in a
   * separator ("/", "#", ":"), the separator is removed.
   *
   * @return a IRI
   */
  public IRI toIRI() {
    String IRIstart = content.toString();

    boolean removeLast = ( IRIstart.endsWith( "/" )
        || IRIstart.endsWith( "#" ) || IRIstart.endsWith( ":" ) );
    return vf.createIRI( removeLast
        ? IRIstart.substring( 0, IRIstart.length() - 1 ) : IRIstart );
  }

  public IRI build() {
    return toIRI();
  }

  public IRI build( String extra ) {
    return copy().add( sanitizer.sanitize( extra ) ).build();
  }

  public IRI uniqueIri() {
    StringBuilder IRIstr = new StringBuilder( content );
    IRIstr.append( RandomStringUtils.randomAlphabetic( 1 ) );
    IRIstr.append( UUID.randomUUID().toString() );
    return vf.createIRI( IRIstr.toString() );
  }

  /**
   * Gets a new UriBuilder from this instance's content
   *
   * @return a new builder
   */
  public UriBuilder copy() {
    UriBuilder bldr = new UriBuilder( content.toString() );
    try {
      bldr.setSanitizer( sanitizer.getClass().getConstructor().newInstance() );
    }
    catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException e ) {
      log.error( "BUG: unable to create new instance of " + sanitizer, e );
      bldr.setSanitizer( new DefaultSanitizer() );
    }

    return bldr;
  }

  /**
   * Gets a new UriBuilder like {@link #copy()}, but with an unsanitized segment
   *
   * @param additional the extra stuff to add to the content. MUST BE IRI-VALID
   *
   * @return a new copy of this instance's content, with the extra segment added
   */
  private UriBuilder internalCopy( String additional ) {
    StringBuilder newcontent = new StringBuilder( content );
    if ( !lastIsConcatenator ) {
      newcontent.append( "/" );
    }
    newcontent.append( additional );
    UriBuilder bldr = getBuilder( newcontent.toString() );
    try {
      bldr.setSanitizer( sanitizer.getClass().getConstructor().newInstance() );
    }
    catch ( IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException e ) {
      log.error( "BUG: unable to create new instance of " + sanitizer, e );
      bldr.setSanitizer( new DefaultSanitizer() );
    }
    return bldr;
  }

  public static UriBuilder getBuilder( String ns ) {
    UriBuilder ldr;
    UriSanitizer sani;
    try {
      ldr = bldrclass.getConstructor().newInstance();
    }
    catch ( IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException e ) {
      log.error( "BUG: cannot create UriBuilder instance; using fallback", e );
      ldr = new UriBuilder();
    }

    try {
      sani = saniclass.getConstructor().newInstance();
    }
    catch ( IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException e ) {
      log.error( "BUG: cannot create UriSanitizer instance; using fallback", e );
      sani = new DefaultSanitizer();
    }

    ldr.setBase( ns );
    ldr.setSanitizer( sani );
    return ldr;
  }

  public static UriBuilder getBuilder( IRI ns ) {
    return getBuilder( ns.stringValue() );
  }

  /**
   * Adds a new part to the IRI, after sanitizing it. If <code>localname</code>
   * ends in a "concatenator" character, the character is preserved, but
   * everything else is sent to the sanitizer
   *
   * @param localname the part to add to the IRI
   *
   * @return this UriBuilder
   */
  public UriBuilder add( String localname ) {
    localname = ( null == localname ? "" : localname.trim() );

    if ( localname.isEmpty() ) {
      return this;
    }

    if ( !lastIsConcatenator ) {
      content.append( "/" );
    }

    String newlocal;
    String lastchar;
    Matcher m = CONCAT_PAT.matcher( localname );
    if ( m.matches() ) {
      newlocal = m.group( 1 );
      lastchar = m.group( 2 );
      lastIsConcatenator = true;
    }
    else {
      newlocal = localname;
      lastchar = "";
      lastIsConcatenator = false;
    }

    content.append( sanitizer.sanitize( newlocal ) ).append( lastchar );

    return this;
  }

  /**
   * Does the given string start with our current content?
   *
   * @param IRI the IRI to check
   * @return true, if <code>IRI.startsWith( content.toString() )</code>
   */
  public boolean contains( String IRI ) {
    return IRI.startsWith( content.toString() );
  }

  public boolean contains( Resource IRI ) {
    return contains( IRI.stringValue() );
  }

  /**
   * Checks if the given IRI's namespace is the same as our current content.
   * This is a stricter check than {@link #contains(org.eclipse.rdf4j.model.Resource)}
   * because the namespace must match the this builder's content exactly, and
   * not just start the same way
   *
   * @param IRI the IRI to check
   * @return true, if
   * <code>IRI.getNamespace().equals( content.toString() )</code>
   */
  public boolean namespaceOf( IRI IRI ) {
    return IRI.getNamespace().equals( content.toString() );
  }

  /**
   * Gets a copy of this instance, as if in the core namespace
   *
   * @return a copy of this instance
   */
  public UriBuilder getCoreIRI() {
    return internalCopy( "core#" );
  }

  /**
   * A convenience {@link #getCoreIRI()}.{@link #add(java.lang.String) }.
   *
   * @param localname the core name to add
   *
   * @return a copy of this instance
   */
  public IRI getCoreIRI( String localname ) {
    return getCoreIRI().add( localname ).build();
  }

  /**
   * Gets a copy of this instance, as if in the relation namespace
   *
   * @return a copy of this instance
   */
  public UriBuilder getRelationIri() {
    return internalCopy( "Relation/" );
  }

  /**
   * A convenience {@link #getRelationIRI() }.{@link #add(java.lang.String) }.
   *
   * @param localname the core name to add
   *
   * @return a copy of this instance
   */
  public IRI getRelationIri( String localname ) {
    return UriBuilder.this.getRelationIri().add( localname ).build();
  }

  public IRI getContainsIri() {
    return getRelationIri( Constants.CONTAINS );
  }

  /**
   * Gets a copy of this instance, as if in the concept instance (does not have
   * a trailing "/")
   *
   * @return a copy of this instance
   */
  public UriBuilder getConceptIri() {
    return internalCopy( Constants.DEFAULT_NODE_CLASS );
  }

  /**
   * Gets a copy of this instance, as if in the concept namespace (has a
   * trailing "/")
   *
   * @return a copy of this instance
   */
  public UriBuilder getConceptNamespace() {
    return internalCopy( Constants.DEFAULT_NODE_CLASS + "/" );
  }

  @Override
  public String toString() {
    return content.toString();
  }

  /**
   * A convenience to {@link #getConceptIRI()}.{@link  #add(java.lang.String)
   * }.
   *
   * @param localname the core name to add
   *
   * @return a copy of this instance
   */
  public IRI getConceptIRI( String localname ) {
    return getConceptIri().add( localname ).build();
  }

  public UriSanitizer getSanitizer() {
    return sanitizer;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 29 * hash + Objects.hashCode( this.content );
    return hash;
  }

  @Override
  public boolean equals( Object obj ) {
    if ( obj == null ) {
      return false;
    }
    if ( getClass() != obj.getClass() ) {
      return false;
    }
    final UriBuilder other = (UriBuilder) obj;
    return content.toString().equals( other.content.toString() );
  }

  public static class DefaultSanitizer implements UriSanitizer {

    private final int localPartMaxLength = 54; // TBD: qualify this limit, consider making a semoss property

    public String getUUIDLocalName() {
      return RandomStringUtils.randomAlphabetic( 1 ) + UUID.randomUUID().toString();
    }

    @Override
    public String sanitize( String raw ) {
      // Check if the string is already valid:
      String sanitized = raw;
      if ( !RDFDatatypeTools.isValidIriChars( raw ) ) {
        // Attempt a simple sanitizing:

        String rawWithUnderscores
            = raw.trim().replaceAll( "(\\p{Punct}|\\s)", "_" );

        if ( RDFDatatypeTools.isValidIriChars( rawWithUnderscores ) ) {
          sanitized = rawWithUnderscores;
        }
        else {
          // Still not clean enough, just use a random IRI (below)
          sanitized = "";
        }
      }

      // At issue here was a that truncating the local part at the length limit (as done previously) did not
      // guarantee uniqueness.  This lead to occasional IRI collisions and the overlap of data.  So now when
      // we hit the max length limit, we graph a UUID since we are otherwise unable to check for uniqueness
      // at this stage.
      //
      return ( sanitized.length() > localPartMaxLength || sanitized.isEmpty() )
          ? getUUIDLocalName() : sanitized;
    }
  }
}
