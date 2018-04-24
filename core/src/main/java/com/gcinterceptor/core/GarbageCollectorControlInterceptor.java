package com.gcinterceptor.core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class GarbageCollectorControlInterceptor {
	private static final Duration WAIT_FOR_TRAILERS_SLEEP_MILLIS = Duration.ofMillis(10);
	private static final int SAMPLE_HISTORY_SIZE = 5;
	private static final String SHED_RATIO_CSV_FILE = System.getenv("SHED_RATIO_CSV_FILE");
	private AtomicBoolean doingGC;
	private AtomicLong incoming;
	private AtomicLong finished;
	private RuntimeEnvironment runtime;
	private SheddingThreshold sheddingThreshold;
	private Sampler sampler;
	private BufferedWriter bw;

	/**
	 * Creates a new instance of {@code GarbageCollectorControlInterceptor}
	 *
	 * @param runtime
	 *            {@code Runtime} used to interface with JVM heap.
	 */
	public GarbageCollectorControlInterceptor(RuntimeEnvironment runtime) {
		this.runtime = runtime;
		this.sampler = new Sampler(SAMPLE_HISTORY_SIZE);
		this.doingGC = new AtomicBoolean(false);
		this.incoming = new AtomicLong();
		this.finished = new AtomicLong();
		this.sheddingThreshold = new SheddingThreshold(runtime);

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
	 * @see Clock#systemDefaultZone()
	 */
	public GarbageCollectorControlInterceptor() {
		this(new RuntimeEnvironment());
	}

	private ShedResponse shed() {
		return new ShedResponse(true);
	}

	private void writeLine(String col1, String col2) {
		try {
			bw.write(col1 + "," + col2 + System.lineSeparator());
			bw.flush();
		} catch (IOException e) {
			System.err.println("IOException! GarbageCollectorControlInterceptor had problems to write data. It means that an I/O error has occurred.");
		}
	}

	private synchronized boolean shouldGC() {
		double heapUsed = runtime.getHeapUsageSinceLastGC();
		double avgReqHeapUsage = heapUsed / finished.get();
		double heapUsedToProcessQueue = avgReqHeapUsage * (incoming.get()-finished.get());
		return heapUsed > (sheddingThreshold.get() - heapUsedToProcessQueue);
	}

	public ShedResponse before(String gciHeader) {
		if(gciHeader != null) {
				long shedRequests = Long.parseLong(gciHeader.substring(gciHeader.indexOf('/')+1));

				// Loop waiting for the queue to get empty.
				while (finished.get() < incoming.get()) {
					try {
						Thread.sleep(WAIT_FOR_TRAILERS_SLEEP_MILLIS.toMillis());
					} catch (InterruptedException ie) {
						throw new RuntimeException(ie);
					}
				}

				// Force a garbage collect and keep the memory usage before the collection.
				runtime.collect();

				// Update sampler and ST.
				sampler.update(finished.get());
                sheddingThreshold.update();

				if (SHED_RATIO_CSV_FILE != null) {
					writeLine(String.valueOf(finished.get()), String.valueOf(shedRequests));
				}

				// Zeroing counters.
				incoming.set(0);
				finished.set(0);

				doingGC.set(false);
				incoming.incrementAndGet();
				return new ShedResponse(false);
		 }

		// The service is unavailable.
		if (doingGC.get()) {
			return shed();
		}

		if ((incoming.get() + 1) % sampler.getCurrentSampleSize() == 0 && shouldGC()) {
			doingGC.set(true);
			return shed();
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
