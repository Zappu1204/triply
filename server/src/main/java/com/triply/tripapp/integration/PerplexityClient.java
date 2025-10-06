package com.triply.tripapp.integration;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.triply.tripapp.util.BadRequestException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class PerplexityClient {

    @Value("${external-apis.perplexity.api-key:}")
    private String apiKey;

    public String chatCompletionsJson(String prompt, String jsonSchema) throws IOException {
        if (apiKey == null || apiKey.isBlank()) {
            throw new BadRequestException("Thiếu API key Perplexity. Vui lòng cấu hình 'external-apis.perplexity.api-key' trong application.yml");
        }
        String url = "https://api.perplexity.ai/chat/completions";
        HttpPost post = new HttpPost(url);
        post.addHeader("Content-Type", "application/json");
        post.addHeader("Accept", "application/json");
        post.addHeader("Authorization", "Bearer " + apiKey);

        String payload = "{\n" +
                "  \"model\": \"sonar-pro\",\n" +
                "  \"messages\": [{\n" +
                "    \"role\": \"user\",\n" +
                "    \"content\": " + quote(prompt) + "\n" +
                "  }],\n" +
                "  \"response_format\": {\n" +
                "    \"type\": \"json_schema\",\n" +
                "    \"json_schema\": {\n" +
                "      \"schema\": " + jsonSchema + "\n" +
                "    }\n" +
                "  }\n" +
                "}";
        post.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(post)) {
            int status = response.getCode();
            HttpEntity entity = response.getEntity();
            String body = entity != null ? new String(entity.getContent().readAllBytes(), StandardCharsets.UTF_8) : "";
            if (status >= 200 && status < 300) {
                return body;
            }
            String snippet = body == null ? "" : body.substring(0, Math.min(body.length(), 500));
            throw new BadRequestException("Perplexity API trả về lỗi (" + status + "): " + snippet);
        }
    }

    private String quote(String text) {
        if (text == null) return "\"\"";
        String escaped = text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
        return "\"" + escaped + "\"";
    }
}



