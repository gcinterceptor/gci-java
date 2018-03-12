package com.gcinterceptor.gci;

/**
 * Holds the response of processing a single request from
 * {@code GarbageCollectorControlInterceptor}.
 * 
 * @author danielfireman
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
