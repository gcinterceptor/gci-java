package com.gcinterceptor.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RuntimeEnvironmentTest {
	
	@Test(expected=ExceptionInInitializerError.class)
	public void testLibgcFailedCall() {
		RuntimeEnvironment runtime = new RuntimeEnvironment();
		runtime.collect();
	}

	@Test
	public void testYoungHeap() {
		FakeHeap heap = new FakeHeap();
		RuntimeEnvironment runtime = new RuntimeEnvironment(heap);

		assertEquals(0, runtime.getYoungHeapUsageSinceLastGC());
		int n = 10;
		long arrayCost = 4000016; // 4 bytes * 10^6 + 16 bytes (array default cost) = 4,000016MB.
		for (int i = 1; i <= n; i++) {
			heap.alloc(arrayCost);
			assertEquals(i * arrayCost, runtime.getYoungHeapUsageSinceLastGC());
		}
		runtime.collect();
		assertEquals(0, runtime.getYoungHeapUsageSinceLastGC());

		for (int i = 1; i <= n; i++) {
			heap.alloc(arrayCost);
			assertEquals(i * arrayCost, runtime.getYoungHeapUsageSinceLastGC());
		}
		runtime.collect();
		assertEquals(0, runtime.getYoungHeapUsageSinceLastGC());
	}
	
	@Test
	public void testTenuredHeap() {
		FakeHeap heap = new FakeHeap();
		RuntimeEnvironment runtime = new RuntimeEnvironment(heap);

		assertEquals(0, runtime.getTenuredHeapUsageSinceLastGC());
		int n = 10;
		long arrayCost = 4000016; // 4 bytes * 10^6 + 16 bytes (array default cost) = 4,000016MB.
		for (int i = 1; i <= n; i++) {
			heap.alloc(arrayCost);
			assertEquals(i * arrayCost, runtime.getTenuredHeapUsageSinceLastGC());
		}
		runtime.collect();
		assertEquals(0, runtime.getTenuredHeapUsageSinceLastGC());

		for (int i = 1; i <= n; i++) {
			heap.alloc(arrayCost);
			assertEquals(i * arrayCost, runtime.getTenuredHeapUsageSinceLastGC());
		}
		runtime.collect();
		assertEquals(0, runtime.getTenuredHeapUsageSinceLastGC());

	}

	private class FakeHeap extends RuntimeEnvironment.Heap {
		private long memoryUsedInBytes;
		
		void alloc(long toAlloc) {
			memoryUsedInBytes += toAlloc; 
		}
		
		@Override
		long getYoungUsage() {
			return memoryUsedInBytes;
		}
		

		@Override
		long getTenuredUsage() {
			return memoryUsedInBytes;
		}

		@Override
		void gc() {
			memoryUsedInBytes = 0;
		}
		
	}
	
}
