package com.gcinterceptor.core;

import java.time.Clock;

public class GarbageCollectorControlInterceptor {
	private RuntimeEnvironment runtime;

	/**
	 * Creates a new instance of {@code GarbageCollectorControlInterceptor}
	 *
	 * @param runtime
	 *            {@code Runtime} used to interface with JVM heap.
	 */
	public GarbageCollectorControlInterceptor(RuntimeEnvironment runtime) {
		this.runtime = runtime;
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

    public long getHeapUsageSinceLastGC() {
        return this.runtime.getHeapUsageSinceLastGC();
    }
    
    public long collect() {
        return this.runtime.collect();
    }
	
	
}
