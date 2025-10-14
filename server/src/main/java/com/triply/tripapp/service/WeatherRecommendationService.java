package com.triply.tripapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class WeatherRecommendationService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ObjectNode buildRecommendations(String forecastJson, String locationName) throws IOException {
        JsonNode root = objectMapper.readTree(forecastJson);
        JsonNode location = root.path("location");
        JsonNode forecastDays = root.path("forecast").path("forecastday");

        String loc = locationName != null && !locationName.isBlank() ? locationName : location.path("name").asText("");

        ObjectNode out = objectMapper.createObjectNode();
        out.put("location", loc);
        if (forecastDays.isArray() && forecastDays.size() > 0) {
            String start = forecastDays.get(0).path("date").asText("");
            String end = forecastDays.get(forecastDays.size()-1).path("date").asText("");
            out.put("forecast_period", start + " to " + end);
        } else {
            out.put("forecast_period", "");
        }

        ArrayNode daily = objectMapper.createArrayNode();
        out.set("daily_recommendations", daily);

        for (JsonNode day : forecastDays) {
            ObjectNode dayNode = objectMapper.createObjectNode();
            dayNode.put("date", day.path("date").asText(""));

            ArrayNode timeSlots = objectMapper.createArrayNode();
            dayNode.set("time_slots", timeSlots);

            addSlot(day, timeSlots, "08:00-11:00", "Hoạt động sáng");
            addSlot(day, timeSlots, "11:00-14:00", "Ăn nghỉ trưa");
            addSlot(day, timeSlots, "14:00-17:00", "Hoạt động chiều");
            addSlot(day, timeSlots, "17:00-20:00", "Ăn tối");
            addSlot(day, timeSlots, "20:00-22:00", "Hoạt động tối");

            daily.add(dayNode);
        }

        return out;
    }

    private void addSlot(JsonNode day, ArrayNode timeSlots, String timeRange, String periodName) {
        String[] parts = timeRange.split("-");
        LocalTime start = LocalTime.parse(parts[0]);
        LocalTime end = LocalTime.parse(parts[1]);

        SlotWeather sw = aggregateSlotWeather(day, start, end);

        Map<String, ActivityThreshold> thresholds = defaultThresholds();

        ArrayNode suitable = new ObjectMapper().createArrayNode();
        for (Map.Entry<String, ActivityThreshold> e : thresholds.entrySet()) {
            double score = calculateWeatherScore(sw, e.getValue());
            if (score >= 50) {
                suitable.add(e.getKey());
            }
        }

        ObjectNode slot = new ObjectMapper().createObjectNode();
        slot.put("time", timeRange);
        slot.put("period_name", periodName);
        slot.set("suitable_activities", suitable);
        slot.put("outdoor_ok", sw.precipitation <= 0.5 && sw.windSpeed <= 25.0);

        ObjectNode summary = new ObjectMapper().createObjectNode();
        summary.put("temp", round(sw.temp));
        summary.put("rain", round(sw.precipitation));
        slot.set("weather_summary", summary);

        timeSlots.add(slot);
    }

    private SlotWeather aggregateSlotWeather(JsonNode day, LocalTime start, LocalTime end) {
        JsonNode hours = day.path("hour");
        double tSum = 0.0, rSum = 0.0, hSum = 0.0, uvSum = 0.0, wSum = 0.0;
        int count = 0;
        for (JsonNode h : hours) {
            String timeStr = h.path("time").asText("");
            if (timeStr.length() < 11) continue; // "YYYY-MM-DD HH:MM"
            String hhmm = timeStr.substring(timeStr.length() - 5);
            LocalTime t = LocalTime.parse(hhmm);
            if (!t.isBefore(start) && !t.isAfter(end)) {
                tSum += h.path("temp_c").asDouble(0.0);
                rSum += h.path("precip_mm").asDouble(0.0);
                hSum += h.path("humidity").asDouble(0.0);
                uvSum += h.path("uv").asDouble(0.0);
                wSum += h.path("wind_kph").asDouble(0.0);
                count++;
            }
        }
        if (count == 0) count = 1;
        SlotWeather sw = new SlotWeather();
        sw.temp = tSum / count;
        sw.precipitation = rSum / count;
        sw.humidity = hSum / count;
        sw.uv = uvSum / count;
        sw.windSpeed = wSum / count;
        return sw;
    }

    private Map<String, ActivityThreshold> defaultThresholds() {
        Map<String, ActivityThreshold> m = new HashMap<>();
        m.put("Museum/Indoor", new ActivityThreshold(-10, 50, 60, 10.0, 0, 11, 100));
        m.put("Photography", new ActivityThreshold(15, 32, 20, 1.0, 0, 8, 90));
        m.put("Hiking", new ActivityThreshold(18, 28, 25, 0.5, 0, 8, 85));
        m.put("Beach/Swimming", new ActivityThreshold(24, 35, 30, 1.5, 0, 10, 95));
        m.put("Cycling", new ActivityThreshold(18, 32, 20, 0.5, 0, 8, 85));
        m.put("Camping", new ActivityThreshold(20, 30, 25, 0.5, 0, 8, 85));
        return m;
    }

    public double calculateWeatherScore(SlotWeather weather, ActivityThreshold threshold) {
        double score = 100.0;
        if (weather.temp < threshold.minTemp || weather.temp > threshold.maxTemp) score -= 40;
        if (weather.windSpeed > threshold.maxWind) score -= 20;
        if (weather.precipitation > threshold.maxPrecip) score -= 30;
        if (weather.uv < threshold.minUV || weather.uv > threshold.maxUV) score -= 5;
        if (weather.humidity > threshold.maxHumidity) score -= 5;
        return Math.max(0, score);
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    public static class SlotWeather {
        public double temp;
        public double windSpeed;
        public double precipitation;
        public double uv;
        public double humidity;
    }

    public static class ActivityThreshold {
        public double minTemp;
        public double maxTemp;
        public double maxWind;
        public double maxPrecip;
        public double minUV;
        public double maxUV;
        public double maxHumidity;

        public ActivityThreshold(double minTemp, double maxTemp, double maxWind, double maxPrecip,
                                 double minUV, double maxUV, double maxHumidity) {
            this.minTemp = minTemp;
            this.maxTemp = maxTemp;
            this.maxWind = maxWind;
            this.maxPrecip = maxPrecip;
            this.minUV = minUV;
            this.maxUV = maxUV;
            this.maxHumidity = maxHumidity;
        }
    }
}


