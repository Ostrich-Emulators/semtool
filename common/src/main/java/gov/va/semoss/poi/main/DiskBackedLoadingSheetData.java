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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import jdbm.PrimaryStoreMap;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
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
public class DiskBackedLoadingSheetData extends LoadingSheetData {

	private static final Logger log = Logger.getLogger( DiskBackedLoadingSheetData.class );
	private static final long COMMITLIMIT = 100000;

	private final PrimaryStoreMap<Long, String> data;
	private RecordManager recman;
	private File cachedir;
	private long datacount = 0;
	private long lastcommit = 0;
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

	protected DiskBackedLoadingSheetData( String tabtitle, String sType, String oType,
			String relname, Map<String, URI> props ) throws IOException {
		super( tabtitle, sType, oType, relname, props );

		try {
			File tmpdir = new File( FileUtils.getTempDirectory(), "loading-cache" );
			cachedir = new File( tmpdir, tabtitle + "-"
					+ RandomStringUtils.randomAlphanumeric( 6 ) );
			cachedir.mkdirs();
			String filenamer = new File( cachedir, "cache" ).getPath();
			recman = RecordManagerFactory.createRecordManager( filenamer );
		}
		catch ( IOException x ) {
			log.error( x, x );
			if ( null != recman ) {
				try {
					recman.close();
				}
				catch ( IOException xx ) {
					log.warn( xx, xx );
				}
			}
			FileUtils.deleteQuietly( cachedir );

			throw x;
		}

		SimpleModule sm = new SimpleModule();
		sm.addSerializer( LoadingNodeAndPropertyValues.class, new NapSerializer() );
		sm.addDeserializer( LoadingNodeAndPropertyValues.class, new NapDeserializer() );
		oxm.registerModule( sm );

		data = recman.storeMap( tabtitle );
	}

	@Override
	public boolean isMemOnly() {
		return true;
	}

	/**
	 * Releases any resources used by this class.
	 */
	@Override
	public void release() {
		try {
			recman.close();
		}
		catch ( IOException ioe ) {
			log.warn( "problem releasing cache", ioe );
		}

		try {
			FileUtils.deleteDirectory( cachedir );
		}
		catch ( IOException ioe ) {
			log.warn( "problem cleaning up tmp cache", ioe );
		}
	}

	@Override
	public void finishLoading() {
		try {
			recman.clearCache();
		}
		catch ( IOException ioe ) {
			log.warn( "could not clear cache", ioe );
		}
	}

	/**
	 * Clears any stored loading data
	 */
	@Override
	public void clear() {
		super.clear();
		data.clear();
		commit();
	}

	@Override
	public boolean isEmpty() {
		return data.isEmpty();
	}

	@Override
	protected void commit() {
		try {
			recman.commit();
			recman.clearCache(); // mostly during loads, we don't need a cache
			lastcommit = data.size();
		}
		catch ( IOException ioe ) {
			log.warn( "loading sheet internal commit failed", ioe );
		}
	}

	@Override
	public List<LoadingNodeAndPropertyValues> getData() {
		List<LoadingNodeAndPropertyValues> list = new ArrayList<>();
		try {
			for ( Map.Entry<Long, String> en : data.entrySet() ) {
				list.add( oxm.readValue( en.getValue(), LoadingNodeAndPropertyValues.class ) );
			}
		}
		catch ( IOException ioe ) {
			log.error( ioe, ioe );
		}
		return list;
	}

	@Override
	public int rows() {
		return data.size();
	}

	@Override
	protected void iadd( LoadingNodeAndPropertyValues nap ) {
		try {
			data.putValue( oxm.writeValueAsString( nap ) );
			datacount++;
		}
		catch ( Throwable t ) {
			log.error( t, t );
		}

		if ( ( datacount - lastcommit ) >= COMMITLIMIT ) {
			commit();
		}
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
				jgen.writeStartObject();
				jgen.writeStringField( "prop", en.getKey() );
				jgen.writeStringField( "value", en.getValue().stringValue() );
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
				nap.put( prop, RDFDatatypeTools.getRDFStringValue( valstr, null, vf ) );
			}

			return nap;
		}
	}
}
