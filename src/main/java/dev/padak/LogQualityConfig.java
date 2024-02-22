package dev.padak;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import net.logstash.logback.appender.LogstashTcpSocketAppender;
import org.springframework.beans.factory.annotation.Value;

public class LogQualityConfig {

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

    @Value("${logquality.cassandra.connection}")
    private Boolean cassandra;

    @Value("${logquality.cassandra.host}")
    private String host;

    @Value("${logquality.cassandra.port}")
    private Integer port;

    @Value("${logquality.cassandra.keyspace}")
    private String keyspace;

    @Value("${logquality.cassandra.tableName}")
    private String tableName;


    public void configureLogback(LoggerContext loggerContext) {


        loggerContext.getLogger("ROOT").detachAndStopAllAppenders();

        ch.qos.logback.classic.Logger rootLoggerDebug = loggerContext.getLogger("ROOT");
        rootLoggerDebug.setLevel(Level.DEBUG);

        ch.qos.logback.classic.Logger rootLoggerInfo = loggerContext.getLogger("ROOT");
        rootLoggerInfo.setLevel(Level.INFO);

        FilterLog filterLog = new FilterLog();
        filterLog.start();

        //CONSOLE
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setName("CONSOLE");

        PatternLayoutEncoder consoleEncoder = new PatternLayoutEncoder();
        consoleEncoder.setPattern("%highlight(%d{yyyy-MM-dd HH:mm:ss.SSS} [%21thread] %-5level %-35logger{36} - %X{requestId} %msg%n%exception{0})");
        consoleEncoder.setContext(loggerContext);
        consoleEncoder.start();
        consoleAppender.addFilter(filterLog);
        consoleAppender.setEncoder(consoleEncoder);
        consoleAppender.setContext(loggerContext);
        consoleAppender.start();


        rootLoggerInfo.addAppender(consoleAppender);



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
            stashAppender.start();
            stashAppender.addFilter(filterLog);
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
            elkAppender.start();
            elkAppender.addFilter(filterLog);
            rootLoggerDebug.addAppender(elkAppender);

        }

        if (logstash) {
            try {
                //LOGSTASH LOG
                LogstashTcpSocketAppender logstashTcpLogSocketAppender = new LogstashTcpSocketAppender();
                logstashTcpLogSocketAppender.setName("TCP_LOG");
                logstashTcpLogSocketAppender.setContext(loggerContext);
                logstashTcpLogSocketAppender.addDestination(logstash_host + ":" + logstash_port);
                logstashTcpLogSocketAppender.addFilter(filterLog);
                EncoderCompositeLog encoderCompositeLog = new EncoderCompositeLog();
                encoderCompositeLog.setContext(loggerContext);
                encoderCompositeLog.start();
                logstashTcpLogSocketAppender.setEncoder(encoderCompositeLog);
                logstashTcpLogSocketAppender.start();

                rootLoggerDebug.addAppender(logstashTcpLogSocketAppender);

                //LOGSTASH METRIC
                FilterMetric filterMetric = new FilterMetric();
                filterMetric.start();

                LogstashTcpSocketAppender logstashTcpMetricsSocketAppender = new LogstashTcpSocketAppender();
                logstashTcpMetricsSocketAppender.setName("TCP_LOG");
                logstashTcpMetricsSocketAppender.setContext(loggerContext);
                logstashTcpMetricsSocketAppender.addDestination(logstash_host + ":" + logstash_port);
                logstashTcpMetricsSocketAppender.addFilter(filterLog);
                EncoderCompositeMetric encoderCompositeMetric = new EncoderCompositeMetric();
                encoderCompositeMetric.setContext(loggerContext);
                encoderCompositeMetric.start();
                logstashTcpMetricsSocketAppender.setEncoder(encoderCompositeMetric);
                logstashTcpMetricsSocketAppender.start();

                rootLoggerDebug.addAppender(logstashTcpMetricsSocketAppender);

            } catch (Exception e) {
                rootLoggerDebug.error("Elasticsearch hata!");
            }
        }


        if (cassandra) {
            //CASSANDRA
            CassandraAppender cassandraAppender = new CassandraAppender(host, port, keyspace, tableName);
            cassandraAppender.setName("CASSANDRA");
            cassandraAppender.setContext(loggerContext);
            cassandraAppender.start();
            cassandraAppender.addFilter(filterLog);
            rootLoggerDebug.addAppender(cassandraAppender);

        }






    }
}