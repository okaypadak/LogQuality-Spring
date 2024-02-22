package dev.padak;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Configuration
@EnableScheduling
@Slf4j
public class LogQualityDepency {

    @Bean
    public FilterRegistrationBean<HttpFilter> loggingFilter() {
        FilterRegistrationBean<HttpFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new HttpFilter());
        registrationBean.addUrlPatterns("/*");

        return registrationBean;
    }

    @Bean
    public LogAspect logAspect() {
        return new LogAspect();
    }

    @Bean
    public LogQualityConfig logQualityConfig() { return new LogQualityConfig();}


    @Component
    public class LogMetrics {

        @Autowired
        MetricsEndpoint metricsEndpoint;

        @Scheduled(fixedRate = 10000) // 10 seconds
        public void logCPUUsage() {
            Set<String> metricNames = metricsEndpoint.listNames().getNames();
            Map<String, Object> metricMap = new HashMap<>();

            for (String metricName : metricNames) {
                Double metricValue = metricsEndpoint.metric(metricName, null)
                        .getMeasurements()
                        .stream()
                        .filter(Objects::nonNull)
                        .findFirst()
                        .map(MetricsEndpoint.Sample::getValue)
                        .filter(Double::isFinite)
                        .orElse(0.0D);
                metricMap.put(metricName, metricValue);
            }

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                log.info("metrics: {}", objectMapper.writeValueAsString(metricMap));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }

}