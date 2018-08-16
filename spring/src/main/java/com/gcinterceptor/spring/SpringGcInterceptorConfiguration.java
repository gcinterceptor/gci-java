package com.gcinterceptor.spring;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

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
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public SpringGcInterceptor springGcInterceptor() {
        return new SpringGcInterceptor();
    }
}
