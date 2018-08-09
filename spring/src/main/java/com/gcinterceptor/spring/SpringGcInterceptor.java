package com.gcinterceptor.spring;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.gcinterceptor.core.RuntimeEnvironment;

/**
 * Spring interceptor that uses {@link GarbageCollectorControlInterceptor} to
 * control garbage collection and decide whether to shed requests.
 *
 * @see GarbageCollectorControlInterceptor
 */
@Configuration
public class SpringGcInterceptor extends HandlerInterceptorAdapter {

	private static final String EMPTY_HEADERS_NAME = "";
	private static final String CH_HEADERS_NAME = "ch";
	private static final String GCI_HEADERS_NAME = "gci";

	@Autowired
	private RuntimeEnvironment runtime;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		String gciHeader = request.getHeader(GCI_HEADERS_NAME);
		if (gciHeader == null) {
			return true;
		}
		
		switch (gciHeader) {
		case EMPTY_HEADERS_NAME:
			return true;

		case CH_HEADERS_NAME:
			String heapUsageString = Long.valueOf(runtime.getYoungHeapUsageSinceLastGC()) + "|"
					+ Long.valueOf(runtime.getTenuredHeapUsageSinceLastGC());
			response.getWriter().write(heapUsageString);
			response.getWriter().flush();
			response.getWriter().close();
			response.setStatus(HttpStatus.OK.value());
			return false;

		default:
			runtime.collect();
			response.setStatus(HttpStatus.OK.value());
			return false;
		}
	}
}
