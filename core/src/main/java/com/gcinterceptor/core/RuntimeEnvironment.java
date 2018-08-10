package com.gcinterceptor.core;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;

public class RuntimeEnvironment {
	private MemoryPoolMXBean youngPool;
	private MemoryPoolMXBean tenuredPool;
	private long youngLastAlloc;
	private long tenuredLastAlloc;

	public RuntimeEnvironment() {
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
		youngLastAlloc = youngPool.getUsage().getUsed();
		tenuredLastAlloc = tenuredPool.getUsage().getUsed();
	}

	public void collect() {
		GC.force();
		youngLastAlloc = getYoungHeapUsage();
		tenuredLastAlloc = getTenuredHeapUsage();
	}

	public long getYoungHeapUsageSinceLastGC() {
		return getYoungHeapUsage() - youngLastAlloc;
	}

	public long getTenuredHeapUsageSinceLastGC() {
		return getTenuredHeapUsage() - tenuredLastAlloc;
	}

	private long getYoungHeapUsage() {
		return youngPool.getUsage().getUsed();
	}

	private long getTenuredHeapUsage() {
		return tenuredPool.getUsage().getUsed();
	}

}
