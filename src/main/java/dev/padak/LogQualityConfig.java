package dev.padak;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.appender.LogstashTcpSocketAppender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Map;
@Slf4j
public class LogQualityConfig {

    @Value("${logquality.project_name}")
    private String projectName;
    @Value("${logquality.file}")
    private Boolean file;

    @Value("${logquality.logstash.connection}")
    private Boolean logstash;

    @Value("${logquality.logstash.host}")
    private String logstash_host;

    @Value("${logquality.logstash.port}")
    private String logstash_port;

    @Value("${logquality.filebeat}")
    private Boolean filebeat;


    public void configureLogback(LoggerContext loggerContext) {

        ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.INFO);

        ch.qos.logback.classic.Logger rootLoggerDebug = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        rootLoggerDebug.setLevel(Level.INFO);

        rootLogger.detachAndStopAllAppenders();
        rootLoggerDebug.detachAndStopAllAppenders();

        //CONSOLE
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setName("CONSOLE");

        PatternLayoutEncoder consoleEncoder = new PatternLayoutEncoder();
        consoleEncoder.setPattern("%highlight(%d{yyyy-MM-dd HH:mm:ss.SSS} [%21thread] %-5level %-35logger{36} - %X{requestId} %msg%n%exception{0})");
        consoleEncoder.setContext(loggerContext);
        consoleEncoder.start();

        consoleAppender.setEncoder(consoleEncoder);
        consoleAppender.setContext(loggerContext);
        consoleAppender.addFilter(new FilterLog());
        consoleAppender.start();


        rootLogger.addAppender(consoleAppender);


        if (file) {
            //ONLY FILE
            RollingFileAppender<ILoggingEvent> stashAppender = new RollingFileAppender<>();
            stashAppender.setName("FILE");
            stashAppender.setFile("logback/quality.log");

            TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
            rollingPolicy.setFileNamePattern("logback/quality.%d{yyyy-MM-dd}.log");
            rollingPolicy.setParent(stashAppender);
            rollingPolicy.setContext(loggerContext);
            rollingPolicy.start();

            stashAppender.setRollingPolicy(rollingPolicy);

            PatternLayoutEncoder patternLayoutEncoder = new PatternLayoutEncoder();
            patternLayoutEncoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - %X{requestId} %msg%n%exception{full}");
            patternLayoutEncoder.setContext(loggerContext);
            patternLayoutEncoder.start();

            stashAppender.setEncoder(patternLayoutEncoder);
            stashAppender.setContext(loggerContext);
            stashAppender.addFilter(new FilterLog());
            stashAppender.start();

            rootLoggerDebug.addAppender(stashAppender);
        }

        if (filebeat) {
            //JSON FILE
            RollingFileAppender<ILoggingEvent> elkAppender = new RollingFileAppender<>();
            elkAppender.setName("FILEBAEAT_JSON");
            elkAppender.setFile("logback/filebeat.json");

            TimeBasedRollingPolicy<ILoggingEvent> ElkRollingPolicy = new TimeBasedRollingPolicy<>();
            ElkRollingPolicy.setFileNamePattern("logback/filebeat.%d{yyyy-MM-dd}.json");
            ElkRollingPolicy.setParent(elkAppender);
            ElkRollingPolicy.setContext(loggerContext);
            ElkRollingPolicy.start();

            elkAppender.setRollingPolicy(ElkRollingPolicy);

            elkAppender.setEncoder(new EncoderJSON());
            elkAppender.setContext(loggerContext);
            elkAppender.addFilter(new FilterLog());
            elkAppender.start();

            rootLoggerDebug.addAppender(elkAppender);

        }

        if (logstash) {
            //LOGSTASH TCP

            try {
                LogstashTcpSocketAppender LogsTcpSocketAppender = new LogstashTcpSocketAppender();
                LogsTcpSocketAppender.setName("TCP_LOG");
                LogsTcpSocketAppender.setContext(loggerContext);
                LogsTcpSocketAppender.addDestination(logstash_host+":"+logstash_port);
                EncoderCompositeLog encoderCompositeLog = new EncoderCompositeLog(projectName);
                encoderCompositeLog.setContext(loggerContext);
                LogsTcpSocketAppender.setEncoder(encoderCompositeLog);
                LogsTcpSocketAppender.addFilter(new FilterLog());
                LogsTcpSocketAppender.start();

                rootLoggerDebug.addAppender(LogsTcpSocketAppender);

                LogstashTcpSocketAppender MetricTcpSocketAppender = new LogstashTcpSocketAppender();
                MetricTcpSocketAppender.setName("TCP_METRIC");
                MetricTcpSocketAppender.setContext(loggerContext);
                MetricTcpSocketAppender.addDestination(logstash_host+":"+logstash_port);
                EncoderCompositeMetric encoderCompositeMetric = new EncoderCompositeMetric(projectName);
                encoderCompositeLog.setContext(loggerContext);
                MetricTcpSocketAppender.setEncoder(encoderCompositeMetric);
                MetricTcpSocketAppender.addFilter(new FilterMetric());
                MetricTcpSocketAppender.start();

                rootLoggerDebug.addAppender(MetricTcpSocketAppender);

            } catch (Exception e) {
                // Bağlantı hatası durumunda loglama
                log.error("LogstashTcpSocketAppender başlatılırken bir hata oluştu: " + e.getMessage());
            }



        }

    }
}