package com.ostrichemulators.semtool.util;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MetaphoneDistanceTest {

	@Test
	public void testDissimilar() {
		String s1 = "abcdefgh";
		String s2 = "1234567";
		MetaphoneDistance distanceCalc = new MetaphoneDistance();
		float distance = distanceCalc.getDistance(s1, s2);
		if (distance != 0.0f){
			fail("Distance of two unequal strings should be zero. Actual distance: " + distance);
		}
	}
	
	@Test
	public void testSimilar() {
		String s1 = "abcdefgh";
		String s2 = "abcdefgh";
		MetaphoneDistance distanceCalc = new MetaphoneDistance();
		float distance = distanceCalc.getDistance(s1, s2);
		if (distance != 1.0f){
			fail("Distance of two equal strings should be one. Actual distance: " + distance);
		}
	}

}
