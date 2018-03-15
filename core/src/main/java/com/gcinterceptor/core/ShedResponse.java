package com.gcinterceptor.core;

/**
 * Holds the response of processing a single request from
 * {@code GarbageCollectorControlInterceptor}.
 */
public class ShedResponse {
	/**
	 * Whether the request should be shed.
	 */
	public boolean shouldShed;

	public ShedResponse(boolean shouldShed) {
		this.shouldShed = shouldShed;
	}
}
