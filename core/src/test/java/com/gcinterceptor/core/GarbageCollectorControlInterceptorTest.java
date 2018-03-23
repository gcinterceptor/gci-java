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
		
		heap.reset();
		heap.setAlloc(Long.MAX_VALUE);

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
		
	}

}

class FakeHeap implements IHeap {
	private boolean hasCollected;
	private boolean hasChecked;
	private long alloc;

	FakeHeap() {
		hasCollected = false;
		hasChecked = false;
		alloc = Long.MIN_VALUE;
	}

	public long getHeapUsageSinceLastGC() {
		hasChecked = true;
		return getAlloc();
	}

	public long collect() {
		hasCollected = true;
		return getAlloc();
	}

	void reset() {
		hasChecked = false;
		hasCollected = false;
	}

	boolean hasChecked() {
		return hasChecked;
	}
	
	boolean hasCollected() {
		return hasCollected;
	}
	
	void setAlloc(long newAlloc) {
		alloc = newAlloc;
	}
	
	long getAlloc() {
		return alloc;
	}
	
}