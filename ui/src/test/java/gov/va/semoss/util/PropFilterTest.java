package gov.va.semoss.util;

import java.io.File;

import org.junit.Test;

public class PropFilterTest {

	private final PropFilter filter = new PropFilter();
	
	private final File dir = new File(System.getProperty("user.home"));
	
	@Test
	public void positiveTest() {
		boolean accept = filter.accept(dir, "someName.ext");
		assert(accept);
	}
	
	@Test
	public void negativeTest() {
		boolean accept = filter.accept(dir, "someName.exo");
		assert(!accept);
	}

}
