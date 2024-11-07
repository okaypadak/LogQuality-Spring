package dev.padak;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

public class PodMetricsService {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${KUBERNETES_NAMESPACE:default}")
    private String namespace;

    @Value("${HOSTNAME}")
    private String podName;

    public Map<String, Map<String, Double>> getOwnPodMetrics() {
        String url = "https://kubernetes.default.svc/apis/metrics.k8s.io/v1beta1/namespaces/" + namespace + "/pods/" + podName;
        String response = restTemplate.getForObject(url, String.class);

        JSONObject jsonResponse = new JSONObject(response);
        JSONArray containers = jsonResponse.getJSONArray("containers");

        Map<String, Map<String, Double>> podMetricsMap = new HashMap<>();

        for (int i = 0; i < containers.length(); i++) {
            JSONObject container = containers.getJSONObject(i);
            String containerName = container.getString("name");

            if (containerName.equals(podName)) {

                String cpuUsage = container.getJSONObject("usage").getString("cpu");
                String memoryUsage = container.getJSONObject("usage").getString("memory");

                Map<String, Double> containerMetrics = new HashMap<>();
                containerMetrics.put(podName+"_CPU", Double.valueOf(cpuUsage));
                containerMetrics.put(podName+"_Memory", Double.valueOf(memoryUsage));

                podMetricsMap.put(podName, containerMetrics);
                break;
            }
        }

        return podMetricsMap;
    }
}

