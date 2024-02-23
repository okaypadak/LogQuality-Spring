package dev.padak;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.logstash.logback.composite.JsonProviders;
import net.logstash.logback.composite.loggingevent.LoggingEventFormattedTimestampJsonProvider;
import net.logstash.logback.composite.loggingevent.MessageJsonProvider;
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class EncoderCompositeMetric extends LoggingEventCompositeJsonEncoder
{
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EncoderCompositeMetric() {
        setProviders(createProviders());
    }

    private JsonProviders<ILoggingEvent> createProviders() {
        LoggingEventFormattedTimestampJsonProvider timestampProvider = new LoggingEventFormattedTimestampJsonProvider();
        timestampProvider.setTimeZone("UTC");
        timestampProvider.setFieldName("@timestamp");
        timestampProvider.setPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");


        JsonProviders<ILoggingEvent> providers = new JsonProviders<>();
        providers.addProvider(timestampProvider);


        return providers;
    }

    @Override
    public void encode(ILoggingEvent event, OutputStream outputStream) throws IOException {


        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        try {
            String message = event.getFormattedMessage();

            Map<String, Object> logMap = new LinkedHashMap<>();
            logMap.put("message", message);


            Map<String, String> fields = new HashMap<>();
            fields.put("app", "metric");

            logMap.put("fields",fields);

            String jsonLog = objectMapper.writeValueAsString(logMap)  + "\n";

            outputStream.write(jsonLog.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
