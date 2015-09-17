/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.poi.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * A class to encapsulate relationship loading sheet information.
 *
 * @author ryan
 */
public class LoadingSheetData {

	private static final Logger log = Logger.getLogger( LoadingSheetData.class );
	private String subjectType;
	private String objectType;
	private String relname;

	private boolean sbjErr = false;
	private boolean objErr = false;
	private boolean relErr = false;
	private final Set<String> propErrs = new HashSet<>();

	// property name => datatype lookup
	private final Map<String, URI> propcache = new LinkedHashMap<>();
	private final Set<String> proplink = new HashSet<>();
	private final Set<String> napcache = new HashSet<>();
	private Map<Long, LoadingNodeAndPropertyValues> data;
	private final String tabname;

	protected LoadingSheetData( String tabtitle, String type ) {
		this( tabtitle, type, new HashMap<>() );
	}

	protected LoadingSheetData( String tabtitle, String type, Collection<String> props ) {
		this( tabtitle, type, null, null, props );
	}

	protected LoadingSheetData( String tabtitle, String type, Map<String, URI> props ) {
		this( tabtitle, type, null, null, props );
	}

	protected LoadingSheetData( String tabtitle, String sType, String oType,
			String relname ) {
		this( tabtitle, sType, oType, relname, new HashMap<>() );
	}

	protected LoadingSheetData( String tabtitle, String sType, String oType,
			String relname, Collection<String> props ) {
		this( tabtitle, sType, oType, relname );
		for ( String p : props ) {
			propcache.put( p, null );
		}
	}

	protected LoadingSheetData( String tabtitle, String sType, String oType,
			String relname, Map<String, URI> props ) {
		subjectType = sType;
		tabname = tabtitle;
		this.objectType = oType;
		this.relname = relname;
		propcache.putAll( props );
		data = new HashMap<>();
	}

	public int rows() {
		return data.size();
	}

	public boolean hasErrors() {
		for ( LoadingNodeAndPropertyValues nap : getData() ) {
			if ( nap.hasError() ) {
				return true;
			}
		}

		return false;
	}

	public boolean hasSubjectTypeError() {
		return sbjErr;
	}

	public void setSubjectTypeIsError( boolean sbjErr ) {
		this.sbjErr = sbjErr;
	}

	public boolean hasObjectTypeError() {
		return objErr;
	}

	public void setObjectTypeIsError( boolean objErr ) {
		this.objErr = objErr;
	}

	public boolean hasRelationError() {
		return relErr;
	}

	public void setRelationIsError( boolean relErr ) {
		this.relErr = relErr;
	}

	public void setPropertyIsError( String errprop, boolean iserr ) {
		if ( propcache.containsKey( errprop ) ) {
			if ( iserr ) {
				propErrs.add( errprop );
			}
			else {
				propErrs.remove( errprop );
			}
		}
	}

	public void setSubjectType( String subjectType ) {
		this.subjectType = subjectType;
	}

	public void setObjectType( String objectType ) {
		this.objectType = objectType;
	}

	public void setRelname( String relname ) {
		this.relname = relname;
	}

	public boolean propertyIsError( String prop ) {
		return propErrs.contains( prop );
	}

	public boolean hasModelErrors() {
		return ( relErr || sbjErr || objErr || !propErrs.isEmpty() );
	}

	/**
	 * Sets the URI for a given property
	 *
	 * @param prop the property name
	 * @param type the datatype it should be
	 */
	public void setPropertyDataType( String prop, URI type ) {
		propcache.put( prop, type );
	}

	public boolean hasPropertyDataType( String prop ) {
		return ( propcache.containsKey( prop )
				? null != propcache.get( prop ) : false );
	}

	public URI getPropertyDataType( String prop ) {
		return propcache.get( prop );
	}

	public String getObjectType() {
		return objectType;
	}

	public String getRelname() {
		return relname;
	}

	public String getName() {
		return tabname;
	}

	public void addProperty( String prop ) {
		addProperty( prop, null );
	}

	public void addProperty( String prop, URI type ) {
		propcache.put( prop, type );
	}

