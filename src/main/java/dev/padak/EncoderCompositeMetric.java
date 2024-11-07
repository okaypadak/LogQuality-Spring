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
    private final String projectName;

    public EncoderCompositeMetric(String projectName) {
        setProviders(createProviders());
        this.projectName = projectName;
    }

    private JsonProviders<ILoggingEvent> createProviders() {
        LoggingEventFormattedTimestampJsonProvider timestampProvider = new LoggingEventFormattedTimestampJsonProvider();
        timestampProvider.setTimeZone("Europe/Istanbul");
        timestampProvider.setFieldName("@timestamp");
        timestampProvider.setPattern("yyyy-MM-dd HH:mm:ss.SSS");

        JsonProviders<ILoggingEvent> providers = new JsonProviders<>();
        providers.addProvider(timestampProvider);


        return providers;
    }

    @Override
    public void encode(ILoggingEvent event, OutputStream outputStream) throws IOException {


        try {
            String message = event.getFormattedMessage();

            Map<String, Object> logMap = new LinkedHashMap<>();
            logMap.put("metrics", message);
            logMap.put("processed", false);

            Map<String, String> fields = new HashMap<>();
            fields.put("app", projectName+"_metrics");

            logMap.put("fields",fields);



            String jsonLog = objectMapper.writeValueAsString(logMap)  + "\n";

            outputStream.write(jsonLog.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
