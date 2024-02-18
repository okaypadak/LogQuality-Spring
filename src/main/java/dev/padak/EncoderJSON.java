package dev.padak;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.logstash.logback.encoder.LogstashEncoder;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class EncoderJSON extends LogstashEncoder {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public byte[] encode(ILoggingEvent event) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        try {
            String timestamp = dateFormat.format(new Date(event.getTimeStamp()));
            String threadName = event.getThreadName();
            String level = event.getLevel().toString();
            String loggerName = event.getLoggerName();
            String requestId = String.valueOf(event.getMDCPropertyMap().get("requestId") != null ? event.getMDCPropertyMap().get("requestId") : "");
            String message = event.getFormattedMessage();
            //String exception = event.getThrowableProxy() != null ? event.getThrowableProxy().toString() : "";

            Map<String, String> exceptionMap = new HashMap<>();

            exceptionMap.put("class",event.getThrowableProxy().getClassName());
            exceptionMap.put("message",event.getThrowableProxy().getMessage());
            exceptionMap.put("type",event.getThrowableProxy() != null ? event.getThrowableProxy().toString() : "");

            List<Map<String, String>> stackTraceList = new ArrayList<>();


            for (StackTraceElementProxy stackTraceElement : event.getThrowableProxy().getStackTraceElementProxyArray()) {
                Map<String, String> stackTraceElementMap = new HashMap<>();
                stackTraceElementMap.put("class",stackTraceElement.getStackTraceElement().getClassName());
                stackTraceElementMap.put("method",stackTraceElement.getStackTraceElement().getMethodName());
                stackTraceElementMap.put("file",stackTraceElement.getStackTraceElement().getFileName());
                stackTraceElementMap.put("line", String.valueOf(stackTraceElement.getStackTraceElement().getLineNumber()));
                stackTraceList.add(stackTraceElementMap);
            }

            exceptionMap.put("stack_trace", objectMapper.writeValueAsString(stackTraceList));



            Map<String, Object> logMap = new LinkedHashMap<>();
            logMap.put("timestamp", timestamp);
            logMap.put("level", level);
            logMap.put("logId", requestId);
            logMap.put("message", message);
            logMap.put("threadName", threadName);
            logMap.put("loggerName", loggerName);
            logMap.put("exception", objectMapper.writeValueAsString(exceptionMap));

            Map<String, String> fields = new HashMap<>();
            fields.put("app", "test");
            logMap.put("fields",fields);

            String jsonLog = objectMapper.writeValueAsString(logMap)  + "\n";

            return jsonLog.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
}
