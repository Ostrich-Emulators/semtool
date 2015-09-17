/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.poi.main;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import gov.va.semoss.util.RDFDatatypeTools;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * A class to encapsulate relationship loading sheet information.
 *
 * @author ryan
 */
public class DiskBackedLoadingSheetData extends LoadingSheetData {

	private static final Logger log = Logger.getLogger( DiskBackedLoadingSheetData.class );
	private static final long COMMITLIMIT = 100000;
	private static final long READCACHELIMIT = 10000;

	private File backingfile;
	private long opsSinceLastCommit = 0;
	private int datacount = 0;
	private final Set<LoadingNodeAndPropertyValues> removedNodes = new HashSet<>();
	private final ObjectMapper oxm = new ObjectMapper();

	protected DiskBackedLoadingSheetData( String tabtitle, String type ) throws IOException {
		this( tabtitle, type, new HashMap<>() );
	}

	protected DiskBackedLoadingSheetData( String tabtitle, String type,
			Collection<String> props ) throws IOException {
		this( tabtitle, type, null, null, props );
	}

	protected DiskBackedLoadingSheetData( String tabtitle, String type,
			Map<String, URI> props ) throws IOException {
		this( tabtitle, type, null, null, props );
	}

	protected DiskBackedLoadingSheetData( String tabtitle, String sType, String oType,
			String relname ) throws IOException {
		this( tabtitle, sType, oType, relname, new HashMap<>() );
	}

	protected DiskBackedLoadingSheetData( String tabtitle, String sType, String oType,
			String relname, Collection<String> props ) throws IOException {
		this( tabtitle, sType, oType, relname );
	}

	public DiskBackedLoadingSheetData( LoadingSheetData model ) throws IOException {
		this( model.getName(), model.getSubjectType(), model.getObjectType(),
				model.getRelname(), model.getPropertiesAndDataTypes() );

		Iterator<LoadingNodeAndPropertyValues> it = model.iterator();
		while ( it.hasNext() ) {
			add( it.next() );
		}
	}

	protected DiskBackedLoadingSheetData( String tabtitle, String sType, String oType,
			String relname, Map<String, URI> props ) throws IOException {
		super( tabtitle, sType, oType, relname, props );

		backingfile = File.createTempFile( tabtitle + "-", ".lsdata" );
		log.debug( "backing file is: " + backingfile );
		SimpleModule sm = new SimpleModule();
		sm.addSerializer( LoadingNodeAndPropertyValues.class, new NapSerializer() );
		sm.addDeserializer( LoadingNodeAndPropertyValues.class, new NapDeserializer() );
		oxm.registerModule( sm );
	}

	@Override
	public boolean isMemOnly() {
		return false;
	}

	/**
	 * Releases any resources used by this class.
	 */
	@Override
	public void release() {
		super.clear();
		removedNodes.clear();
		FileUtils.deleteQuietly( backingfile );
	}

	@Override
	public void finishLoading() {
		commit();
	}

	/**
	 * Clears any stored loading data
	 */
	@Override
	public void clear() {
		super.clear();
		datacount = 0;
		commit();
	}

	@Override
	protected void commit() {
		// flush everything to our backing file
		try ( BufferedWriter writer
				= new BufferedWriter( new FileWriter( backingfile, true ) ) ) {
			Iterator<LoadingNodeAndPropertyValues> it = super.iterator();
			while ( it.hasNext() ) {
				writer.write( oxm.writeValueAsString( it.next() ) );
				writer.newLine();
				it.remove();
			}

			opsSinceLastCommit = 0;
		}
		catch ( IOException ioe ) {
			log.warn( "loading sheet internal commit failed", ioe );
		}
	}

	@Override
	public DataIterator iterator() {
		try {
			return new CacheIterator();
		}
		catch ( IOException ioe ) {
			log.warn( "cannot access backing file in iterator", ioe );
		}
		return super.iterator();
	}

	@Override
	public List<LoadingNodeAndPropertyValues> getData() {
		List<LoadingNodeAndPropertyValues> list = new ArrayList<>();
		Iterator<LoadingNodeAndPropertyValues> it = iterator();
		while ( it.hasNext() ) {
			list.add( it.next() );
		}
		return list;
	}

	@Override
	public int rows() {
		return datacount;
	}

	@Override
	public boolean isEmpty() {
		return ( 0 == datacount );
	}

	@Override
	protected void added( LoadingNodeAndPropertyValues nap ) {
		datacount++;
		tryCommit();
	}

	private void tryCommit() {
		opsSinceLastCommit++;
		if ( opsSinceLastCommit >= COMMITLIMIT ) {
			commit();
		}
	}

	@Override
	public void removeAll( Collection<LoadingNodeAndPropertyValues> naps ) {
		super.removeAll( naps );
		removedNodes.addAll( naps );
	}

	@Override
	public void remove( LoadingNodeAndPropertyValues nap ) {
		super.remove( nap );
		removedNodes.add( nap );
	}

	private static class NapSerializer extends JsonSerializer<LoadingNodeAndPropertyValues> {

