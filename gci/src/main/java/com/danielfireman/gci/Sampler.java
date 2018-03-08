package com.danielfireman.gci;

import java.util.concurrent.atomic.AtomicInteger;

public class Sampler {
	// Default sample rate should be fairly small, so big requests get checked up quickly.
	private final int DEFAULT_SAMPLE_RATE = 10; // TODO(David) Update this value, if needed
	// Max sample rate can not be very big because of peaks.
	// The algorithm is fairly conservative, but we never know.
	private final int MAX_SAMPLE_RATE = 30; // TODO(David) Update this value, if needed
	
	private int next;
	private int[] pastSampleRates;
	private AtomicInteger currentSampleSize; 

	public Sampler(int historySize) {
		currentSampleSize = new AtomicInteger(DEFAULT_SAMPLE_RATE); 
		pastSampleRates = new int[historySize];
		for (int i = 0; i < historySize; i++) { 
			pastSampleRates[i] = Integer.MAX_VALUE;
		}
	}
	
	public int getCurrentSampleSize() {
		return currentSampleSize.get();
	}
	
	public void updateSampler(int lastFinished) { 
		// Update history.
		pastSampleRates[next] = lastFinished;
		next = (next + 1) % pastSampleRates.length;
		
		// Get minimum value.
		int min = Integer.MIN_VALUE;
		for (int i = 0; i < pastSampleRates.length; i++) {
			if (min < pastSampleRates[i]) {
				min = pastSampleRates[i];
			}
		}

		// Update currentSampleSize
		if (min > 0) {
			Math.min(min, MAX_SAMPLE_RATE);
			currentSampleSize.set(min);
		}
		
	}

}
