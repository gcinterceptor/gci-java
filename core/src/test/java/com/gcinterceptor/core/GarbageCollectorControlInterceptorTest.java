package com.gcinterceptor.core;

import static org.junit.Assert.*;

import org.junit.Test;

public class GarbageCollectorControlInterceptorTest {

	@Test
	public void testGarbageCollectorControlInterceptorHeapExecutor() throws InterruptedException {
		FakeRuntime runtime = new FakeRuntime();
		GarbageCollectorControlInterceptor gci = new GarbageCollectorControlInterceptor(runtime);
				
		assertFalse(runtime.hasChecked);
		assertFalse(runtime.hasCollected);
		
		gci.getHeapUsageSinceLastGC();
		assertTrue(runtime.hasChecked);
		assertFalse(runtime.hasCollected);
		
		gci.collect();
		assertTrue(runtime.hasChecked);
		assertTrue(runtime.hasCollected);
		
		runtime.resetFlags();
		assertFalse(runtime.hasChecked);
		assertFalse(runtime.hasCollected);
		
		runtime.fillMemory();
		long usage = gci.getHeapUsageSinceLastGC();
		assertTrue(runtime.hasChecked);
		assertFalse(runtime.hasCollected);
		assertEquals(usage, runtime.alloc);
		
		usage = gci.collect();
		assertTrue(runtime.hasChecked);
		assertTrue(runtime.hasCollected);
		assertEquals(usage, runtime.alloc);
		assertEquals(usage, 0);
		
	}

	private class FakeRuntime extends RuntimeEnvironment {
		private boolean hasCollected;
		private boolean hasChecked;
		private long alloc;

		@Override
		long getHeapUsageSinceLastGC() {
			hasChecked = true;
			return alloc;
		}

		@Override
		long collect() {
			hasCollected = true;
			cleanMemory();
			return alloc;
		}

		void resetFlags() {
			hasChecked = false;
			hasCollected = false;
		}

		void fillMemory() {
			alloc = Long.MAX_VALUE;
		}

		void cleanMemory() {
			alloc = 0;
		}
	}
}
