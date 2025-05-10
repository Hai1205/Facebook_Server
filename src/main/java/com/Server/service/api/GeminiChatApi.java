package com.Server.service.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GeminiChatApi {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    public String getGeminiResponse(String prompt) throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(apiUrl + "?key=" + apiKey);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> payload = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                )
        );
        String json = mapper.writeValueAsString(payload);
        StringEntity entity = new StringEntity(json, "UTF-8");
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");

        try (CloseableHttpResponse response = client.execute(httpPost)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

            if (statusCode != 200) {
                throw new RuntimeException("API request failed with status: " + statusCode + ", response: " + responseBody);
            }

            // Parse JSON
            try {
                var jsonNode = mapper.readTree(responseBody);
                if (jsonNode.has("candidates") && !jsonNode.get("candidates").isEmpty()) {
                    return jsonNode.get("candidates").get(0).get("content").get("parts").get(0).get("text").asText();
                } else {
                    throw new RuntimeException("No valid candidates in response: " + responseBody);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse JSON response: " + responseBody, e);
            }
        } finally {
            client.close();
        }
    }
}