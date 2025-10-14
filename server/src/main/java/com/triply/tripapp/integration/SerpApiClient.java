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
public class SerpApiClient {

    @Value("${external-apis.serpapi.api-key:}")
    private String apiKey;

    public String searchGoogleFlights(String departureId,
                                      String arrivalId,
                                      String airlineId,
                                      String outboundDate,
                                      String returnDate,
                                      String currency,
                                      Integer maxPrice,
                                      String hl,
                                      String gl) throws IOException {
        if (apiKey == null || apiKey.isBlank()) {
            throw new BadRequestException("Thiếu API key SerpAPI. Vui lòng cấu hình 'external-apis.serpapi.api-key' trong application.yml");
        }

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            URIBuilder builder = new URIBuilder("https://serpapi.com/search");
            builder.addParameter("api_key", apiKey)
                   .addParameter("engine", "google_flights")
                   .addParameter("hl", hl == null ? "vi" : hl)
                   .addParameter("gl", gl == null ? "vn" : gl)
                   .addParameter("departure_id", departureId)
                   .addParameter("arrival_id", arrivalId)
                   .addParameter("outbound_date", outboundDate)
                   .addParameter("return_date", returnDate)
                   .addParameter("currency", currency == null ? "VND" : currency);

            if (airlineId != null && !airlineId.isBlank()) {
                builder.addParameter("include_airlines", airlineId);
            }
            if (maxPrice != null && maxPrice > 0) {
                builder.addParameter("max_price", String.valueOf(maxPrice));
            }

            HttpGet get = new HttpGet(builder.build());
            try (CloseableHttpResponse response = client.execute(get)) {
                int status = response.getCode();
                String body = response.getEntity() == null ? "" : new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                if (status >= 200 && status < 300) {
                    return body;
                }
                String snippet = body.substring(0, Math.min(body.length(), 500));
                throw new BadRequestException("SerpAPI trả về lỗi (" + status + "): " + snippet);
            }
        } catch (URISyntaxException e) {
            throw new BadRequestException("SerpAPI URL lỗi: " + e.getMessage());
        }
    }

    public String searchGoogleHotels(String query,
                                     String checkInDate,
                                     String checkOutDate,
                                     Integer adults,
                                     Integer children,
                                     String currency,
                                     Integer maxPrice,
                                     String sortBy,
                                     String hl,
                                     String gl) throws IOException {
        if (apiKey == null || apiKey.isBlank()) {
            throw new BadRequestException("Thiếu API key SerpAPI. Vui lòng cấu hình 'external-apis.serpapi.api-key' trong application.yml");
        }
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            URIBuilder builder = new URIBuilder("https://serpapi.com/search.json");
            builder.addParameter("api_key", apiKey)
                    .addParameter("engine", "google_hotels")
                    .addParameter("q", query)
                    .addParameter("gl", gl == null ? "vn" : gl)
                    .addParameter("hl", hl == null ? "vi" : hl)
                    .addParameter("currency", currency == null ? "VND" : currency)
                    .addParameter("check_in_date", checkInDate)
                    .addParameter("check_out_date", checkOutDate)
                    .addParameter("adults", String.valueOf(adults == null ? 2 : adults))
                    .addParameter("children", String.valueOf(children == null ? 0 : children));

            if (maxPrice != null && maxPrice > 0) {
                builder.addParameter("max_price", String.valueOf(maxPrice));
            }
            if (sortBy != null && !sortBy.isBlank()) {
                builder.addParameter("sort_by", sortBy);
            }

            HttpGet get = new HttpGet(builder.build());
            try (CloseableHttpResponse response = client.execute(get)) {
                int status = response.getCode();
                String body = response.getEntity() == null ? "" : new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                if (status >= 200 && status < 300) {
                    return body;
                }
                String snippet = body.substring(0, Math.min(body.length(), 500));
                throw new BadRequestException("SerpAPI trả về lỗi (" + status + "): " + snippet);
            }
        } catch (URISyntaxException e) {
            throw new BadRequestException("SerpAPI URL lỗi: " + e.getMessage());
        }
    }
}


