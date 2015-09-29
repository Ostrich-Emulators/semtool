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
		TestPlaySheet sheet = new TestPlaySheet();
		File file = ExportUtility.getSuggestedFilename( sheet, ".pdf" );
		if ( !file.getName().contains( sheet.getTitle() ) ) {
			fail( "File name must contain the title of the component from which "
					+ "it is created." );
		}
	}

	@Test
	public void testExport() throws Exception {
		testPDFFile = File.createTempFile( "export-test-", ".pdf" );
		ExportUtility.exportAsPdf( image, testPDFFile );
		assertTrue( "File did not save properly.", testPDFFile.exists() );
		assertNotEquals( "File is 0-lenght.", 0, testPDFFile.length() );
	}

	private class TestPlaySheet extends JFrame implements IPlaySheet {

		private String title = "ATestingTitle";

		@Override
		public IEngine getEngine() {
			return null;
		}

		@Override
		public void setTitle( String title ) {
			this.title = title;
		}

		@Override
		public String getTitle() {
			return this.title;
		}

		@Override
		public boolean hasChanges() {
			return false;
		}

		@Override
		public Map<String, Action> getActions() {
			return null;
		}

		@Override
		public List<String> getHeaders() {
			return null;
		}

		@Override
		public void activated() {
		}

		@Override
		public void create( List<Value[]> data, List<String> headers,
				IEngine engine ) {
		}

		@Override
		public void create( Model m, IEngine engine ) {
		}

		@Override
		public void overlay( List<Value[]> data, List<String> headers,
				IEngine eng ) {
		}

		@Override
		public void overlay( Model m, IEngine engine ) {
		}

		@Override
		public void incrementFont( float incr ) {
		}

		@Override
		public boolean canAcceptDataWithHeaders( List<String> newheaders ) {
			return false;
		}

		@Override
		public boolean canAcceptModelData() {
			return false;
		}

		@Override
		public List<Object[]> getTabularData() {
			return null;
		}

		@Override
		public boolean prefersTabs() {
			return false;
		}

	}

}
