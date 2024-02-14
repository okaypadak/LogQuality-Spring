package dev.padak;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import net.logstash.logback.appender.LogstashTcpSocketAppender;
import net.logstash.logback.encoder.LogstashEncoder;
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

    public static void configureLogback(LoggerContext loggerContext) {

        ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

        RollingFileAppender<ILoggingEvent> stashAppender = new RollingFileAppender<>();
        stashAppender.setName("STASH");
        stashAppender.setFile("logback/uptLog.log");

        TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
        rollingPolicy.setFileNamePattern("logback/uptLog.%d{yyyy-MM-dd}.log");
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
        stashAppender.start();

        // CONSOLE Appender Configuration
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setName("CONSOLE");

        PatternLayoutEncoder consoleEncoder = new PatternLayoutEncoder();
        consoleEncoder.setPattern("%highlight(%d{yyyy-MM-dd HH:mm:ss.SSS} [%21thread] %-5level %-35logger{36} - %X{requestId} %msg%n%exception{0})");
        consoleEncoder.setContext(loggerContext);
        consoleEncoder.start();

        consoleAppender.setEncoder(consoleEncoder);
        consoleAppender.setContext(loggerContext);
        consoleAppender.start();

        //Cassandra
        CassandraAppender cassandraAppender = new CassandraAppender();
        cassandraAppender.setName("CASSANDRA");
        cassandraAppender.setContext(loggerContext);
        cassandraAppender.start();


        //ELK
        ElasticsearchAppender elasticsearchAppender = new ElasticsearchAppender();
        elasticsearchAppender.setIndexName("logquality");
        elasticsearchAppender.setContext(loggerContext);
        elasticsearchAppender.start();

        // Root Logger Configuration
        rootLogger.detachAndStopAllAppenders();
        rootLogger.addAppender(stashAppender);
        rootLogger.addAppender(consoleAppender);
        rootLogger.addAppender(cassandraAppender);
        rootLogger.addAppender(elasticsearchAppender);
        rootLogger.setLevel(Level.INFO);

    }
}