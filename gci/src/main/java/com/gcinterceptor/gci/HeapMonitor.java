package com.gcinterceptor.gci;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;

class HeapMonitor {
	private MemoryPoolMXBean youngPool;
	private GarbageCollector collector;
	private long lastAlloc; 
	private long lastUsed;

	HeapMonitor() {
		for (final MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
			if (pool.getName().contains("Eden")) {
				youngPool = pool;
				break;
			}
		}
		lastAlloc = getUsage();
		collector = () -> System.gc();
	}

	private long getUsage() {
		return  this.youngPool.getUsage().getUsed();
	}

	long getHeapUsageSinceLastGC() {
		lastUsed = getUsage() - lastAlloc;
		return lastUsed;
	}

	long collect() {
		long lastAllocAux = getHeapUsageSinceLastGC();
		collector.collect();
		lastAlloc = getUsage();
		return lastAllocAux;
	}

}
