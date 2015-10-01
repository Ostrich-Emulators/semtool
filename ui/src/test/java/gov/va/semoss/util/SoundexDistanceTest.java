package gov.va.semoss.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class SoundexDistanceTest {

	@Test
	public void testDissimilar() {
		String s1 = "abcdefgh";
		String s2 = "1234567";
		SoundexDistance distanceCalc = new SoundexDistance();
		float distance = distanceCalc.getDistance(s1, s2);
		if (distance != 0.0f){
			fail("Distance of two unequal strings should be zero. Actual distance: " + distance);
		}
	}
	
	@Test
	public void testSimilar() {
		String s1 = "abcdefgh";
		String s2 = "abcdefgh";
		SoundexDistance distanceCalc = new SoundexDistance();
		float distance = distanceCalc.getDistance(s1, s2);
		if (distance != 1.0f){
			fail("Distance of two equal strings should be one. Actual distance: " + distance);
		}
	}
}
