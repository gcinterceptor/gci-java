package com.gcinterceptor.core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class GarbageCollectorControlInterceptor {
	private static final Duration WAIT_FOR_TRAILERS_SLEEP_MILLIS = Duration.ofMillis(10);
	private static final int SAMPLE_HISTORY_SIZE = 5;
	private static final String SHED_RATIO_CSV_FILE = System.getenv("SHED_RATIO_CSV_FILE");
	private AtomicBoolean doingGC;
	private AtomicLong incoming;
	private AtomicLong finished;
	private AtomicLong shedRequests;
	private RuntimeEnvironment runtime;
	private SheddingThreshold sheddingThreshold;
	private Sampler sampler;
	private Executor executor;
	private BufferedWriter bw;

	/**
	 * Creates a new instance of {@code GarbageCollectorControlInterceptor}
	 *
	 * @param runtime
	 *            {@code HeapMonitor} used to monitoring JVM heap pools.
	 * @param executor
	 *            thread pool used to trigger/control garbage collection.
	 */
	public GarbageCollectorControlInterceptor(RuntimeEnvironment runtime, Executor executor) {
		this.runtime = runtime;
		this.executor = executor;
		this.sampler = new Sampler(SAMPLE_HISTORY_SIZE);
		this.doingGC = new AtomicBoolean(false);
		this.incoming = new AtomicLong();
		this.finished = new AtomicLong();
		this.shedRequests = new AtomicLong();
		this.sheddingThreshold = new SheddingThreshold();

		if (SHED_RATIO_CSV_FILE != null) {
			initiateCSVFlow();
		}
	}

	private void initiateCSVFlow() {
		try {
			bw = new BufferedWriter(new FileWriter(SHED_RATIO_CSV_FILE, true));
		} catch (IOException e) {
			System.err.println(
					"IOException! GarbageCollectorControlInterceptor had problems to be initiated. Maybe the file can't be opened or created.");
		}

		Runtime.getRuntime().addShutdownHook(new Thread() { // Ensure that the file will be closed at the end.
			public void run() {
				try {
					bw.close();
				} catch (IOException e) {
					System.err.println("IOException! GarbageCollectorControlInterceptor had problems to close the BufferedWriter. It means that an I/O problem error has occurred.");
				}
			}
		});

		writeLine("finished", "shed");
	}

	/**
	 * Creates a new instance of {@code GarbageCollectorControlInterceptor} using
	 * defaults.
	 *
	 * @see RuntimeEnvironment
	 * @see System#gc()
	 * @see Executors#newSingleThreadExecutor()
	 * @see UnavailabilityDuration
	 * @see Clock#systemDefaultZone()
	 */
	public GarbageCollectorControlInterceptor() {
		this(new RuntimeEnvironment(), Executors.newSingleThreadExecutor());
	}

	private ShedResponse shed() {
		shedRequests.incrementAndGet();
		return new ShedResponse(true);
	}

	private void writeLine(String col1, String col2) {
		try {
			bw.write(col1 + "," + col2 + System.lineSeparator());
		} catch (IOException e) {
			System.err.println("IOException! GarbageCollectorControlInterceptor had problems to write data. It means that an I/O error has occurred.");
		}
	}

	boolean doingGC() {
		return doingGC.get();
	}
	
	public ShedResponse before() {
		// The service is unavailable.
		if (doingGC.get()) {
			return shed();
		}

		if ((incoming.get() + 1) % sampler.getCurrentSampleSize() == 0) {
			if (runtime.getHeapUsageSinceLastGC() > sheddingThreshold.get()) {
				// Starting unavailability period. 
				synchronized (this) {
					if (doingGC.get()) {
						return shed();
					} else {
						doingGC.set(true);
					}					
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
					long alloc = runtime.collect();

					// Update sampler and ST.
					sampler.update(finished.get());
					sheddingThreshold.update(alloc, finished.get(), shedRequests.get());

					if (SHED_RATIO_CSV_FILE != null) {
						writeLine(String.valueOf(finished.get()), String.valueOf(shedRequests.get()));
					}

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
	 * After must be called before the response is set to the client. It is strongly
	 * recommended that this is the last method called in the request processing
	 * chain.
	 */
	public void after(ShedResponse response) {
		if (!response.shouldShed) {
			finished.incrementAndGet();
		}
	}

}
