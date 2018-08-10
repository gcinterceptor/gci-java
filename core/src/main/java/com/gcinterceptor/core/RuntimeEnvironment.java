package com.gcinterceptor.core;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;

public class RuntimeEnvironment {
	private Heap heap;
	private long youngLastAlloc;
	private long tenuredLastAlloc;
	private long youngLastUsed;
	private long tenuredLastUsed;

	public RuntimeEnvironment(Heap heap) {
		this.heap = heap;
		youngLastAlloc = this.heap.getYoungUsage();
		tenuredLastAlloc = this.heap.getTenuredUsage();
	}

	public RuntimeEnvironment() {
		this(new Heap());
	}

	public void collect() {
		heap.gc();
		youngLastAlloc = heap.getYoungUsage();
		tenuredLastAlloc = heap.getTenuredUsage();
	}

	public long getYoungHeapUsageSinceLastGC() {
		youngLastUsed = getYoungHeapUsage() - youngLastAlloc;
		return youngLastUsed;
	}

	public long getTenuredHeapUsageSinceLastGC() {
		tenuredLastUsed = getTenuredHeapUsage() - tenuredLastAlloc;
		return tenuredLastUsed;
	}

	private long getYoungHeapUsage() {
		return heap.getYoungUsage();
	}

	private long getTenuredHeapUsage() {
		return heap.getTenuredUsage();
	}

	static class Heap {
		private MemoryPoolMXBean youngPool;
		private MemoryPoolMXBean tenuredPool;

		Heap() {
			for (final MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
				if (pool.getName().contains("Eden")) {
					youngPool = pool;
					continue;
				}
				if (pool.getName().contains("Old")) {
					tenuredPool = pool;
					continue;
				}

				if (youngPool != null && tenuredPool != null) {
					break;
				}
			}

		}

		long getYoungUsage() {
			return this.youngPool.getUsage().getUsed();
		}

		long getTenuredUsage() {
			return this.tenuredPool.getUsage().getUsed();
		}

		void gc() {
			GC.force();
		}

	}
}
