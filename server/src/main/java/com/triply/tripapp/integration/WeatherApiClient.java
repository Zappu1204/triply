package com.triply.tripapp.integration;

import com.triply.tripapp.util.BadRequestException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

@Component
public class WeatherApiClient {

    @Value("${external-apis.weatherapi.api-key:}")
    private String apiKey;

    public String getForecastByLatLon(double latitude, double longitude, int days) throws IOException {
        if (apiKey == null || apiKey.isBlank()) {
            throw new BadRequestException("Thiếu API key WeatherAPI. Vui lòng cấu hình 'external-apis.weatherapi.api-key' trong application.yml");
        }
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            URIBuilder builder = new URIBuilder("https://api.weatherapi.com/v1/forecast.json");
            builder.addParameter("key", apiKey)
                   .addParameter("q", latitude + "," + longitude)
                   .addParameter("days", String.valueOf(days <= 0 ? 3 : days))
                   .addParameter("aqi", "no")
                   .addParameter("alerts", "no");

            HttpGet get = new HttpGet(builder.build());
            try (CloseableHttpResponse response = client.execute(get)) {
                int status = response.getCode();
                String body = response.getEntity() == null ? "" : new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                if (status >= 200 && status < 300) {
                    return body;
                }
                String snippet = body.substring(0, Math.min(body.length(), 500));
                throw new BadRequestException("WeatherAPI trả về lỗi (" + status + "): " + snippet);
            }
        } catch (URISyntaxException e) {
            throw new BadRequestException("WeatherAPI URL lỗi: " + e.getMessage());
        }
    }
}


