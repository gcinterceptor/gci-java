package com.gcinterceptor.core;

import java.util.concurrent.atomic.AtomicLong;

class SheddingThreshold {
	/**
	 * The minimum value of shedding threshold to collect. Default heap threshold rate should
	 * be fairly small, so the first collection happens quickly.
	 */
	private final long MIN_SHEDDING_THRESHOLD = 32 * 1024 * 1024; // TODO(David) Update this value, if needed

	/**
	 * The maximum value of shedding threshold to collect. Default heap threshold rate should
	 * be not too bigger, aiming at to avoid overflow of memory.
	 */
	private final long MAX_SHEDDING_THRESHOLD = 512 * 1024 * 1024; // TODO(David) Update this value, if needed

	/** Maximum accepted Overhead (#shed/#processed). */
	private final double START_MAX_OVERHEAD = (float) 0.1;

	/** Smooth out the exponential decay. */
	private final long SMOOTH_FACTOR = 5;

	/**
	 * Is the max number of collects that can be used to calculate the max overhead.
	 * Looking at the function we can see where that at 22 it reaches the overhead
	 * of 0.001.
	 * https://www.wolframcloud.com/objects/danielfireman/gci_overhead_exp_decay.
	 */
	private final int MAX_GCS = 23; // TODO(David) Update this value, if needed

	private AtomicLong threshold;
	private int numGCs;

	SheddingThreshold() {
		threshold = new AtomicLong((long) (MIN_SHEDDING_THRESHOLD + (Math.random() * MIN_SHEDDING_THRESHOLD)));
	}

	double get() {
		return threshold.get();
	}

	void update(long alloc, long finished, long shedRequests) {
		// Calculating the maximum overhead via exponential decay
		// https://en.wikipedia.org/wiki/Exponential_decay
		// https://www.wolframcloud.com/objects/danielfireman/gci_overhead_exp_decay
		double maxOverhead = (START_MAX_OVERHEAD / Math.exp(SMOOTH_FACTOR * numGCs));
		// That way we avoid h.numGCs unbound growth.
		numGCs = Math.min(MAX_GCS, numGCs + 1);

		// Updating threshold value.
		long thresholdCandidate = 0;
		double overhead = (double) shedRequests / (double) finished;
		if (overhead > maxOverhead) {
			thresholdCandidate = (long) (alloc - (Math.random() * MIN_SHEDDING_THRESHOLD));
		} else {
			thresholdCandidate = (long) (alloc + (Math.random() * MIN_SHEDDING_THRESHOLD));
		}

		// Checking ST bounds.
		if (thresholdCandidate <= MIN_SHEDDING_THRESHOLD) {
			thresholdCandidate = (long) (MIN_SHEDDING_THRESHOLD + (Math.random() * MIN_SHEDDING_THRESHOLD));
		} else if (thresholdCandidate >= MAX_SHEDDING_THRESHOLD) {
			thresholdCandidate = (long) (MAX_SHEDDING_THRESHOLD - (Math.random() * MIN_SHEDDING_THRESHOLD));
		}

		threshold.set(thresholdCandidate);
	}

}
