package com.gcinterceptor.gci;

import java.util.concurrent.atomic.AtomicLong;

public class SheddingThreshold {
	private final long MIN_SHEDDING_THRESHOLD = 32 * 1024 * 1024; // TODO(David) Update this value, if needed
	private final long MAX_SHEDDING_THRESHOLD = 512 * 1024 * 1024;  // TODO(David) Update this value, if needed 
	private final double START_MAX_OVER_HEAD = (float) 0.1; // Maximum accepted Overhead (#shed/#processed)
	private final long SMOOTH_FACTOR = 5;   // Smooth out the exponential decay.
	// Looking at the function we can see where that at 22 it reaches the overhead of 0.001.
	// https://www.wolframcloud.com/objects/danielfireman/gci_overhead_exp_decay
	private final int MAX_GCS =  23; // TODO(David) Update this value, if needed
	
	private AtomicLong threshold;
	private int numGCs;
	
	public SheddingThreshold() {
		threshold = new AtomicLong((long) (MIN_SHEDDING_THRESHOLD + (Math.random() * MIN_SHEDDING_THRESHOLD)));
	}
	
	public double getThreshold() {
		return threshold.get();
	}
	
	public void updateThreshold(int alloc, int finished, int shedRequests) {
		// Calculating the maximum overhead via exponential decay
		// https://en.wikipedia.org/wiki/Exponential_decay
		// https://www.wolframcloud.com/objects/danielfireman/gci_overhead_exp_decay
		double maxOverhead = (START_MAX_OVER_HEAD / Math.exp(SMOOTH_FACTOR* numGCs));
		// That way we avoid h.numGCs unbound growth.
		numGCs = Math.min(MAX_GCS, numGCs + 1);
		
		// Updating threshold value.
		long thresholdCandidate = 0;
		double overhead = (double) shedRequests / ((double) finished); 
		if ( overhead  > maxOverhead) {
			thresholdCandidate = (long) (alloc - (Math.random() * MIN_SHEDDING_THRESHOLD));
		} else {
			thresholdCandidate = (long) (alloc + (Math.random() * MIN_SHEDDING_THRESHOLD));
		}
		
		// Checking ST bounds.
		if (thresholdCandidate <= MIN_SHEDDING_THRESHOLD) {
			thresholdCandidate = (long) (MIN_SHEDDING_THRESHOLD + (Math.random() * MIN_SHEDDING_THRESHOLD));			
		}
		else if (thresholdCandidate >= MAX_SHEDDING_THRESHOLD) {
			thresholdCandidate = (long) (MAX_SHEDDING_THRESHOLD - (Math.random() * MIN_SHEDDING_THRESHOLD));			
		}
		
		threshold.set(thresholdCandidate); 
	}

}
