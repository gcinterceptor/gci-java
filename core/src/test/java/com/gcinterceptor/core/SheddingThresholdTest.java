package com.gcinterceptor.core;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class SheddingThresholdTest {
	private final long MIN_SHEDDING_THRESHOLD = 32 * 1024 * 1024; 
	private final long MAX_SHEDDING_THRESHOLD = 512 * 1024 * 1024;
	private final int MAX_GCS = 23;
	private SheddingThreshold st;
	
	@Before
	public void setUp() {
		st = new SheddingThreshold();
	}
	
	@Test
	public void testSheddingThreshold() { 
		assertEquals(0, st.numbGCs());
		assertTrue(st.get() >= MIN_SHEDDING_THRESHOLD && st.get() <= 2 * MIN_SHEDDING_THRESHOLD);
	}

	@Test
	public void testUpdate() {
		long alloc = 256 * 1025 * 1024;
		st.update(alloc, 1000, 50); // It is fine for the first run (see startMaxOverhead).
		assertTrue(st.get() > alloc);
		for (int i = 0; i < MAX_GCS; i++) {
			st.update(alloc, 1000, 50);	
		}
		st.update(alloc, 1000, 50);
		assertTrue(st.get() < alloc);
		assertEquals(st.numbGCs(), MAX_GCS);
	}
	
	@Test
	public void testBounds() {
		st.update(MIN_SHEDDING_THRESHOLD / 2, 1000, 1000);
		assertTrue(st.get() >= MIN_SHEDDING_THRESHOLD);

		st.update(2 * MAX_SHEDDING_THRESHOLD, 1000, 1000);
		assertTrue(st.get() <= MAX_SHEDDING_THRESHOLD);
		
	}

}
