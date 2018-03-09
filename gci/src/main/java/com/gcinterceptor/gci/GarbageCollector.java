package com.gcinterceptor.gci;

/**
 * Abstract garbage the runtime's garbage collector. Meant to be used by tests.
 *
 * @author danielfireman
 */
@FunctionalInterface
public interface GarbageCollector {
	void collect();
}
