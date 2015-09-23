/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.util;

import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * A class that helps to build URIs.
 *
 * @author ryan
 */
public class UriBuilder {

	private static final Logger log = Logger.getLogger( UriBuilder.class );
	private static Class<? extends UriBuilder> bldrclass = UriBuilder.class;
	private static Class<? extends UriSanitizer> saniclass = DefaultSanitizer.class;
	private final ValueFactory vf = new ValueFactoryImpl();
	private final StringBuilder content = new StringBuilder();
	private UriSanitizer sanitizer;
	private boolean lastIsConcatenator;
	private static final Pattern CONCAT_PAT = Pattern.compile( "(.*)([/#:])$" );
	private static final Pattern PUNCTUATION = Pattern.compile( "\\p{Punct}",
			Pattern.UNICODE_CHARACTER_CLASS );

	/**
	 * Sets the class to use when a user calls
	 * {@link #getBuilder(java.lang.String)} or
	 * {@link #getBuilder(org.openrdf.model.URI)}
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
	 * Sets the base URI from which to build other URIs
	 *
	 * @param owl
	 *
	 * @throws IllegalArgumentException if <code>owl</code> would lead to invalid
	 * URIs
	 */
	protected final void setBase( String owl ) {
		if ( null == owl || owl.isEmpty() ) {
			throw new IllegalArgumentException( "argument cannot be null or empty" );
		}

		// do a check to see that we have a valid URI to start with
		try {
			URI impl = vf.createURI( owl );
			content.append( impl.stringValue() );
		}
		catch ( Exception e ) {
			throw new IllegalArgumentException( "invalid URI component", e );
		}

		lastIsConcatenator = ( owl.endsWith( "/" )
				|| owl.endsWith( "#" ) || owl.endsWith( ":" ) );
	}

	/**
	 * Converts the "starter" URI into an actual URI. If the working URI ends in a
	 * separator ("/", "#", ":"), the separator is removed.
	 *
	 * @return a URI
	 */
	public URI toUri() {
		String uristart = content.toString();

		boolean removeLast = ( uristart.endsWith( "/" )
				|| uristart.endsWith( "#" ) || uristart.endsWith( ":" ) );
		return vf.createURI( removeLast
				? uristart.substring( 0, uristart.length() - 1 ) : uristart );
	}

	public URI build() {
		return toUri();
	}

	public URI build( String extra ) {
		return copy().add( extra ).build();
	}

	public URI uniqueUri() {
		StringBuilder uristr = new StringBuilder( content );
		uristr.append( RandomStringUtils.randomAlphabetic( 1 ) );
		uristr.append( UUID.randomUUID().toString() );
		return vf.createURI( uristr.toString() );
	}

	/**
	 * Gets a new UriBuilder from this instance's content
	 *
	 * @return a new builder
	 */
	public UriBuilder copy() {
		UriBuilder bldr = new UriBuilder( content.toString() );
		try {
			bldr.setSanitizer( sanitizer.getClass().newInstance() );
		}
		catch ( InstantiationException | IllegalAccessException e ) {
			log.error( "BUG: unable to create new instance of " + sanitizer, e );
			bldr.setSanitizer( new DefaultSanitizer() );
		}

		return bldr;
	}

