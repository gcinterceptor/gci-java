package com.gcinterceptor.core;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class SamplerTest {
	private final int DEFAULT_SAMPLE_RATE = 64;
	private final int MAX_SAMPLE_RATE = 512;
	private Sampler sampler;
	
	@Before
	public void setUp() {
		sampler = new Sampler(5);
	}
	 
	@Test
	public void testSampler() {	
		assertEquals(DEFAULT_SAMPLE_RATE, sampler.getCurrentSampleSize());
		for (int i = 0; i < 5; i++) {
			sampler.update(80);			
			assertEquals(80, sampler.getCurrentSampleSize());
		}
		
		for (int i = 0; i < 4; i++) {
			sampler.update(MAX_SAMPLE_RATE);			
			assertEquals(80, sampler.getCurrentSampleSize());
		}
		sampler.update(MAX_SAMPLE_RATE);			
		assertEquals(MAX_SAMPLE_RATE, sampler.getCurrentSampleSize());
		
		sampler.update(DEFAULT_SAMPLE_RATE);
		assertEquals(DEFAULT_SAMPLE_RATE, sampler.getCurrentSampleSize());

	}
	
	@Test
	public void testBounds() {
		sampler.update(-1);			
		assertEquals(64, sampler.getCurrentSampleSize());
		for (int i = 0; i < 4; i++) {
			sampler.update(MAX_SAMPLE_RATE);
			assertEquals(64, sampler.getCurrentSampleSize());
		}
		sampler.update(MAX_SAMPLE_RATE);
		assertEquals(MAX_SAMPLE_RATE, sampler.getCurrentSampleSize());
		
		sampler.update(-1);			
		assertEquals(MAX_SAMPLE_RATE, sampler.getCurrentSampleSize());
		for (int i = 0; i < 4; i++) {
			sampler.update(2 * MAX_SAMPLE_RATE);
			assertEquals(MAX_SAMPLE_RATE, sampler.getCurrentSampleSize());
		}
		sampler.update(2 * MAX_SAMPLE_RATE);
		assertEquals(MAX_SAMPLE_RATE, sampler.getCurrentSampleSize());

	}

}
