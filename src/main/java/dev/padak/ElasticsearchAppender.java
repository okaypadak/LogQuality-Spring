package dev.padak;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.client.Node;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.internal.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ElasticsearchAppender extends AppenderBase<ILoggingEvent> {

    private Client client;
    private String indexName;

    private RestClient restClient;
    private ObjectMapper mapper;

    @Override
    public void start() {
        super.start();



        restClient = RestClient.builder(
                new HttpHost("localhost", 9200)
        ).build();

        mapper = new ObjectMapper();
    }

    @Override
    protected void append(ILoggingEvent event) {
        try {
            Map<String, Object> logMessage = createLogMessage(event);
            String jsonMsg = mapper.writeValueAsString(logMessage);


            Request request = new Request("POST", "/logquatiy/");
            request.setJsonEntity(jsonMsg);
            Response response = restClient.performRequest(request);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                // Başarıyla gönderildi
            } else {
                // Hata durumu
                addError("Elasticsearch'e veri gönderilirken hata oluştu. HTTP status code: " + statusCode);
            }


        } catch (Exception e) {
            addError("Failed to send message to Elasticsearch", e);
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (client != null) {
            try {
                restClient.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Map<String, Object> createLogMessage(ILoggingEvent event) {
        Map<String, Object> logMessage = new HashMap<>();
        logMessage.put("timestamp", event.getTimeStamp());
        logMessage.put("level", event.getLevel().toString());
        logMessage.put("logger", event.getLoggerName());
        logMessage.put("thread", event.getThreadName());
        logMessage.put("message", event.getFormattedMessage());
        // Add more fields as needed
        return logMessage;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    // Add other setter methods for client configuration, etc.
}
