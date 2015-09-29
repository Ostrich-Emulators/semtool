package gov.va.semoss.util;

import java.io.File;

import org.junit.Test;

public class PropFilterTest {

	private final PropFilter filter = new PropFilter();
	
	private final File dir = new File(System.getProperty("user.home"));
	
	@Test
	public void positiveTest() {
		boolean accept = filter.accept(dir, "someName.smss");
		assert(accept);
	}
	
	@Test
	public void negativeTest() {
		boolean accept = filter.accept(dir, "someName.smss1");
		assert(!accept);
	}


}
