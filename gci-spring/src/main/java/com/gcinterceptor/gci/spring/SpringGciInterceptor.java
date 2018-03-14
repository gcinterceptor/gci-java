package com.gcinterceptor.gci.spring;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.gcinterceptor.gci.GarbageCollectorControlInterceptor;
import com.gcinterceptor.gci.ShedResponse;

/**
 * Spring interceptor that uses {@link GarbageCollectorControlInterceptor} to control garbage
 * collection and decide whether to shed requests.
 *
 * @author danielfireman
 * @see GarbageCollectorControlInterceptor
 */
@Configuration
public class SpringGciInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private GarbageCollectorControlInterceptor gci;

    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object o)
            throws Exception {
        ShedResponse shedResponse = gci.before();
        if (shedResponse.shouldShed) {
            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
            response.setContentLength(0);
        }
        gci.after(shedResponse);
        return !shedResponse.shouldShed;
    }
}
