package com.gcinterceptor.core;

import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class RuntimeEnvironmentTest {

	@BeforeClass
	public static void setUpEnvironmentTest() {
		System.setProperty("jvmtilib", System.getProperty("user.dir") + "/src/main/java/libgc.so");
	}
	
	@AfterClass
	public static void setDownEnvironmentTest() {
		System.clearProperty("jvmtilib");
	}	
	
	@Test
	public void testLibgcCall() {
		RuntimeEnvironment runtime = new RuntimeEnvironment();
		runtime.collect();
	}

	@Test
	public void testRuntimeNullPoolsInit() {
		try {
			RuntimeEnvironment runtime = new RuntimeEnvironment();
			runtime.getTenuredHeapUsage();
			runtime.getYoungHeapUsage();
			runtime.collect();
		} catch (NullPointerException e) {
			fail();
		}
	}
}
