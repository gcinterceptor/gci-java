package com.gcinterceptor.gci;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;

public class HeapMonitor {
	private MemoryPoolMXBean youngPool;
	private GarbageCollector collector;
	private long lastAlloc; 
	private long lastUsed;

	public HeapMonitor() {
		for (final MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
			// TODO(danielfireman): Generalize this to other JVM versions.
			if (pool.getName().contains("Eden")) {
				youngPool = pool;
				break;
			}
		}
		lastAlloc = getUsage();
		collector = () -> System.gc();
	}

	Long getUsage() {
		return  this.youngPool.getUsage().getUsed();
	}

	Long AllocSinceLastGC() {
		lastUsed = getUsage() - lastAlloc;
		return lastUsed;
	}
	
	Long collect() {
		Long lastAllocAux = AllocSinceLastGC();
		collector.collect();
		lastAlloc = getUsage();
		return lastAllocAux;
	}

}
