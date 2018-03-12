package com.gcinterceptor.gci;

/**
 * Holds the response of processing a single request from
 * {@code GarbageCollectorControlInterceptor}.
 */
class ShedResponse {
	/**
	 * Whether the request should be shed.
	 */
	boolean shouldShed;

	ShedResponse(boolean shouldShed) {
		this.shouldShed = shouldShed;
	}
}
