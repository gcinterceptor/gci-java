package com.gcinterceptor.gci;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

class Sampler {
	// Default sample rate should be fairly small, so big requests get checked up quickly.
	private final int DEFAULT_SAMPLE_RATE = 64; // TODO(David) Update this value, if needed
	// Max sample rate can not be very big because of peaks.
	// The algorithm is fairly conservative, but we never know.
	private final int MAX_SAMPLE_RATE = 512; // TODO(David) Update this value, if needed
	
	private int next;
	private int[] pastSampleRates;
	private AtomicInteger currentSampleRate; 

	Sampler(int historySize) {
		currentSampleRate = new AtomicInteger(DEFAULT_SAMPLE_RATE); 
		pastSampleRates = new int[historySize];
		for (int i = 0; i < historySize; i++) { 
			pastSampleRates[i] = Integer.MAX_VALUE;
		}
	}
	
	int getCurrentSampleSize() {
		return currentSampleRate.get();
	}
	
	void updateSampler(int lastFinished) { 
		// Update history.
		pastSampleRates[next] = lastFinished;
		next = (next + 1) % pastSampleRates.length;
		
		int min = Arrays.stream(pastSampleRates).min().getAsInt();

		if (min > 0) {	
			currentSampleRate.set(Math.min(min, MAX_SAMPLE_RATE));
		}
		
	}

}
