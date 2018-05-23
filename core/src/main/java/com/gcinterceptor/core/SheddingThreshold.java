package com.gcinterceptor.core;

import com.sun.management.GarbageCollectionNotificationInfo;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.openmbean.CompositeData;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;

class SheddingThreshold {
	/**
	 * The minimum value of shedding threshold to collect.
	 */
	static final long MIN_SHEDDING_THRESHOLD = 32 * 1024 * 1024;

	private AtomicLong maxSheddingThreshold = new AtomicLong(256 * 1024 * 1024);
	private final Random random;
	private AtomicLong threshold;
	private int numGCs;

	SheddingThreshold() {
		this(System.nanoTime());
	}

	SheddingThreshold(long seed) {
		random = new Random(seed);

		try {
			maxSheddingThreshold.set((long)(Long.parseLong(System.getenv("YOUNG_GEN"))*0.7));
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		}

		threshold = new AtomicLong((long)(maxSheddingThreshold.get()/3.0));


		List<GarbageCollectorMXBean> gcbeans = ManagementFactory.getGarbageCollectorMXBeans();
		for (GarbageCollectorMXBean gcbean : gcbeans) {
			NotificationEmitter emitter = (NotificationEmitter) gcbean;
			NotificationListener listener = (notification, handback) -> {
				if (notification.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {
					GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData());
					if (!info.getGcCause().equals("JvmtiEnv ForceGarbageCollection")) {
						maxSheddingThreshold.set((long)(maxSheddingThreshold.get() - MIN_SHEDDING_THRESHOLD));
						threshold.set((long)(maxSheddingThreshold.get() - (random.nextDouble() * MIN_SHEDDING_THRESHOLD)));
					}
				}
			};
			emitter.addNotificationListener(listener, null, null);
		}
	}

	int numGCs() {
		return numGCs;
	}

	double get() {
		return threshold.get();
	}

	void update(long alloc, long finished, long shedRequests) {
		long thresholdCandidate = 0;
		thresholdCandidate = (long) (alloc + (random.nextDouble() * MIN_SHEDDING_THRESHOLD));
		long maxSt = maxSheddingThreshold.get();
		if (thresholdCandidate >= maxSt) {
			thresholdCandidate = (long) (maxSt - (random.nextDouble() * MIN_SHEDDING_THRESHOLD));
		}
		threshold.set(thresholdCandidate);
	}

	public String toString() {
		return "ST: " + threshold.get()/(1024*1024) + " MB";
	}
}

