package com.gcinterceptor.core;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

class Sampler {
	// Default sample rate should be fairly small, so big requests get checked up quickly.
	private final int DEFAULT_SAMPLE_RATE = 64;
	// Max sample rate can not be very big because of peaks.
	// The algorithm is fairly conservative, but we never know.
	private final int MAX_SAMPLE_RATE = 512;
	
	private int next;
	private long[] pastSampleRates;
	private AtomicLong currentSampleRate; 

	Sampler(int historySize) {
		currentSampleRate = new AtomicLong(DEFAULT_SAMPLE_RATE); 
		pastSampleRates = new long[historySize];
		for (int i = 0; i < historySize; i++) { 
			pastSampleRates[i] = Integer.MAX_VALUE;
		}
	}
	
	long getCurrentSampleSize() {
		return currentSampleRate.get();
	}
	
	void update(long finished) { 
		pastSampleRates[next] = finished;
		next = (next + 1) % pastSampleRates.length;
		long min = Arrays.stream(pastSampleRates).min().getAsLong();
		if (min > 0) {	
			currentSampleRate.set(Math.min(min, MAX_SAMPLE_RATE));
		}
	}
}
