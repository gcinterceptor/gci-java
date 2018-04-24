package com.gcinterceptor.spring;

import com.gcinterceptor.core.GarbageCollectorControlInterceptor;
import com.gcinterceptor.core.ShedResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.ModelAndView;

/**
 * Spring interceptor that uses {@link GarbageCollectorControlInterceptor} to
 * control garbage collection and decide whether to shed requests.
 *
 * @see GarbageCollectorControlInterceptor
 */
@Configuration
public class SpringGcInterceptor extends HandlerInterceptorAdapter {

	private static final String SHED_RESPONSE_OBJ_NAME = "sr";
	private static final String GCI_HEADERS_NAME = "GCI";

	@Autowired
	private GarbageCollectorControlInterceptor gci;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		ShedResponse shedResponse = gci.before(request.getHeader(GCI_HEADERS_NAME));
		if (shedResponse.shouldShed) {
			response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
			response.setContentLength(0);
		}
		request.getSession().setAttribute(SHED_RESPONSE_OBJ_NAME, shedResponse);
		return !shedResponse.shouldShed;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			@Nullable ModelAndView modelAndView) throws Exception {
		ShedResponse shedResponse = (ShedResponse) request.getSession().getAttribute(SHED_RESPONSE_OBJ_NAME);
		gci.after(shedResponse);
	}
}
