package dev.padak;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.net.InetSocketAddress;
import java.util.UUID;

public class CassandraAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    
    private CqlSession session;
    private PreparedStatement preparedStatement;

    private String host;
    private Integer port;
    private String keyspace;
    private String tableName;


    public CassandraAppender(String host, Integer port, String keyspace, String tableName) {
        this.host = host;
        this.port = port;
        this.keyspace = keyspace;
        this.tableName = tableName;
    }

    @Override
    public void start() {
        super.start();

        InetSocketAddress address = new InetSocketAddress(host, port);

        session = CqlSession.builder()
                .addContactPoint(address)
                .withLocalDatacenter("datacenter1")
                .build();

        String insertQuery = String.format("INSERT INTO %s.%s (ID, timestamp, thread, log_level, logger, request_id, log_message, exception) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", keyspace, tableName);
        preparedStatement = session.prepare(insertQuery);
    }


    @Override
    protected void append(ILoggingEvent eventObject) {
        if (session == null) {
            addError("Cassandra session is not available");
            return;
        }

        try {
            session.execute(preparedStatement.bind()
                    .setUuid("ID", UUID.randomUUID())
                    .setInstant("timestamp", eventObject.getInstant())
                    .setString("thread", eventObject.getThreadName())
                    .setString("log_level", eventObject.getLevel().toString())
                    .setString("logger", eventObject.getLoggerName())
                    .setString("logId", eventObject.getMDCPropertyMap().get("requestId"))
                    .setString("log_message", eventObject.getFormattedMessage())
                    .setString("exception", eventObject.getThrowableProxy() != null ? eventObject.getThrowableProxy().getMessage() : ""));
        } catch (Exception e) {
            addError("Error appending log to Cassandra", e);
        }
    }

    @Override
    public void stop() {
        super.stop();

        session.close();

    }

}
