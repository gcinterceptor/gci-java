package com.gcinterceptor.core;

import java.lang.management.MemoryUsage;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Random;

class SheddingThreshold {
	private final Random random;
	private final RuntimeEnvironment rt;
	private AtomicLong threshold = new AtomicLong();

	SheddingThreshold(RuntimeEnvironment rt) {
		this(System.nanoTime(), rt);
	}

	SheddingThreshold(long seed, RuntimeEnvironment rt) {
		this.random = new Random(seed);
		this.rt = rt;
		this.threshold = new AtomicLong(64 * 1024 * 1024);
	}

	long get() {
		return threshold.get();
	}

	void update() {
	    // TODO: Adjust these constants based on spurious GC executions.
        	long max = this.rt.getMaxHeapUsage();
		threshold.set((long)(max - (max*0.3) - (max*0.2)*random.nextDouble()));
	}

	public String toString() {
		return "ST: " + threshold.get()/(1024*1024) + " MB";
	}
}
