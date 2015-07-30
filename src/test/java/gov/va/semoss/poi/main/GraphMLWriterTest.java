/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.poi.main;

import java.io.File;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 *
 * @author ryan
 */
public class GraphMLWriterTest {
	
	private static final File EXPECTED = new File( "src/test/resources/graphtest.graphml" );
	private static final Logger log = Logger.getLogger( GraphMLWriterTest.class );
	private static final String now;
	
	static {
		Calendar cal = Calendar.getInstance();
		cal.set( 2031, 9, 22, 6, 58, 59 );
		cal.set( Calendar.MILLISECOND, 15 );
		cal.setTimeZone( TimeZone.getTimeZone( "GMT-04:00" ) );
		SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSSXXX" );
		sdf.setTimeZone( TimeZone.getTimeZone( "GMT-04:00" ) );
		now = sdf.format( cal.getTime() );
	}
	
	@Test
	public void testWrite() throws Exception {
		LoadingSheetData rels = LoadingSheetData.relsheet( "Human Being", "Car", "Purchased" );
		rels.addProperties( Arrays.asList( "Price", "Date" ) );
		
		LoadingSheetData nodes = LoadingSheetData.nodesheet( "Human Being" );
		nodes.addProperties( Arrays.asList( "First Name", "Last Name" ) );
		
		ImportData data = new ImportData();
		data.add( rels );
		data.add( nodes );
		
		ValueFactory vf = new ValueFactoryImpl();
		Map<String, Value> props = new HashMap<>();
		props.put( "Price", vf.createLiteral( "3000 USD" ) );
		props.put( "Date", vf.createLiteral( now, XMLSchema.DATETIME ) );
		rels.add( "Yuri", "Yugo", props );
		rels.add( "Yuri", "Pinto" );
		
		Map<String, Value> hprop = new HashMap<>();
		hprop.put( "First Name", vf.createLiteral( "Yuri" ) );
		hprop.put( "Last Name", vf.createLiteral( "Gagarin" ) );
		nodes.add( "Yuri", hprop );
		
		GraphMLWriter writer = new GraphMLWriter();
		StringWriter strings = new StringWriter();
		writer.write( data, strings );
		
		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			File trace = new File( tmpdir, EXPECTED.getName() );
			FileUtils.write( trace, strings.toString() );
		}
		
//		log.fatal( strings.toString() );
//		log.fatal(  FileUtils.readFileToString( EXPECTED ) );
		assertEquals( FileUtils.readFileToString( EXPECTED ), strings.toString() );
	}
}
