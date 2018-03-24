package com.gcinterceptor.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Executors;

import org.junit.Test;

public class GarbageCollectorControlInterceptorTest {

	@Test
	public void testGarbageCollectorControlInterceptorHeapExecutor() throws InterruptedException {
		FakeHeap heap = new FakeHeap();
		GarbageCollectorControlInterceptor GCI = new GarbageCollectorControlInterceptor(heap,
				Executors.newSingleThreadExecutor());

		ShedResponse sr = null;
		for (int i = 1; i <= 63; i++) { // The default sample size is 64. So, we want call before 63 times...
			sr = GCI.before();
			assertFalse(sr.shouldShed);
			assertFalse(heap.hasChecked()); // Note that we can do it outside the loop, but here we can find out which
											// interaction fail.
			assertFalse(heap.hasCollected()); // Same here...
			GCI.after(sr);
		}
		sr = GCI.before();
		assertFalse(sr.shouldShed);
		assertTrue(heap.hasChecked()); // That the 64º before call, it should have already checked.
		assertFalse(heap.hasCollected()); 
		GCI.after(sr);

		heap.resetFlags();
		heap.fillMemory();
		for (int i = 1; i <= 63; i++) {
			sr = GCI.before();
			assertFalse(sr.shouldShed);
			assertFalse(heap.hasChecked());
			assertFalse(heap.hasCollected());
			GCI.after(sr);
		}
		sr = GCI.before();
		assertTrue(sr.shouldShed); // We have call before 127 times and now the heap is full. ((127 + 1) % 64 = 0. See GarbageCollectorControlInterceptor.)
		assertTrue(heap.hasChecked());
		assertFalse(heap.hasCollected()); // Since we call before 127 times, but after only 126, there is one "request remaining" in queue. 
		GCI.after(sr); 
		Thread.sleep(15); // After sending the last request, we'll wait time enough to finish the shedding flow.
		assertTrue(sr.shouldShed);
		assertTrue(heap.hasChecked());
		assertTrue(heap.hasCollected()); 

		heap.resetFlags(); // Reseting the flags to do the same as before.
		for (int i = 1; i <= 126; i++) { // It is 126 because sampler was updated.
			sr = GCI.before();
			assertFalse(sr.shouldShed);
			assertFalse(heap.hasChecked());
			assertFalse(heap.hasCollected());
			GCI.after(sr);
		}
		sr = GCI.before();
		assertFalse(sr.shouldShed);
		assertTrue(heap.hasChecked());
		assertFalse(heap.hasCollected());
		GCI.after(sr);

		heap.resetFlags();
		heap.fillMemory();
		for (int i = 1; i <= 126; i++) {
			sr = GCI.before();
			assertFalse(sr.shouldShed);
			assertFalse(heap.hasChecked());
			assertFalse(heap.hasCollected());
			GCI.after(sr);
		}
		sr = GCI.before();
		assertTrue(sr.shouldShed);
		assertTrue(heap.hasChecked());
		assertFalse(heap.hasCollected());
		GCI.after(sr);
		Thread.sleep(15);
		assertTrue(sr.shouldShed);
		assertTrue(heap.hasChecked());
		assertTrue(heap.hasCollected());

	}

	private class FakeHeap implements IHeap {
		private boolean hasCollected;
		private boolean hasChecked;
		private long alloc;

		public long getHeapUsageSinceLastGC() {
			hasChecked = true;
			return alloc;
		}

		public long collect() {
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
