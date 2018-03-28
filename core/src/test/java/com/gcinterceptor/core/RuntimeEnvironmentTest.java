package com.gcinterceptor.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;

import org.junit.Test;

public class RuntimeEnvironmentTest {

	@Test
	public void testHeap() {
		FakeHeap heap = new FakeHeap();
		RuntimeEnvironment runtime = new RuntimeEnvironment(heap);

		assertEquals(0, runtime.getHeapUsageSinceLastGC());
		int n = 10;
		long arrayCost = 4000016; // 4 bytes * 10^6 + 16 bytes (array default cost) = 4,000016MB.
		for (int i = 1; i <= n; i++) {
			heap.alloc(arrayCost);
			assertEquals(i * arrayCost, runtime.getHeapUsageSinceLastGC());
		}
		assertEquals(n * arrayCost, runtime.collect());

		for (int i = 1; i <= n; i++) {
			heap.alloc(arrayCost);
			assertEquals(i * arrayCost, runtime.getHeapUsageSinceLastGC());
		}
		assertEquals(n * arrayCost, runtime.collect());

	}
	
	class FakeHeap extends RuntimeEnvironment.Heap {
		private long memoryUsedInBytes;
		
		void alloc(long toAlloc) {
			memoryUsedInBytes += toAlloc; 
		}
		
		@Override
		long getUsage() {
			return memoryUsedInBytes;
		}

		@Override
		void gc() {
			memoryUsedInBytes = 0;
		}
		
	}
	
}
