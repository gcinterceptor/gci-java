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
	private static final String EMPTY_HEADER = "";
	private static final String CH_HEADER = "ch";
	private static final String GCI_HEADERS_NAME = "gci";

	@Autowired
	private RuntimeEnvironment runtime;

	public SpringGcInterceptor() {
		runtime = new RuntimeEnvironment();
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		System.out.println("preHandle");

		String gciHeader = request.getHeader(GCI_HEADERS_NAME);
		if (gciHeader == null) {
			System.out.println("NULL");

			return true;
		}

		switch (gciHeader) {
		case EMPTY_HEADER:
			System.out.println("EMPTY");

			return true;

		case CH_HEADER:
			System.out.println("CH HEADER");

			String heapUsageString = Long.valueOf(runtime.getYoungHeapUsage()) + "|"
					+ Long.valueOf(runtime.getTenuredHeapUsage());
			response.getWriter().write(heapUsageString);
			response.getWriter().flush();
			response.getWriter().close();
			response.setStatus(HttpStatus.OK.value());
			return false;

		default:
			System.out.println("DEFAULT");
	
			runtime.collect();
			response.setStatus(HttpStatus.OK.value());
			return false;
		}
	}
}