	/**
	 * Gets a new UriBuilder like {@link #copy()}, but with an unsanitized segment
	 *
	 * @param additional the extra stuff to add to the content. MUST BE URI-VALID
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
			bldr.setSanitizer( sanitizer.getClass().newInstance() );
		}
		catch ( InstantiationException | IllegalAccessException e ) {
			log.error( "BUG: unable to create new instance of " + sanitizer, e );
			bldr.setSanitizer( new DefaultSanitizer() );
		}
		return bldr;
	}

	public static UriBuilder getBuilder( String ns ) {
		UriBuilder ldr;
		UriSanitizer sani;
		try {
			ldr = bldrclass.newInstance();
		}
		catch ( InstantiationException | IllegalAccessException e ) {
			log.error( "BUG: cannot create UriBuilder instance; using fallback", e );
			ldr = new UriBuilder();
		}

		try {
			sani = saniclass.newInstance();
		}
		catch ( InstantiationException | IllegalAccessException e ) {
			log.error( "BUG: cannot create UriSanitizer instance; using fallback", e );
			sani = new DefaultSanitizer();
		}

		ldr.setBase( ns );
		ldr.setSanitizer( sani );
		return ldr;
	}

	public static UriBuilder getBuilder( URI ns ) {
		return getBuilder( ns.stringValue() );
	}

	/**
	 * Adds a new part to the URI, after sanitizing it. If <code>localname</code>
	 * ends in a "concatenator" character, the character is preserved, but
	 * everything else is sent to the sanitizer
	 *
	 * @param localname the part to add to the URI
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
	 * @param uri the uri to check
	 * @return true, if <code>uri.startsWith( content.toString() )</code>
	 */
	public boolean contains( String uri ) {
		return uri.startsWith( content.toString() );
	}

	public boolean contains( Resource uri ) {
		return contains( uri.stringValue() );
	}

	/**
	 * Checks if the given URI's namespace is the same as our current content.
	 * This is a stricter check than {@link #contains(org.openrdf.model.Resource)}
	 * because the namespace must match the this builder's content exactly, and
	 * not just start the same way
	 *
	 * @param uri the uri to check
	 * @return true, if
	 * <code>uri.getNamespace().equals( content.toString() )</code>
	 */
	public boolean namespaceOf( URI uri ) {
		return uri.getNamespace().equals( content.toString() );
	}

	/**
	 * Gets a copy of this instance, as if in the core namespace
	 *
	 * @return a copy of this instance
	 */
	public UriBuilder getCoreUri() {
		return internalCopy( "core#" );
	}

	/**
	 * A convenience {@link #getCoreUri()}.{@link #add(java.lang.String) }.
	 *
	 * @param localname the core name to add
	 *
	 * @return a copy of this instance
	 */
	public URI getCoreUri( String localname ) {
		return getCoreUri().add( localname ).build();
	}

	/**
	 * Gets a copy of this instance, as if in the relation namespace
	 *
	 * @return a copy of this instance
	 */
	public UriBuilder getRelationUri() {
		return internalCopy( "Relation/" );
	}

	/**
	 * A convenience {@link #getRelationUri() }.{@link #add(java.lang.String) }.
	 *
	 * @param localname the core name to add
	 *
	 * @return a copy of this instance
	 */
	public URI getRelationUri( String localname ) {
		return getRelationUri().add( localname ).build();
	}

	public URI getContainsUri() {
		return getRelationUri( Constants.CONTAINS );
	}

	/**
	 * Gets a copy of this instance, as if in the concept instance (does not have
	 * a trailing "/")
	 *
	 * @return a copy of this instance
	 */
	public UriBuilder getConceptUri() {
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
	 * A convenience to {@link #getConceptUri()}.{@link  #add(java.lang.String)
	 * }.
	 *
	 * @param localname the core name to add
	 *
	 * @return a copy of this instance
	 */
	public URI getConceptUri( String localname ) {
		return getConceptUri().add( localname ).build();
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
			if ( !RDFDatatypeTools.isValidUriChars( raw ) ) {
				// Attempt a simple sanitizing:

				String rawWithUnderscores
						= raw.trim().replaceAll( "(\\p{Punct}|\\s)", "_" );

				if ( RDFDatatypeTools.isValidUriChars( rawWithUnderscores ) ) {
					sanitized = rawWithUnderscores;
				}
				else {
					// Still not clean enough, just use a random URI (below)
					sanitized = "";
				}
			}

			// At issue here was a that truncating the local part at the length limit (as done previously) did not
			// guarantee uniqueness.  This lead to occasional URI collisions and the overlap of data.  So now when
			// we hit the max length limit, we graph a UUID since we are otherwise unable to check for uniqueness
			// at this stage.
			//
			return ( sanitized.length() > localPartMaxLength || sanitized.isEmpty() )
					? getUUIDLocalName() : sanitized;
		}
	}
}
