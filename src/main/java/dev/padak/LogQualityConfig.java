package dev.padak;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import net.logstash.logback.appender.LogstashTcpSocketAppender;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogQualityConfig {
    private static boolean fileLoggingEnabled = true;

    private static boolean jsonLoggingEnabled = true;
    private static boolean tcpLoggingEnabled = true;
    private static boolean cassandraLoggingEnabled = false;

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
        rootLogger.detachAndStopAllAppenders();

        //CONSOLE
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setName("CONSOLE");

        PatternLayoutEncoder consoleEncoder = new PatternLayoutEncoder();
        consoleEncoder.setPattern("%highlight(%d{yyyy-MM-dd HH:mm:ss.SSS} [%21thread] %-5level %-35logger{36} - %X{requestId} %msg%n%exception{0})");
        consoleEncoder.setContext(loggerContext);
        consoleEncoder.start();

        consoleAppender.setEncoder(consoleEncoder);
        consoleAppender.setContext(loggerContext);
        consoleAppender.start();


        rootLogger.addAppender(consoleAppender);

        //ONLY FILE
        if (fileLoggingEnabled) {

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
            stashAppender.start();

            rootLogger.addAppender(stashAppender);
        }


        //JSON FILE
        if (jsonLoggingEnabled) {
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
            elkAppender.start();

            rootLogger.addAppender(elkAppender);

        }

        if (tcpLoggingEnabled) {
            //LOGSTASH TCP
            LogstashTcpSocketAppender logstashTcpSocketAppender = new LogstashTcpSocketAppender();
            logstashTcpSocketAppender.setName("TCP_LOG");
            logstashTcpSocketAppender.setContext(loggerContext);
            logstashTcpSocketAppender.addDestination("localhost:5000");
            logstashTcpSocketAppender.setEncoder(new EncoderJSON());
            logstashTcpSocketAppender.start();

            rootLogger.addAppender(logstashTcpSocketAppender);
        }


        if (cassandraLoggingEnabled) {
            //CASSANDRA
            CassandraAppender cassandraAppender = new CassandraAppender();
            cassandraAppender.setName("CASSANDRA");
            cassandraAppender.setContext(loggerContext);
            cassandraAppender.start();

            rootLogger.addAppender(cassandraAppender);

        }

        rootLogger.setLevel(Level.INFO);




    }
}