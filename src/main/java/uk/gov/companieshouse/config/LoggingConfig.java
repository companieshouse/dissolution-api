package uk.gov.companieshouse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.interceptor.LoggingInterceptor;

@Configuration
public class LoggingConfig implements WebMvcConfigurer {

    @Value("${loggingInterceptor}")
    private LoggingInterceptor loggingInterceptor;

    @Bean
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor).excludePathPatterns("/dissolution-request/healthcheck");
    }
}
