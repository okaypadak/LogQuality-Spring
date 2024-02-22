package dev.padak;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class EncoderCompositeLog extends LoggingEventCompositeJsonEncoder
{
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void encode(ILoggingEvent event, OutputStream outputStream) throws IOException {


        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        try {
            String timestamp = dateFormat.format(new Date(event.getTimeStamp()));
            String threadName = event.getThreadName();
            String level = event.getLevel().toString();
            String loggerName = event.getLoggerName();
            String requestId = String.valueOf(event.getMDCPropertyMap().get("requestId") != null ? event.getMDCPropertyMap().get("requestId") : "");
            String message = event.getFormattedMessage();
            String exception = event.getThrowableProxy() != null ? event.getThrowableProxy().toString() : "";

            Map<String, Object> logMap = new LinkedHashMap<>();
            logMap.put("timestamp", timestamp);
            logMap.put("level", level);
            logMap.put("logId", requestId);
            logMap.put("message", message);
            logMap.put("threadName", threadName);
            logMap.put("loggerName", loggerName);
            logMap.put("exception", exception);


            Map<String, String> fields = new HashMap<>();
            fields.put("app", "test");

            logMap.put("fields",fields);

            String jsonLog = objectMapper.writeValueAsString(logMap)  + "\n";

            outputStream.write(jsonLog.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
