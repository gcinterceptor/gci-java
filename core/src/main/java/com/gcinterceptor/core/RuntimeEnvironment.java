package com.gcinterceptor.core;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;

class RuntimeEnvironment {
	private Heap heap;
	private long lastAlloc; 
	private long lastUsed;

	RuntimeEnvironment(Heap heap) {
		this.heap = heap;
		lastAlloc = this.heap.getUsage();
	}

	RuntimeEnvironment() {
		this(new Heap());
	}
	
	long getHeapUsageSinceLastGC() {
		lastUsed = heap.getUsage() - lastAlloc;
		return lastUsed;
	}

	long collect() {
		long lastUsage = getHeapUsageSinceLastGC();
		heap.gc();
		lastAlloc = heap.getUsage();
		return lastUsage;
	}
	
	static class Heap {
		private MemoryPoolMXBean youngPool;
		
		Heap() {
			for (final MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
				if (pool.getName().contains("Eden")) {
					youngPool = pool;
					break;
				}
			}
		}
		
		long getUsage() {
			return  this.youngPool.getUsage().getUsed();
		}
		
		void gc() {
			System.gc();
		}
	}
}
