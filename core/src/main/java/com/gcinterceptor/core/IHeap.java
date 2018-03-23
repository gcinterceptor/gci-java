package com.gcinterceptor.core;

public interface IHeap {
	
	long getHeapUsageSinceLastGC();

	long collect();
	
}
