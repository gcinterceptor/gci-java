package com.gcinterceptor.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Executors;

import org.junit.Test;

public class GarbageCollectorControlInterceptorTest {

	@Test
	public void testGarbageCollectorControlInterceptorHeapExecutor() throws InterruptedException {
		FakeRuntime heap = new FakeRuntime();
		GarbageCollectorControlInterceptor gci = new GarbageCollectorControlInterceptor(heap,
				Executors.newSingleThreadExecutor());

		ShedResponse sr = null;
		for (int i = 1; i <= 63; i++) { // The default sample size is 64. So, we want call before 63 times...
			sr = gci.before();
			assertFalse(sr.shouldShed);
			assertFalse(heap.hasChecked()); // Note that we can do it outside the loop, but here we can find out which
											// interaction fail.
			assertFalse(heap.hasCollected()); // Same here...
			gci.after(sr);
		}
		sr = gci.before();
		assertFalse(sr.shouldShed);
		assertTrue(heap.hasChecked()); // That the 64º before call, it should have already checked.
		assertFalse(heap.hasCollected());
		gci.after(sr);

		heap.resetFlags();
		heap.fillMemory();
		for (int i = 1; i <= 63; i++) {
			sr = gci.before();
			assertFalse(sr.shouldShed);
			assertFalse(heap.hasChecked());
			assertFalse(heap.hasCollected());
			gci.after(sr);
		}
		sr = gci.before();
		assertTrue(sr.shouldShed); // We have call before 127 times and now the heap is full. ((127 + 1) % 64 = 0.
									// See GarbageCollectorControlInterceptor.)
		assertTrue(heap.hasChecked());
		assertFalse(heap.hasCollected()); // Since we call before 127 times, but after only 126, there is one "request
											// remaining" in queue.
		gci.after(sr);
		while (gci.doingGC()) {
			sr = gci.before();
			assertTrue(sr.shouldShed);
			Thread.sleep(5); // After sending the last request, we'll wait time enough to finish the shedding
								// flow.
		}

		assertTrue(heap.hasChecked());
		assertTrue(heap.hasCollected());

		heap.resetFlags(); // Reseting the flags to do the same as before.
		for (int i = 1; i <= 126; i++) { // It is 126 because sampler was updated.
			sr = gci.before();
			assertFalse(sr.shouldShed);
			assertFalse(heap.hasChecked());
			assertFalse(heap.hasCollected());
			gci.after(sr);
		}
		sr = gci.before();
		assertFalse(sr.shouldShed);
		assertTrue(heap.hasChecked());
		assertFalse(heap.hasCollected());
		gci.after(sr);

		heap.resetFlags();
		heap.fillMemory();
		for (int i = 1; i <= 126; i++) {
			sr = gci.before();
			assertFalse(sr.shouldShed);
			assertFalse(heap.hasChecked());
			assertFalse(heap.hasCollected());
			gci.after(sr);
		}
		sr = gci.before(); 
		assertTrue(sr.shouldShed);
		assertTrue(heap.hasChecked());
		assertFalse(heap.hasCollected());
		gci.after(sr);

		while (gci.doingGC()) {
			sr = gci.before(); 
			assertTrue(sr.shouldShed);
			Thread.sleep(5);
		} 

		assertTrue(heap.hasChecked());
		assertTrue(heap.hasCollected());
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
