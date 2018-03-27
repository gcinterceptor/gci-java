package com.gcinterceptor.core;

import static org.junit.Assert.*;

import org.junit.Test;

public class HeapTest {

	@Test
	public void testHeap() {
		Heap heap = new Heap();
		assertEquals(0, heap.getHeapUsageSinceLastGC());
		int n = 5;
		long arrayCost = 4000016; // 4 bytes * 10^6 + 16 bytes (array default cost) = 4,000016MB.
		for (int i = 1; i <= n; i++) {
			int[] array = new int[1000000];
			assertEquals(i * arrayCost, heap.getHeapUsageSinceLastGC());
		}
		assertEquals(n * arrayCost, heap.collect());

		long alloc = heap.getHeapUsageSinceLastGC();
		assertTrue(alloc < n * arrayCost); // Ensure the heap was cleaned.
		for (int i = 1; i <= n; i++) {
			int[] array = new int[1000000];
			assertEquals(i * arrayCost + alloc, heap.getHeapUsageSinceLastGC());
		}
		assertEquals(n * arrayCost + alloc, heap.collect());
		
	}

}
