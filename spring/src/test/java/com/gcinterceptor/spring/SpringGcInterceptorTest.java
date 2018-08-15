package com.gcinterceptor.spring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Unit test for simple SpringGciInterceptor.
 */
 public class SpringGcInterceptorTest {
	private static final String CH_HEADERS_NAME = "ch";
	private static final String GCI_HEADERS_NAME = "gci";
	
	private SpringGcInterceptor springGcInterceptor;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	@BeforeClass
	public static void setUpEnvironmentTest() {
		System.setProperty("jvmtilib", System.getProperty("user.dir") + "/src/main/java/libgc.so");
	}

	@AfterClass
	public static void setDownEnvironmentTest() {
		System.clearProperty("jvmtilib");
	}

	@Before 
	public void setUp() {
		springGcInterceptor = new SpringGcInterceptor();
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}

	@Test
	public void testNullGCIHeader() throws Exception {
		springGcInterceptor.preHandle(request, response, null);
		assertEquals(0, response.getContentAsString().length());
	}

	@Test
	public void testCHHeader() throws Exception {
		request.addHeader(GCI_HEADERS_NAME, CH_HEADERS_NAME);
		springGcInterceptor.preHandle(request, response, null);
		assertTrue(0 != response.getContentAsString().length());
	}
}
