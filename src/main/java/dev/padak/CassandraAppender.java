package dev.padak;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;

import java.net.InetSocketAddress;
import java.util.UUID;

public class CassandraAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private CqlSession session;
    private String keyspace;
    private String tableName;
    private PreparedStatement preparedStatement;

    String url = "localhost";
    private int port = 9042;
    InetSocketAddress address = new InetSocketAddress(url, port);


    private String username;
    private String password;

    @Override
    public void start() {
        super.start();

        session = CqlSession.builder()
                .addContactPoint(address)
                .withLocalDatacenter("datacenter1")
                .build();

        keyspace = "logquality";
        tableName = "test";

        String insertQuery = String.format("INSERT INTO %s.%s (ID, timestamp, thread, log_level, logger, request_id, log_message, metrics) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", keyspace, tableName);
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
                    .setString("request_id", eventObject.getMDCPropertyMap().get("requestId"))
                    .setString("log_message", eventObject.getFormattedMessage())
                    .setString("metrics", ""));
        } catch (Exception e) {
            addError("Error appending log to Cassandra", e);
        }
    }

    @Override
    public void stop() {
        super.stop();
        // Logback UnsynchronizedAppenderBase sınıfı, kendi close metodu çağrıldığında bu metod da çağrılır.
    }

}