	public Collection<String> getProperties() {
		return propcache.keySet();
	}

	public boolean hasProperties() {
		return !propcache.isEmpty();
	}

	public final void addProperties( Collection<String> props ) {
		for ( String s : props ) {
			addProperty( s );
		}
	}

	/**
	 * Releases any resources used by this class. Technically, this function must
	 * only be called when {@link #isMemOnly() } returns false, but it's good
	 * practice to always call it
	 */
	public void release() {
	}

	public void finishLoading(){
	}
	
	/**
	 * Clears any stored loading data
	 */
	public void clear() {
		data.clear();
		propcache.clear();
	}

	public void setProperties( Map<String, URI> props ) {
		propcache.clear();
		propcache.putAll( props );
	}

	public Map<String, URI> getPropertiesAndDataTypes() {
		return new HashMap<>( propcache );
	}

	public String getSubjectType() {
		return subjectType;
	}

	protected void cacheNapLabel( String label ) {
		napcache.add( label );
	}

	protected boolean isNapLabelCached( String s ) {
		return napcache.contains( s );
	}

	protected boolean isPropLabelCached( String s ) {
		return propcache.containsKey( s );
	}

	/**
	 * Gets this instance's node and property data. Changes made to the returned
	 * collection are NOT propagated to the internal copy
	 *
	 * @return the internal data
	 */
	public List<LoadingNodeAndPropertyValues> getData() {
		return new ArrayList<>( data.values() );
	}

	/**
	 * Sets the internal data to a copy of the given data.
	 *
	 * @param newdata the new data for this instance
	 */
	public void setData( Collection<LoadingNodeAndPropertyValues> newdata ) {
		data.clear();
		for ( LoadingNodeAndPropertyValues nap : newdata ) {
			add( nap );
		}
		commit();
	}

	protected void commit() {
	}

	protected void iadd( LoadingNodeAndPropertyValues nap ) {
		long lng = data.size();
		data.put( lng++, nap );
	}

	public void add( LoadingNodeAndPropertyValues nap ) {
		iadd( nap );

		// add this NAP's label to our cache, if we have it (we should)
		cacheNapLabel( nap.getSubject() );
	}

	public LoadingNodeAndPropertyValues add( String slabel ) {
		LoadingNodeAndPropertyValues nap = new LoadingNodeAndPropertyValues( slabel );
		add( nap );
		return nap;
	}

	public LoadingNodeAndPropertyValues add( String slabel, Map<String, Value> props ) {
		LoadingNodeAndPropertyValues nap = add( slabel );
		nap.putAll( props );
		return nap;
	}

	public LoadingNodeAndPropertyValues add( String slabel, String olabel ) {

		LoadingNodeAndPropertyValues nap
				= new LoadingNodeAndPropertyValues( slabel, olabel );
		cacheNapLabel( olabel );
		add( nap );
		return nap;
	}

	/**
	 * Adds a new nap to the list. This is the preferred way to construct the nap
	 * @param slabel
	 * @param olabel
	 * @param props
	 * @return 
	 */
	public LoadingNodeAndPropertyValues add( String slabel, String olabel,
			Map<String, Value> props ) {
		LoadingNodeAndPropertyValues nap
				= new LoadingNodeAndPropertyValues( slabel, olabel, props );
		add( nap );
		return nap;
	}

	public List<String> getHeaders() {
		List<String> heads = new ArrayList<>();
		heads.add( getSubjectType() );
		if ( isRel() ) {
			heads.add( getObjectType() );
		}

		heads.addAll( propcache.keySet() );
		return heads;
	}

