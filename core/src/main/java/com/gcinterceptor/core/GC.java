package com.gcinterceptor.core;

// TODO: Move this functionality to the Runtime class.
public class GC {
	public static native void force();

	static {
		System.load(System.getProperty("jvmtilib"));

	}
}
