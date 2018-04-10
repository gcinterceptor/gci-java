package com.gcinterceptor.core;

import java.util.concurrent.atomic.AtomicLong;
import java.util.Random;

class SheddingThreshold {
	/**
	 * The minimum value of shedding threshold to collect.
	 */
	static final long MIN_SHEDDING_THRESHOLD = 32 * 1024 * 1024;

	/**
	 * The maximum value of shedding threshold to collect. Default heap threshold rate should
	 * be not too bigger, aiming at to avoid overflow of memory.
	 */
	// TODO(danielfireman): Can we get that from the JVM heap configuration?
	static final long MAX_SHEDDING_THRESHOLD = 256 * 1024 * 1024;

	private final Random random;
	private AtomicLong threshold;
	private int numGCs;

	SheddingThreshold() {
		this(System.nanoTime());
	}

	SheddingThreshold(long seed) {
		random = new Random(seed);
		threshold = new AtomicLong((long) (MIN_SHEDDING_THRESHOLD + (random.nextDouble() * MIN_SHEDDING_THRESHOLD)));
	}

	int numGCs() {
		return numGCs;
	}
	
	double get() {
		return threshold.get();
	}

	void update(long alloc, long finished, long shedRequests) {
		// Updating threshold value.
		long thresholdCandidate = 0;
		double overhead = (double) shedRequests / (double) finished;
		if (overhead > maxOverhead()) {
			// Agressively descrease if the overhead is bigger than maximum allowed.
			thresholdCandidate = (long) (alloc - 2*(random.nextDouble() * MIN_SHEDDING_THRESHOLD));
		} else {
			thresholdCandidate = (long) (alloc + (random.nextDouble() * MIN_SHEDDING_THRESHOLD));
		}	
		// Restricting bounds.	
		if (thresholdCandidate <= MIN_SHEDDING_THRESHOLD) {
			thresholdCandidate = (long) (MIN_SHEDDING_THRESHOLD + (random.nextDouble() * MIN_SHEDDING_THRESHOLD));
		} else if (thresholdCandidate >= MAX_SHEDDING_THRESHOLD) {
			thresholdCandidate = (long) (MAX_SHEDDING_THRESHOLD - (random.nextDouble() * MIN_SHEDDING_THRESHOLD));
		}
		threshold.set(thresholdCandidate);
	}

	// Calculates the maximum overhead when an update happens.
	// Package-protected for testing purposes.
	double maxOverhead() {
		// Calculating the maximum overhead via exponential decay
		// https://en.wikipedia.org/wiki/Exponential_decay
		// https://www.wolframcloud.com/objects/danielfireman/gci_overhead_exp_decay
		// Looking at the graph above we can see where that at x=9, the function
		// reaches the overhead of 0.001.
		double maxOverhead = 0.1 / Math.exp(numGCs/2.0);  // 2.0 is a smooth factor.
		numGCs = Math.min(9, numGCs + 1);
		return maxOverhead;
	}

	public String toString() {
		return "ST: " + threshold.get()/(1024*1024) + " MB";
	}
}
