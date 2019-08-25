/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.util;

import com.ostrichemulators.semtool.rdf.engine.impl.InMemorySesameEngine;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.model.IRI;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.ntriples.NTriplesWriter;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLWriter;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;

/**
 *
 * @author ryan
 */
public class UtilityTest {

	public UtilityTest() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testSortUrisByLabel() {
		Map<IRI, String> input = new HashMap<>();
		input.put( RDFS.CLASS, "Z" );
		input.put( RDFS.DOMAIN, "A" );

		Map<IRI, String> sorted = Utility.sortIrisByLabel( input );
		List<IRI> uris = Arrays.asList( RDFS.DOMAIN, RDFS.CLASS );
		assertEquals( uris, new ArrayList<>( sorted.keySet() ) );
	}

	@Test
	public void testDuration() {
		Date start = new Date( 1092834757 );
		Date ender = new Date( 1092854747 );
		assertEquals( "19.99s", Utility.getDuration( start, ender ) );

		ender = new Date( 1102723857 );
		assertEquals( "44m, 49.10s", Utility.getDuration( start, ender ) );
	}

	@Test
	public void testExporter() throws IOException {
		Map<String, Class<?>> handlers = new HashMap<>();
		handlers.put( "filename.ttl", TurtleWriter.class );
		handlers.put( "filename.rdf", RDFXMLWriter.class );
		handlers.put( "filename.x", NTriplesWriter.class );

		for ( Map.Entry<String, Class<?>> en : handlers.entrySet() ) {
			RDFHandler handler;
			try ( StringWriter sw = new StringWriter() ) {
				handler = Utility.getExporterFor( en.getKey(), sw );
			}
			assertEquals( en.getValue(), handler.getClass() );
		}
	}

	@Test
	public void testGetLabels() throws Exception {
		InMemorySesameEngine eng = InMemorySesameEngine.open();
		eng.getRawConnection().begin();
		eng.getRawConnection().add( SimpleValueFactory.getInstance().createStatement( RDFS.ISDEFINEDBY,
				RDFS.LABEL, SimpleValueFactory.getInstance().createLiteral( "my label" ) ) );
		eng.getRawConnection().add( SimpleValueFactory.getInstance().createStatement( RDFS.MEMBER,
				RDFS.LABEL, SimpleValueFactory.getInstance().createLiteral( "my label 2" ) ) );
		eng.getRawConnection().commit();

		String label = Utility.getInstanceLabel( RDFS.ISDEFINEDBY, eng );
		assertEquals( "my label", label );

		Map<Resource, String> labels = Utility.getInstanceLabels( null, eng );
		assertTrue( labels.isEmpty() );

		labels = Utility.getInstanceLabels( Arrays.asList(
				RDFS.MEMBER, RDFS.DOMAIN ), eng );
		assertEquals( "domain", labels.get( RDFS.DOMAIN ) );
		assertEquals( "my label 2", labels.get( RDFS.MEMBER ) );

		labels = Utility.getInstanceLabels( Arrays.asList( RDFS.ISDEFINEDBY,
				RDFS.LITERAL ), null );
		assertEquals( 2, labels.size() );
	}

	@Test
	public void testUnzip() throws Exception {
		File file = File.createTempFile( "ziptest-", ".zip" );
		File outdir = File.createTempFile( "ziptestdir-", "" );
		outdir.delete();
		outdir.mkdirs();

		try ( ZipOutputStream zout = new ZipOutputStream( new BufferedOutputStream(
				new FileOutputStream( file ) ) ) ) {
			zout.putNextEntry( new ZipEntry( "testdir/" ) );
			zout.closeEntry();

			zout.putNextEntry( new ZipEntry( "testdir-2/test.txt" ) );
			zout.write( "Hello World!".getBytes() );
			zout.closeEntry();

			zout.putNextEntry( new ZipEntry( "test.empty" ) );
			zout.write( "".getBytes() );
			zout.closeEntry();
		}

		Utility.unzip( file.getPath(), outdir.getPath() );
		File[] firstdirs = outdir.listFiles();
		File[] datas = new File( outdir, "testdir-2" ).listFiles();
		String data = FileUtils.readFileToString( datas[0], Charset.defaultCharset() );

		FileUtils.deleteQuietly( outdir );
		FileUtils.deleteQuietly( file );

		assertEquals( 3, firstdirs.length );
		assertEquals( "Hello World!", data );
	}

	@Test
	public void testImplode() {
		assertEquals( "<one>,<two>",
				Utility.implode( Arrays.asList( "one", "two" ), "<", ">", "," ) );
		assertTrue( Utility.implode( null, "", "", "" ).isEmpty() );
	}

	@Test
	public void testSaveFilename() {
		assertTrue( Utility.getSaveFilename( "junker", ".x" ).startsWith( "junker" ) );
		assertTrue( Utility.getSaveFilename( "junker", "x" ).endsWith( ".x" ) );
	}

	@Test
	public void testRound() {
		double expected = 1.354d;
		assertEquals( expected, Utility.round( 1.3540d, 3 ), 0.0005 );
	}

	@Test
	public void testMergeProps1() {
		Properties base = new Properties();
		base.setProperty( "one", "A" );
		base.setProperty( "two", "B" );

		Properties overlay = new Properties();
		overlay.setProperty( "three", "C" );
		overlay.setProperty( "two", "b" );

		Utility.mergeProperties( base, overlay, false, null );
		assertEquals( 3, base.size() );
		assertEquals( "C", base.getProperty( "three" ) );
		assertEquals( "b", base.getProperty( "two" ) );
	}

	@Test
	public void testMergeProps2() {
		Properties base = new Properties();
		base.setProperty( "one", "A" );
		base.setProperty( "two", "B" );

		Properties overlay = new Properties();
		overlay.setProperty( "three", "C" );
		overlay.setProperty( "one", "extra" );

		Utility.mergeProperties( base, overlay, true, "-" );
		assertEquals( 3, base.size() );
		assertEquals( "A-extra", base.getProperty( "one" ) );
	}
}
