package dev.padak;

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

import java.util.Collection;
import java.util.Objects;

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

    private static final String METRIC_NAME = "system.cpu.usage";
    private static final double MAX_USAGE = 0.50D;


    @Autowired
    MetricsEndpoint metricsEndpoint;

    @Component
    public class LogMetrics {

        @Scheduled(fixedRate = 10000) // 10 seconds
        public void logCPUUsage() {

            Double systemCpuUsage = metricsEndpoint.metric(METRIC_NAME, null)
                    .getMeasurements()
                    .stream()
                    .filter(Objects::nonNull)
                    .findFirst()
                    .map(MetricsEndpoint.Sample::getValue)
                    .filter(Double::isFinite)
                    .orElse(0.0D);

            log.info("CPU {} ", systemCpuUsage);

        }
    }

}