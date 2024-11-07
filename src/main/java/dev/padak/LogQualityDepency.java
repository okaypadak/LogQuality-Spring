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

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
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

        @Autowired
        PodMetricsService podMetricsService;

        @Scheduled(fixedRate = 10000)
        public void metrics() {

            Set<String> metricNames = metricsEndpoint.listNames().getNames();
            Map<String, Object> metricMap = new HashMap<>();

            Map<String, Double> cpuMetrics = new HashMap<>();
            Map<String, Double> memoryMetrics = new HashMap<>();
            Map<String, Double> diskMetrics = new HashMap<>();
            Map<String, Double> gcMetrics = new HashMap<>();
            Map<String, Double> threadMetrics = new HashMap<>();
            Map<String, Double> appStartMetrics = new HashMap<>();

            for (String metricName : metricNames) {
                Double metricValue = metricsEndpoint.metric(metricName, null)
                        .getMeasurements()
                        .stream()
                        .filter(Objects::nonNull)
                        .findFirst()
                        .map(MetricsEndpoint.Sample::getValue)
                        .filter(Double::isFinite)
                        .orElse(0.0D);

                // CPU Metrikleri
                if (metricName.equals("system.cpu.usage") || metricName.equals("process.cpu.usage") || metricName.equals("system.load.average.1m")) {
                    cpuMetrics.put(metricName, metricValue);
                }
                // Bellek Metrikleri
                else if (metricName.equals("jvm.memory.used") || metricName.equals("jvm.memory.max") || metricName.equals("jvm.memory.committed")) {
                    memoryMetrics.put(metricName, metricValue);
                }
                // Disk Metrikleri
                else if (metricName.equals("disk.total") || metricName.equals("disk.free")) {
                    diskMetrics.put(metricName, metricValue);
                }
                // Çöp Toplama (GC) Metrikleri
                else if (metricName.equals("jvm.gc.pause") || metricName.equals("jvm.gc.overhead") || metricName.equals("jvm.gc.memory.allocated") || metricName.equals("jvm.gc.memory.promoted")) {
                    gcMetrics.put(metricName, metricValue);
                }
                // İş Parçacığı (Thread) Metrikleri
                else if (metricName.equals("jvm.threads.daemon") || metricName.equals("jvm.threads.live") || metricName.equals("jvm.threads.peak")) {
                    threadMetrics.put(metricName, metricValue);
                }
                // Uygulama Başlama Süresi
                else if (metricName.equals("application.started.time") || metricName.equals("application.ready.time")) {
                    appStartMetrics.put(metricName, metricValue);
                }

            }

            Map<String, Map<String, Double>> podMetrics = podMetricsService.getOwnPodMetrics();


            Map<String, Map<String, Double>> groupedMetrics = new HashMap<>();
            groupedMetrics.put("cpuMetrics", cpuMetrics);
            groupedMetrics.put("memoryMetrics", memoryMetrics);
            groupedMetrics.put("diskMetrics", diskMetrics);
            groupedMetrics.put("gcMetrics", gcMetrics);
            groupedMetrics.put("threadMetrics", threadMetrics);
            groupedMetrics.put("appStartMetrics", appStartMetrics);
            groupedMetrics.put("podCPU", podMetrics.get("podCPU"));
            groupedMetrics.put("podMemory", podMetrics.get("podMemory"));


            try {
                ObjectMapper objectMapper = new ObjectMapper();
                log.info("system_metrics: {}", objectMapper.writeValueAsString(groupedMetrics));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        @Autowired
        MeterRegistry meterRegistry;

        @Scheduled(fixedRate = 10000*6)
        public void http() {

            Map<String, Object> metricMap = new HashMap<>();
            MetricsEndpoint.MetricDescriptor httpMetric = metricsEndpoint.metric("http.server.requests", null);
            metricMap.put("http.server.requests", httpMetric);

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                log.info("http metrics: {}", objectMapper.writeValueAsString(metricMap));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }

}