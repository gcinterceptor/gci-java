package com.gcinterceptor.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.gcinterceptor.core.RuntimeEnvironment;

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
    public RuntimeEnvironment runtimeEnvironment() {
        return new RuntimeEnvironment();
    }
}
