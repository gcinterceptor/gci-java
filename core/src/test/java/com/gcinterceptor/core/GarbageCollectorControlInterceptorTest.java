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
		
		ShedResponse sr = GCI.before();
		assertFalse(sr.shouldShed);
		assertFalse(heap.hasChecked());
		assertFalse(heap.hasCollected());
		GCI.after(sr);
		for (int i = 2; i <= 63; i++) { // The default sample size is 64. So, we want call before 63 times...
			sr = GCI.before();
			GCI.after(sr);
		}
		sr = GCI.before();
		assertFalse(sr.shouldShed);
		assertTrue(heap.hasChecked());
		assertFalse(heap.hasCollected());
		GCI.after(sr);
		
		heap.resetFlags();
		heap.fillMemory();

		for (int i = 1; i <= 63; i++) { // The default sample size still 64 and now we want call before 63 times!
			sr = GCI.before();
			GCI.after(sr);
		}
		assertFalse(sr.shouldShed);
		assertFalse(heap.hasChecked());
		assertFalse(heap.hasCollected());
		
		sr = GCI.before();
		assertTrue(sr.shouldShed);
		assertTrue(heap.hasChecked());
		assertFalse(heap.hasCollected());
		
		GCI.after(sr);
		Thread.sleep(10);
		assertTrue(sr.shouldShed);
		assertTrue(heap.hasChecked());
		assertTrue(heap.hasCollected());
		
		heap.resetFlags();
		heap.fillMemory();
		
	}

}

class FakeHeap implements IHeap {
	private boolean hasCollected;
	private boolean hasChecked;
	private long alloc;

	public long getHeapUsageSinceLastGC() {
		hasChecked = true;
		return alloc;
	}

	public long collect() {
		hasCollected = true;
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