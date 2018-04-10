package com.gcinterceptor.core;

import static com.gcinterceptor.core.SheddingThreshold.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class SheddingThresholdTest {
	private SheddingThreshold st;
	
	@Before
	public void setUp() {
		st = new SheddingThreshold();
	}
	
	@Test
	public void testSheddingThreshold() { 
		assertEquals(0, st.numGCs());
		assertTrue(st.toString(), st.get() >= MIN_SHEDDING_THRESHOLD && st.get() <= 2 * MIN_SHEDDING_THRESHOLD);
	}

	@Test
	public void testUpdateDecrease() {
		long alloc = 10 * 1025 * 1024;
		st.update(alloc, 1000, 50);
		assertTrue(st.toString(), st.get() > alloc);
	}

	@Test
	public void testUpdateIncrease() {
		long alloc = 256 * 1025 * 1024;
		st.update(alloc, 1000, 1000);
		assertTrue(st.toString(), st.get() < alloc);
	}
	
	@Test
	public void testUpdateBounds() {
		st.update(MIN_SHEDDING_THRESHOLD / 2, 1000, 1000);
		assertTrue(st.get() >= MIN_SHEDDING_THRESHOLD);

		st.update(2 * MAX_SHEDDING_THRESHOLD, 1000, 1000);
		assertTrue(st.get() <= MAX_SHEDDING_THRESHOLD);
	}

	@Test
	public void testMaxOverhead() {
		double oldMaxOverhead = st.maxOverhead();
		assertTrue("Expected: " + oldMaxOverhead + " <= 0.1", oldMaxOverhead <= 0.1);
		// Check the method maxOverhead to a great explanation where the 
		// 23 comes from.
		for (int i=0; i < 9; i++) {
			double newMaxOverhead = st.maxOverhead();
			assertTrue("Iteration: " + i + " newMaxOverhead:" + newMaxOverhead + " oldMaxOverhead:" + oldMaxOverhead, newMaxOverhead < oldMaxOverhead);
			oldMaxOverhead = newMaxOverhead;
		}
		assertEquals(0.001, st.maxOverhead(), 0.001);
		assertEquals(0.001, st.maxOverhead(), 0.001);
		assertEquals(0.001, st.maxOverhead(), 0.001);
	}
}
