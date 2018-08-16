package com.gcinterceptor.core;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;

public class RuntimeEnvironment {
	private MemoryPoolMXBean youngPool;
	private MemoryPoolMXBean tenuredPool;

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
	}

	public void collect() {
		GC.force();
	}

	public long getYoungHeapUsage() {
		return youngPool.getUsage().getUsed();
	}

	public long getTenuredHeapUsage() {
		return tenuredPool.getUsage().getUsed();
	}

}
