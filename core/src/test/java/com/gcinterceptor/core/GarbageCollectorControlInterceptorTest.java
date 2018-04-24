package com.gcinterceptor.core;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GarbageCollectorControlInterceptorTest {

	@Test
	public void testGarbageCollectorControlInterceptorHeapExecutor() throws InterruptedException {
		FakeRuntime heap = new FakeRuntime();
		GarbageCollectorControlInterceptor gci = new GarbageCollectorControlInterceptor(heap);

		ShedResponse sr;
		for (int i = 1; i <= 63; i++) { // The default sample size is 64. So, we want call before 63 times...
			sr = gci.before(null);
			assertFalse(sr.shouldShed);
			assertFalse(heap.hasChecked()); // Note that we can do it outside the loop, but here we can find out which
											// interaction fail.
			assertFalse(heap.hasCollected()); // Same here...
			gci.after(sr);
		}
		sr = gci.before(null);
		assertFalse(sr.shouldShed);
		assertTrue(heap.hasChecked()); // That the 64º before call, it should have already checked.
		assertFalse(heap.hasCollected());
		gci.after(sr);

		heap.resetFlags();
		heap.fillMemory();
		for (int i = 1; i <= 63; i++) {
			sr = gci.before(null);
			assertFalse(sr.shouldShed);
			assertFalse(heap.hasChecked());
			assertFalse(heap.hasCollected());
			gci.after(sr);
		}
        heap.resetFlags();
        sr = gci.before(null);
        assertTrue(sr.shouldShed);
        assertTrue(heap.hasChecked());
        assertFalse(heap.hasCollected());
        gci.after(sr);

        heap.resetFlags();
		sr = gci.before("GCI/0");
        assertFalse(heap.hasChecked());
		assertTrue(heap.hasCollected());
		gci.after(sr);
	}

	private class FakeRuntime extends RuntimeEnvironment {
		private boolean hasCollected;
		private boolean hasChecked;
		private long alloc;

		long getHeapUsageSinceLastGC() {
			hasChecked = true;
			return alloc;
		}

		long collect() {
			hasCollected = true;
			cleanMemory();
			return alloc;
		}

		boolean hasChecked() {
			return hasChecked;
		}

		boolean hasCollected() {
			return hasCollected;
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
