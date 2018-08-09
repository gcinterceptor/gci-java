package com.gcinterceptor.spring;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.gcinterceptor.core.GarbageCollectorControlInterceptor;

/**
 * Spring interceptor that uses {@link GarbageCollectorControlInterceptor} to
 * control garbage collection and decide whether to shed requests.
 *
 * @see GarbageCollectorControlInterceptor
 */
@Configuration
public class SpringGcInterceptor extends HandlerInterceptorAdapter {

	private static final String GCI_HEADERS_NAME = "GCI";

	@Autowired
	private GarbageCollectorControlInterceptor gci;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		
		switch (request.getHeader(GCI_HEADERS_NAME)) {
		case "":
			return true;

		case "ch":
			response.getWriter().write((int) gci.getHeapUsageSinceLastGC());
			response.getWriter().flush();
			response.getWriter().close();
			response.setStatus(HttpStatus.OK.value());
			return false;
			
		default:
			response.setStatus(HttpStatus.OK.value());
			gci.collect();
			return false;
		}
		
	}

}
