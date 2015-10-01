package gov.va.semoss.util;

import static org.junit.Assert.*;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.ui.components.api.IPlaySheet;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JFrame;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.Value;


public class ExportUtilityTest {

	private BufferedImage image = null;

	private File testPDFFile = null;

	@Before
	public void setUp() throws Exception {
		image = GuiUtility.loadImage( "icons16/save_diskette1_16.png" );
	}

	@After
	public void tearDown() throws Exception {
		image = null;
		if ( testPDFFile != null ) {
			testPDFFile.delete();
		}
	}

	@Test
	public void testFileName() {
		File file = ExportUtility.getSuggestedFilename( "A Test File", ".pdf" );
		assertTrue( "File name must contain the title of the component from which it is created",
				file.getName().contains( "A Test File" ) );
	}

	@Test
	public void testExport() throws Exception {
		testPDFFile = File.createTempFile( "export-test-", ".pdf" );
		ExportUtility.exportAsPdf( image, testPDFFile );
		assertTrue( "File did not save properly.", testPDFFile.exists() );
		assertNotEquals( "File is 0-length.", 0, testPDFFile.length() );
	}
}
