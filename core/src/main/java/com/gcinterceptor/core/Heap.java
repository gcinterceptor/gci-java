package com.gcinterceptor.core;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;

class Heap {
	private MemoryPoolMXBean youngPool;
	private long lastAlloc; 
	private long lastUsed;

	Heap() {
		for (final MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
			if (pool.getName().contains("Eden")) {
				youngPool = pool;
				break;
			}
		}
		lastAlloc = getUsage();
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
		System.gc();
		lastAlloc = getUsage();
		return lastAllocAux;
	}

}
