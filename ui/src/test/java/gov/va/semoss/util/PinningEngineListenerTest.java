package gov.va.semoss.util;

import static org.junit.Assert.*;
import gov.va.semoss.rdf.engine.util.EngineManagementException;
import gov.va.semoss.rdf.engine.util.EngineUtil;
import gov.va.semoss.ui.main.SemossPreferences;
import gov.va.semoss.user.LocalUserImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PinningEngineListenerTest {

	private File file = null;
	
	private ArrayList<String> extensions = new ArrayList<String>();
	
	private final PinningEngineListener listener = new PinningEngineListener();
			
	@Before
	public void setUp() throws Exception {
		String userDir = System.getProperty("user.home");
		file = new File(userDir + File.separator + "testFile.ttl");
		extensions.add(".ttl");
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public synchronized void loadFirstTest() {
		SemossPreferences prefs = SemossPreferences.getInstance();
		prefs.put(Constants.PIN_KEY, file.getAbsolutePath());
		Collection<String> pinned = prefs.getPinnedSmsses();
		if (file.exists()){
			listener.onFileCreate( file );
		}
		else {
			SemossPreferences.getInstance().removePin( file.getName() );
		}
	}
	
	private boolean fileHasMyExtension( File file ) {
		
		String ext = "." + FilenameUtils.getExtension( file.getName() );
		return extensions.contains( ext );
	}
	
	private void onFileCreate(File file) {
		
		if ( fileHasMyExtension( file ) ) {
			try {
				EngineUtil.getInstance().mount( file, true, true, new LocalUserImpl() );
			}
			catch ( EngineManagementException ioe ) {
				fail("Unable to mount database.  Engine management exception encountered.");
			}
		}
	}
	
	@Test
	public void onFileCreateTest(){
		if ( fileHasMyExtension( file ) ) {
			try {
				EngineUtil.getInstance().mount( file, true, true, new LocalUserImpl() );
			}
			catch ( EngineManagementException ioe ) {
				fail("Engine Management Exception occurred during onFileCreate.");
			}
		}
	}
	
	@Test
	public void onFileDeleteTest( ) {
		// if we care, remove the engine from the list
		if (!fileHasMyExtension( file ) ) {
			fail("File extension mismatch");
		}
	}

}
