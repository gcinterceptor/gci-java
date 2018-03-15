package com.gcinterceptor.spring;

import com.gcinterceptor.core.GarbageCollectorControlInterceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Spring {@link Configuration} to be exported by the application that would
 * like to use {@link SpringGcInterceptor}.
 *
 * @see SpringGcInterceptor
 */
@Configuration
@ComponentScan
public class SpringGcInterceptorConfiguration {
    @Bean
    public SpringGcInterceptor springGcInterceptor() {
        return new SpringGcInterceptor();
    }

    @Bean
    public GarbageCollectorControlInterceptor gcInterceptor() {
        return new GarbageCollectorControlInterceptor();
    }
}
