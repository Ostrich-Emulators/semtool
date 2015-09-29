package gov.va.semoss.util;

import java.io.File;

import org.junit.Test;

public class PropFilterTest {

	private final PropFilter filter = new PropFilter();
	
	private final File dir = new File("~/");
	
	@Test
	public void positiveTest() {
		boolean accept = filter.accept(dir, "someNameExt");
		assert(accept);
	}
	
	@Test
	public void negativeTest() {
		boolean accept = filter.accept(dir, "someNameExo");
		assert(!accept);
	}

}