		@Override
		public void serialize( LoadingNodeAndPropertyValues value, JsonGenerator jgen,
				SerializerProvider provider ) throws IOException, JsonProcessingException {
			jgen.writeStartObject();
			jgen.writeStringField( "subject", value.getSubject() );
			jgen.writeStringField( "object", value.getObject() );

			jgen.writeBooleanField( "subjectIsError", value.isSubjectError() );
			jgen.writeBooleanField( "objectIsError", value.isObjectError() );
			jgen.writeArrayFieldStart( "properties" );
			for ( Map.Entry<String, Value> en : value.entrySet() ) {
				Value val = en.getValue();
				jgen.writeStartObject();
				jgen.writeStringField( "prop", en.getKey() );
				jgen.writeStringField( "value", val.stringValue() );
				if ( val instanceof Literal ) {
					Literal l = Literal.class.cast( val );
					if ( null == l.getLanguage() ) {
						URI dt = l.getDatatype();
						if ( null != dt ) {
							jgen.writeStringField( "dt", dt.stringValue() );
						}
					}
					else {
						jgen.writeStringField( "lang", l.getLanguage() );
					}
				}
				jgen.writeEndObject();
			}
			jgen.writeEndArray();
			jgen.writeEndObject();
		}
	}

	private class NapDeserializer extends JsonDeserializer<LoadingNodeAndPropertyValues> {

		@Override
		public LoadingNodeAndPropertyValues deserialize( JsonParser jp, DeserializationContext ctxt ) throws IOException, JsonProcessingException {
			ValueFactory vf = new ValueFactoryImpl();

			JsonNode node = jp.getCodec().readTree( jp );
			LoadingNodeAndPropertyValues nap
					= new LoadingNodeAndPropertyValues( node.get( "subject" ).asText() );
			JsonNode oj = node.get( "object" );
			if ( !oj.isNull() ) {
				nap.setObject( oj.asText() );
			}

			nap.setSubjectIsError( node.get( "subjectIsError" ).asBoolean( false ) );
			nap.setObjectIsError( node.get( "objectIsError" ).asBoolean( false ) );

			Iterator<JsonNode> propit = node.get( "properties" ).elements();
			while ( propit.hasNext() ) {
				JsonNode propval = propit.next();
				String prop = propval.get( "prop" ).asText();
				String valstr = propval.get( "value" ).asText();
				JsonNode lang = propval.get( "lang" );
				if ( null == lang || lang.isNull() ) {
					// no language, so check for datatype
					JsonNode dt = propval.get( "dt" );
					if ( null == dt || dt.isNull() ) {
						// just do the best we can
						nap.put( prop, RDFDatatypeTools.getRDFStringValue( valstr, null, vf ) );
					}
					else {
						nap.put( prop, vf.createLiteral( valstr, vf.createURI( dt.asText() ) ) );
					}
				}
				else {
					nap.put( prop, vf.createLiteral( valstr, lang.asText() ) );
				}
			}

			return nap;
		}
	}

	/**
	 * An iterator that iterates over the backing store first, then over the
	 * in-memory naps
	 */
	private class CacheIterator extends DataIteratorImpl {

		private final BufferedReader reader;
		private final Deque<LoadingNodeAndPropertyValues> readcache = new ArrayDeque<>();
		private Iterator<LoadingNodeAndPropertyValues> memoryiter = null;
		private LoadingNodeAndPropertyValues current;

		public CacheIterator() throws IOException {
			if ( backingfile.exists() ) {
				reader = new BufferedReader( new FileReader( backingfile ) );
			}
			else {
				reader = null;
			}
		}

		@Override
		public boolean hasNext() {
			// we need to iterate over data from three places (in this order)
			// 1) our readcache, until it is empty
			// 2) our backing file, until it is empty
			// 3) our in-memory data
			// we'll populate our readcache by periodically filling it with data from 
			// our backing file

			boolean hasnext = !readcache.isEmpty();

			// our read cache is empty
			if ( !hasnext ) {
				// we haven't exhausted our backing file yet, so check there
				if ( null == memoryiter ) {
					hasnext = cacheMoreFromStore();

					if ( !hasnext ) {
						// close our file reader, since the file is empty
						try {
							reader.close();
						}
						catch ( IOException ioe ) {
							log.warn( "problem closing file cache reader", ioe );
						}

						// our backing file is exhausted, so switch to our in-memory data
						memoryiter = DiskBackedLoadingSheetData.super.iterator();
						hasnext = memoryiter.hasNext();
					}
				}
				else {
					// we've already exhausted our backing file, so we're just reading
					// from memory until we're out of data
					hasnext = memoryiter.hasNext();
				}
			}

			// if we're totally empty, release anything we're still holding onto
			if ( !hasnext ) {
				release();
			}

			return hasnext;
		}

		private boolean cacheMoreFromStore() {
			int rowsread = 0;
			try {
				String json = null;
				while ( rowsread < READCACHELIMIT && null != ( json = reader.readLine() ) ) {
					LoadingNodeAndPropertyValues nap
							= oxm.readValue( json, LoadingNodeAndPropertyValues.class );
					if ( !removedNodes.contains( nap ) ) {
						rowsread++;
						readcache.add( nap );
					}
				}
			}
			catch ( IOException ioe ) {
				log.warn( "problem reading from cache", ioe );
			}

			return !readcache.isEmpty();
		}

		@Override
		public LoadingNodeAndPropertyValues next() {
			current = ( null == memoryiter ? readcache.poll() : memoryiter.next() );
			return current;
		}

		@Override
		public void remove() {
			// if we're working off the backing store, just make a note of the 
			// removed node. else actually remove it from the in-memory list
			if ( null == memoryiter ) {
				removedNodes.add( current );
			}
			else {
				memoryiter.remove();
			}
		}

		@Override
		public void release() {
			readcache.clear();
			try {
				reader.close();
			}
			catch ( IOException ioe ) {
				// don't really care
			}
		}
	}
}