	public void setHeaders( List<String> newheads ) {
		if ( newheads.size() != getHeaders().size() ) {
			log.error( "cannot change header size" );
			return;
		}

		String st = newheads.get( 0 );
		setSubjectType( st );

		int firstPropCol = 1;
		if ( isRel() ) {
			String ot = newheads.get( 1 );
			setObjectType( ot );
			firstPropCol = 2;
		}

		// properties are a little tough because we cannot change the key of the map,
		// and if we add a new key, it'll mess up the iteration order
		// so we'll create a new map, and add the columns in the right order
		// also, worry about propagating errors 
		String[] oldkeys = propcache.keySet().toArray( new String[0] );
		int col = 0;
		Map<String, URI> newtypes = new LinkedHashMap<>();
		Set<String> newerrors = new HashSet<>();
		ListIterator<String> propit = newheads.listIterator( firstPropCol );
		while ( propit.hasNext() ) {
			String newkey = propit.next();
			String oldkey = oldkeys[col++];
			URI proptype = propcache.get( oldkey );
			newtypes.put( newkey, proptype );
			if ( this.propertyIsError( oldkey ) ) {
				newerrors.add( newkey );
			}
		}

		setProperties( newtypes );
		propErrs.clear();
		propErrs.addAll( newerrors );
	}

	public boolean isRel() {
		return ( null != objectType );
	}

	public boolean isEmpty() {
		return data.isEmpty();
	}

	@Override
	public String toString() {
		return getName() + ( isRel() ? "(rel)" : "(node)" ) + " with " + getData().size() + " naps";
	}

	/**
	 * Looks through all properties of this sheet to see if any are actually a
	 * link to the {@link #subjectType} of one of the given sheets
	 *
	 * @param others
	 */
	public void findPropertyLinks( Collection<LoadingSheetData> others ) {
		proplink.clear();
		for ( String propname : propcache.keySet() ) {
			for ( LoadingSheetData lsd : others ) {
				if ( lsd.getSubjectType().equals( propname ) ) {
					proplink.add( propname );
				}
			}
		}
	}

	public boolean isLink( String propname ) {
		return proplink.contains( propname );
	}

	public boolean isMemOnly() {
		return true;
	}

	public static LoadingSheetData copyHeadersOf( LoadingSheetData model ) {
		LoadingSheetData lsd;
		if ( model.isRel() ) {
			lsd = new LoadingSheetData( model.getName(), model.getSubjectType(),
					model.getObjectType(), model.getRelname(), model.getPropertiesAndDataTypes() );
		}
		else {
			lsd = new LoadingSheetData( model.getName(), model.getSubjectType(),
					model.getPropertiesAndDataTypes() );
		}

		if ( model.hasModelErrors() ) {
			lsd.setSubjectTypeIsError( model.hasSubjectTypeError() );
			lsd.setObjectTypeIsError( model.hasObjectTypeError() );
			lsd.setRelationIsError( model.hasRelationError() );
			for ( String p : model.getProperties() ) {
				lsd.setPropertyIsError( p, model.propertyIsError( p ) );
			}
		}

		return lsd;
	}

	public static LoadingSheetData nodesheet( String subject ) {
		return nodesheet( subject, subject );
	}

	public static LoadingSheetData nodesheet( String tabname, String subject ) {
		return nodesheet( tabname, subject, true );
	}

	public static LoadingSheetData nodesheet( String tabname, String subject,
			boolean inMemOnly ) {
		if ( !inMemOnly ) {
			try {
				return new DiskBackedLoadingSheetData( tabname, subject );
			}
			catch ( IOException ioe ) {
				log.warn( "could not create file-based loading sheet", ioe );
				// just fall through
			}
		}

		return new LoadingSheetData( tabname, subject );
	}

	public static LoadingSheetData relsheet( String subject, String object,
			String relname ) {
		return relsheet( subject, object, relname, true );
	}

	public static LoadingSheetData relsheet( String subject, String object,
			String relname, boolean inMemOnly ) {
		StringBuilder sb = new StringBuilder( subject ).append( "-" );
		sb.append( relname ).append( "-" ).append( object );
		return relsheet( sb.toString(), subject, object, relname, inMemOnly );
	}

	public static LoadingSheetData relsheet( String tabname, String subject,
			String object, String relname ) {
		return relsheet( tabname, subject, object, relname, true );
	}

