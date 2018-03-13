package com.gcinterceptor.gci;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class GarbageCollectorControlInterceptor {
	private static final Duration WAIT_FOR_TRAILERS_SLEEP_MILLIS = Duration.ofMillis(10);
	private static final int SAMPLE_HISTORY_SIZE = 5;
	private AtomicBoolean doingGC;
	private AtomicLong incoming;
	private AtomicLong finished;
	private AtomicLong shedRequests;
	private Heap monitor;
	private SheddingThreshold sheddingThreshold;
	private Sampler sampler;
	private Executor executor;
	
	/**
	 * Creates a new instance of {@code GarbageCollectorControlInterceptor}
	 *
	 * @param monitor
	 *            {@code HeapMonitor} used to monitoring JVM heap pools.
	 * @param executor
	 *            thread pool used to trigger/control garbage collection.
	 */
	public GarbageCollectorControlInterceptor(Heap monitor, Executor executor) {
		this.monitor = monitor;
		this.executor = executor;
		this.sampler = new Sampler(SAMPLE_HISTORY_SIZE);
		this.doingGC = new AtomicBoolean(false);
		this.incoming = new AtomicLong();
		this.finished = new AtomicLong();
		this.shedRequests = new AtomicLong();;
	}

	/**
	 * Creates a new instance of {@code GarbageCollectorControlInterceptor} using
	 * defaults.
	 *
	 * @see Heap
	 * @see System#gc()
	 * @see Executors#newSingleThreadExecutor()
	 * @see UnavailabilityDuration
	 * @see Clock#systemDefaultZone()
	 */
	public GarbageCollectorControlInterceptor() {
		this(new Heap(), Executors.newSingleThreadExecutor());
	}

	private ShedResponse shed() {
		shedRequests.incrementAndGet();
		return new ShedResponse(true);
	}

	public ShedResponse before() {
		// The service is unavailable.
		if (doingGC.get()) {
			return shed();
		}
		
		if ((incoming.get() + 1) % sampler.getCurrentSampleSize() == 0) {
			if (monitor.getHeapUsageSinceLastGC() > sheddingThreshold.get()) {
				// Starting unavailability period.
				if (doingGC.get()) {
					return shed();
				}
				executor.execute(() -> {
					// Loop waiting for the queue to get empty.
					while (finished.get() < incoming.get()) {
						try {
							Thread.sleep(WAIT_FOR_TRAILERS_SLEEP_MILLIS.toMillis());
						} catch (InterruptedException ie) {
							throw new RuntimeException(ie);
						}
					}
					
					// Force a garbage collect and keep the memory usage before the collection. 
					long alloc = monitor.collect();
					
					// Update sampler and ST.
					sampler.update(finished.get());
					sheddingThreshold.update(alloc, finished.get(), shedRequests.get());

					// Zeroing counters.
					incoming.set(0); 
					finished.set(0); 
					shedRequests.set(0);

					// Finishing unavailability period.
					doingGC.set(false);
					
				});
				return shed();
			}
		}
		
		incoming.incrementAndGet();
		return new ShedResponse(false);
	}

	/**
	 *  After must be called before the response is set to the client.
	 *  It is strongly recommended that this is the last method called in the request
	 *  processing chain.
	 */
	public void after(ShedResponse response) {
		if (!response.shouldShed) {
			finished.incrementAndGet();
		}
	}
}
