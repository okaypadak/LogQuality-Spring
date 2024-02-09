package dev.padak;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogQualityConfig {

    @Bean
    public LogAspect logAspect() {
        return new LogAspect();
    }

    @Bean
    public FilterRegistrationBean<HttpFilter> loggingFilter() {
        FilterRegistrationBean<HttpFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new HttpFilter());
        registrationBean.addUrlPatterns("/*");

        return registrationBean;
    }
}