	public static LoadingSheetData relsheet( String tabname, String subject,
			String object, String relname, boolean inMemOnly ) {

		if ( !inMemOnly ) {
			try {
				return new DiskBackedLoadingSheetData( tabname, subject, object, relname );
			}

			catch ( IOException ioe ) {
				log.warn( "could not create file-based loading sheet", ioe );
				// just fall through
			}
		}

		return new LoadingSheetData( tabname, subject, object, relname );
	}

	public class LoadingNodeAndPropertyValues extends HashMap<String, Value> {

		private String subject;
		private String object;
		private boolean subjectIsError = false;
		private boolean objectIsError = false;

		public LoadingNodeAndPropertyValues( String subj ) {
			this( subj, new HashMap<>() );
		}

		public LoadingNodeAndPropertyValues( String subject, String object ) {
			this.subject = subject;
			this.object = object;
		}

		/**
		 * If at all possible, this is the ctor to use
		 *
		 * @param subject
		 * @param object
		 * @param props
		 */
		public LoadingNodeAndPropertyValues( String subject, String object,
				Map<String, Value> props ) {
			super( props );
			this.subject = subject;
			this.object = object;
		}

		public LoadingNodeAndPropertyValues( String subject, Map<String, Value> props ) {
			this( subject, null, props );
		}

		public String getSubject() {
			return subject;
		}

		public String getObject() {
			return object;
		}

		public String getSheetName() {
			return LoadingSheetData.this.getName();
		}

		public String getRelname() {
			return LoadingSheetData.this.getRelname();
		}

		public void setSubject( String s ) {
			subject = s;
		}

		public void setObject( String s ) {
			object = s;
		}

		public boolean hasError() {
			return ( subjectIsError || objectIsError );
		}

		public void setSubjectIsError( boolean iserr ) {
			subjectIsError = iserr;
		}

		public void setObjectIsError( boolean iserr ) {
			objectIsError = iserr;
		}

		public boolean isSubjectError() {
			return subjectIsError;
		}

		public boolean isObjectError() {
			return objectIsError;
		}

		public String getSubjectType() {
			return LoadingSheetData.this.subjectType;
		}

		public String getObjectType() {
			return LoadingSheetData.this.objectType;
		}

		public Value[] convertToValueArray( ValueFactory vf ) {
			int arrsize = propcache.size() + ( isRel() ? 2 : 1 );

			Value vals[] = new Value[arrsize];

			vals[0] = vf.createLiteral( getSubject() );
			int i = 0;
			if ( isRel() ) {
				vals[1] = vf.createLiteral( getObject() );
				i = 1;
			}

			for ( String prop : propcache.keySet() ) {
				vals[++i] = ( containsKey( prop ) ? get( prop ) : null );
			}

			return vals;
		}

		public boolean hasProperty( URI needle, Map<String, String> namespaces ) {
			ValueFactory vf = new ValueFactoryImpl();
			for ( String head : keySet() ) {
				if ( head.contains( ":" ) ) {
					int idx = head.indexOf( ":" );
					String headns = head.substring( 0, idx );
					String localname = head.substring( idx + 1 );

					if ( namespaces.containsKey( headns ) ) {
						URI uri = vf.createURI( namespaces.get( headns ), localname );
						if ( uri.equals( needle ) ) {
							return true;
						}
					}
				}
			}

			return false;
		}

		@Override
		public int hashCode() {
			int hash = super.hashCode();
			hash = 19 * hash + Objects.hashCode( this.subject );
			hash = 19 * hash + Objects.hashCode( this.object );
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
			final LoadingNodeAndPropertyValues other = (LoadingNodeAndPropertyValues) obj;
			if ( !Objects.equals( this.subject, other.subject ) ) {
				return false;
			}

			return Objects.equals( this.object, other.object );
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder( subject );
			if ( this.subjectIsError ) {
				sb.append( "<e>" );
			}
			if ( isRel() ) {
				sb.append( ";" );
				sb.append( object );
				if ( this.objectIsError ) {
					sb.append( "<e>" );
				}

				sb.append( "; " ).append( relname );
			}

			return sb.toString();
		}
	}
}
