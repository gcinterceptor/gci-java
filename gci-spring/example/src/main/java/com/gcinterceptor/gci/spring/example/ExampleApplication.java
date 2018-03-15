package com.gcinterceptor.gci.spring.example;

import com.gcinterceptor.gci.spring.SpringGciInterceptor;
import com.gcinterceptor.gci.spring.SpringGciInterceptorConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
@Import(SpringGciInterceptorConfiguration.class)
public class ExampleApplication {
	public static void main(String[] args) {
		SpringApplication.run(ExampleApplication.class, args);
	}

	@Bean
	public WebMvcConfigurerAdapter adapter() {
		return new WebMvcConfigurerAdapter() {
			@Autowired
			SpringGciInterceptor gciInterceptor;

			@Override
			public void addInterceptors(InterceptorRegistry registry) {
				registry.addInterceptor(gciInterceptor);
			}
		};
	}

	@RestController
	public static class HelloController {
		@RequestMapping("/")
		public String index() throws InterruptedException {
			return "Hello Garbage Collector Control Interceptor!";
		}
	}
}
