package com.triply.tripapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.triply.tripapp.entity.*;
import com.triply.tripapp.integration.PerplexityClient;
import com.triply.tripapp.integration.WeatherApiClient;
import com.triply.tripapp.integration.SerpApiClient;
import com.triply.tripapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class TripPlanningService {

    @Autowired
    private PerplexityClient perplexityClient;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private DestinationRepository destinationRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ItineraryItemRepository itineraryItemRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private SerpApiClient serpApiClient;

    @Autowired
    private WeatherApiClient weatherApiClient;

    @Autowired
    private WeatherRecommendationService weatherRecommendationService;

    @Autowired
    private TripFlightRepository tripFlightRepository;

    @Autowired
    private TripHotelRepository tripHotelRepository;

    @Autowired
    private TripAttractionRepository tripAttractionRepository;

    public static class PlanRequest {
        public BigDecimal budget;
        public LocalDate startDate;
        public LocalDate endDate;
        public Integer people;
        public String origin; // optional; if null -> GPS by mobile app
        public String destination; // optional
    }

    public static class PlanResponse {
        public List<JsonNode> flights = new ArrayList<>();
        public List<JsonNode> flightBest = new ArrayList<>();
        public List<JsonNode> flightOther = new ArrayList<>();
        public List<JsonNode> hotels = new ArrayList<>();
        public List<JsonNode> attractions = new ArrayList<>();
        public List<JsonNode> weather = new ArrayList<>();
        public String suggestedTitle;
        public BigDecimal estimatedTotal;
    }

    public PlanResponse plan(PlanRequest req) throws IOException {
        boolean hasDestination = req.destination != null && !req.destination.isBlank();
        if (hasDestination) {
            return planScenario1(req);
        } else {
            return planScenario2(req);
        }
    }

    private PlanResponse planScenario1(PlanRequest req) throws IOException {
        PlanResponse response = new PlanResponse();
        
        BigDecimal flightCapPerPerson = req.budget.multiply(BigDecimal.valueOf(0.45)).divide(BigDecimal.valueOf(req.people));
        BigDecimal lodgingCap = req.budget.multiply(BigDecimal.valueOf(0.3));
        BigDecimal attractionsCap = req.budget.multiply(BigDecimal.valueOf(0.1));

        String promptFlights = "Find round-trip air tickets from " + (req.origin == null ? "origin city" : req.origin)
                + " to " + req.destination + " for under " + flightCapPerPerson + " VND per person with departure date "
                + req.startDate + " and return date " + req.endDate
                + ". Only return top 5 results with departure_id(iata-3-letter location code), arrival_id(iata-3-letter location code),"
                + " airline_id(2-letter code), destination, airline, price_vnd, flight_duration.";
        String flightsSchema = schemaArray("flights", new String[]{"departure_id","destination", "arrival_id", "airline", "airline_id", "price_vnd","flight_duration"});
        JsonNode flights = extractArray(perplexityClient.chatCompletionsJson(promptFlights, flightsSchema), "flights");

        if (flights != null && flights.isArray()) {
            Set<String> airlineIdList = new HashSet<>();

            for (JsonNode f : flights) {
                airlineIdList.add(text(f, "airline_id"));
            }

            String dep = text(flights.get(0), "departure_id");
            String arr = text(flights.get(0), "arrival_id");
            String airlineIdListStr = String.join(",", airlineIdList);
            String serpJson = serpApiClient.searchGoogleFlights(
                dep,
                arr,
                "", //airlineIdListStr,
                String.valueOf(req.startDate),
                String.valueOf(req.endDate),
                "VND",
                flightCapPerPerson.intValue(),
                "vi",
                "vn"
            );
            JsonNode moreBestFlights = extractMoreBestFlights(serpJson);
            JsonNode moreOtherFlights = extractMoreOtherFlights(serpJson);
            // Chỉ lấy tối đa 5 kết quả trong More Other Flights
            if (moreOtherFlights != null && moreOtherFlights.size() > 5) {
                ArrayNode newFlights = objectMapper.createArrayNode();
                for (int i = 0; i < 5 && i < moreOtherFlights.size(); i++) {
                    newFlights.add(moreOtherFlights.get(i));
                }
            }
            addAll(response.flightBest, moreBestFlights);
            addAll(response.flightOther, moreOtherFlights);
        }

        // Replace Perplexity hotels with SerpAPI Google Hotels
        String hotelsJson = serpApiClient.searchGoogleHotels(
                "Hotel in " + req.destination,
                String.valueOf(req.startDate),
                String.valueOf(req.endDate),
                req.people == null ? 2 : req.people,
                0,
                "VND",
                lodgingCap.intValue(),
                "8", // sort by Best value / price asc depending on SerpAPI mapping
                "vi",
                "vn"
        );
        JsonNode hotels = extractHotelsProperties(hotelsJson);

        // Build weather-based activity recommendations using the first hotel's GPS
        if (hotels != null && hotels.isArray() && hotels.size() > 0) {
            JsonNode firstHotel = hotels.get(0);
            double lat = firstHotel.path("gps_coordinates").path("latitude").asDouble(Double.NaN);
            double lon = firstHotel.path("gps_coordinates").path("longitude").asDouble(Double.NaN);
            if (Double.isNaN(lat) || Double.isNaN(lon)) {
                lat = firstHotel.path("latitude").asDouble(Double.NaN);
                lon = firstHotel.path("longitude").asDouble(Double.NaN);
            }
            if (!Double.isNaN(lat) && !Double.isNaN(lon)) {
                String forecastJson = weatherApiClient.getForecastByLatLon(lat, lon, 3);
                String locName = (req.destination != null && !req.destination.isBlank()) ? req.destination : firstHotel.path("name").asText("");
                ObjectNode rec = weatherRecommendationService.buildRecommendations(forecastJson, locName);
                
                String recJsonStr = objectMapper.writeValueAsString(rec);

                // Ask Perplexity for detailed itinerary/places using recommendations JSON
                String itinerarySchema = schemaItinerary();
                String prompt = buildItineraryPrompt(locName, recJsonStr);
                JsonNode itinerary = extractArray(perplexityClient.chatCompletionsJson(prompt, itinerarySchema), "itinerary");

                // ObjectNode carrier = objectMapper.createObjectNode();
                // carrier.set("recommendations", rec);
                // carrier.set("itinerary", itinerary == null ? objectMapper.createArrayNode() : itinerary);
                
                addAll(response.attractions, itinerary == null ? objectMapper.createArrayNode() : itinerary);
            }
        }

//        String promptAttractions = "List popular attractions within 50km of " + req.destination + " with estimated cost per person under "
//               + attractionsCap + ". Return top 5 with name, est_cost_vnd, category.";
//        String attractionsSchema = schemaArray("attractions", new String[]{"name","est_cost_vnd","category"});
//        JsonNode attractions = extractArray(perplexityClient.chatCompletionsJson(promptAttractions, attractionsSchema), "attractions");

        addAll(response.flights, flights);
        addAll(response.hotels, hotels);
//        addAll(response.attractions, attractions);
        response.suggestedTitle = "Trip to " + req.destination;
        response.estimatedTotal = req.budget; // rough
        return response;
    }

    private PlanResponse planScenario2(PlanRequest req) throws IOException {
        BigDecimal flightCapPerPerson = req.budget.multiply(BigDecimal.valueOf(0.3)).divide(BigDecimal.valueOf(req.people));

        String promptCandidates = "Suggest 5 nearby destinations from " + (req.origin == null ? "origin city" : req.origin)
                + " with round-trip flights under " + flightCapPerPerson + " VND per person. Return"
                + " departure_id(iata-3-letter location code), arrival_id(iata-3-letter location code),"
                + " airline_id(2-letter code), destination, airline, price_vnd(int), flight_duration.";
        String schema = schemaArray("flights", new String[]{"departure_id","destination", "arrival_id", "airline","price_vnd","flight_duration"});
        JsonNode flights = extractArray(perplexityClient.chatCompletionsJson(promptCandidates, schema), "flights");

        PlanResponse response = new PlanResponse();
        addAll(response.flights, flights);
        response.suggestedTitle = "Suggested destinations";
        response.estimatedTotal = req.budget;
        return response;
    }

    private String schemaArray(String root, String[] fields) {
        StringBuilder sb = new StringBuilder();
        sb
                .append("{")
                .append("\"type\":\"object\",")
                .append("\"properties\": { \n")
                .append("\"").append(root).append("\": {")
                .append("\"type\":\"array\",")
                .append("\"items\": {")
                .append("\"type\":\"object\",")
                .append("\"properties\": { \n");

        for (String field : fields) {
            if (field.contains("vnd")) {
                sb.append("\"").append(field).append("\": { \"type\": \"integer\" }");
            } else {
                sb.append("\"").append(field).append("\": { \"type\": \"string\" }");
            }
//            sb.append("\"").append(field).append("\": { \"type\": \"string\" }");
            if (!field.equals(fields[fields.length - 1])) {
                sb.append(",\n");
            }
        }
        sb.append("},\n");
        sb.append("\"required\": [");
        for (String field : fields) {
            sb.append("\"").append(field).append("\"");
            if (!field.equals(fields[fields.length - 1])) {
                sb.append(", ");
            }
        }

        sb.append("]\n");
        sb.append("}\n");
        sb.append("}\n");
        sb.append("},\n");
        sb.append("\"required\": [\"").append(root).append("\"]\n");
        sb.append("}\n");
        return sb.toString();
    }

    private JsonNode extractArray(String json, String field) throws IOException {
        try {
            JsonNode node = objectMapper.readTree(json);
            JsonNode contentNode =  node.path("choices").path(0).path("message").path("content");
            String content = contentNode.isMissingNode() ? "" : contentNode.asText();
            if (content.isBlank()) {
                return objectMapper.createArrayNode();
            }
            node = objectMapper.readTree(content);
            return node.path(field);
        }
        catch (Exception e) {
            throw new IOException("Lỗi phân tích JSON từ Perplexity: " + e.getMessage() + ". JSON: " + json, e);
        }
    }

    private JsonNode extractHotelsProperties(String serpHotelsJson) throws IOException {
        if (serpHotelsJson == null || serpHotelsJson.isBlank()) {
            return objectMapper.createArrayNode();
        }
        JsonNode root = objectMapper.readTree(serpHotelsJson);
        JsonNode properties = root.path("properties");
        return properties.isMissingNode() ? objectMapper.createArrayNode() : properties;
    }

    private String schemaItinerary() {
        return "{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"itinerary\": {\n" +
                "      \"type\": \"array\",\n" +
                "      \"items\": {\n" +
                "        \"type\": \"object\",\n" +
                "        \"properties\": {\n" +
                "          \"date\": { \"type\": \"string\" },\n" +
                "          \"schedule\": {\n" +
                "            \"type\": \"array\",\n" +
                "            \"items\": {\n" +
                "              \"type\": \"object\",\n" +
                "              \"properties\": {\n" +
                "                \"time\": { \"type\": \"string\" },\n" +
                "                \"activity\": { \"type\": \"string\" },\n" +
                "                \"location\": { \"type\": \"string\" },\n" +
                "                \"reason\": { \"type\": \"string\" }\n" +
                "              },\n" +
                "              \"required\": [\"time\", \"activity\", \"location\"]\n" +
                "            }\n" +
                "          }\n" +
                "        },\n" +
                "        \"required\": [\"date\", \"schedule\"]\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"itinerary\"]\n" +
                "}";
    }

    private String buildItineraryPrompt(String destination, String recommendations) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("Dựa trên dữ liệu thời tiết và hoạt động phù hợp sau đây, hãy đề xuất lịch trình du lịch chi tiết tại ")
          .append(destination == null ? "điểm đến" : destination)
          .append(":\n\n");
        sb.append(recommendations).append("\n\n");
        sb.append("Yêu cầu output JSON format với các trường date, schedule[{time, activity, location, reason}].");
        return sb.toString();
    }

    private JsonNode extractMoreBestFlights(String serpJson) throws IOException {
        if (serpJson == null || serpJson.isBlank()) return null;
        JsonNode root = objectMapper.readTree(serpJson);
        JsonNode bestFlights = root.path("best_flights");
        return bestFlights.isMissingNode() ? objectMapper.createArrayNode() : bestFlights;
    }

    private JsonNode extractMoreOtherFlights(String serpJson) throws IOException {
        if (serpJson == null || serpJson.isBlank()) return null;
        JsonNode root = objectMapper.readTree(serpJson);
        JsonNode otherFlights = root.path("other_flights");
        return otherFlights.isMissingNode() ? objectMapper.createArrayNode() : otherFlights;
    }

    private void addAll(List<JsonNode> target, JsonNode array) {
        if (array != null && array.isArray()) {
            array.forEach(target::add);
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }

    private Integer intOrNull(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) return null;
        try { return v.asInt(); } catch (Exception e) { return null; }
    }

    @Transactional
    public Trip savePlannedTrip(Integer customerId, String title, LocalDate start, LocalDate end, BigDecimal totalBudget,
                                String currency, List<com.triply.tripapp.controller.TripPlanningController.ItineraryItemRequest> items, List<Expense> estimatedExpenses) {
        Trip trip = new Trip();
        trip.setTripName(title);
        trip.setStartDate(start);
        trip.setEndDate(end);
        trip.setCustomerId(customerId);
        Trip savedTrip = tripRepository.save(trip);

        Budget budget = new Budget();
        budget.setTripId(savedTrip.getTripId());
        budget.setTotalAmount(totalBudget);
        budget.setCurrency(currency);
        budgetRepository.save(budget);

        if (items != null) {
            for (com.triply.tripapp.controller.TripPlanningController.ItineraryItemRequest itemReq : items) {
                // Validate destinationName
                if (itemReq.destinationName == null || itemReq.destinationName.trim().isEmpty()) {
                    throw new IllegalArgumentException("Destination name cannot be null or empty");
                }
                
                String destName = itemReq.destinationName.trim();
                
                // Find or create destination
                Destination destination = destinationRepository.findByName(destName)
                        .orElseGet(() -> {
                            Destination newDest = new Destination();
                            newDest.setName(destName);
                            newDest.setCreatedAt(java.time.LocalDateTime.now());
                            return destinationRepository.save(newDest);
                        });

                // Create itinerary item
                ItineraryItem item = new ItineraryItem();
                item.setTripId(savedTrip.getTripId());
                item.setDestinationId(destination.getDestinationId());
                item.setArrivalDate(itemReq.arrivalDate);
                item.setDepartureDate(itemReq.departureDate);
                item.setNotes(itemReq.notes);
                itineraryItemRepository.save(item);
            }
        }

        if (estimatedExpenses != null) {
            for (Expense e : estimatedExpenses) {
                e.setBudgetId(budget.getBudgetId());
                expenseRepository.save(e);
            }
        }

        return savedTrip;
    }

    @Transactional
    public Integer saveCompleteTripPlan(Integer customerId, com.triply.tripapp.controller.TripPlanningController.SaveFullPlanRequest req) {
        // 1. Create and save Trip
        Trip trip = new Trip();
        trip.setTripName(req.title);
        trip.setStartDate(req.startDate);
        trip.setEndDate(req.endDate);
        trip.setCustomerId(customerId);
        Trip savedTrip = tripRepository.save(trip);
        Integer tripId = savedTrip.getTripId();

        // 2. Create and save Budget
        Budget budget = new Budget();
        budget.setTripId(tripId);
        budget.setTotalAmount(req.totalBudget);
        budget.setCurrency(req.currency);
        Budget savedBudget = budgetRepository.save(budget);

        // 3. Save Itinerary Items (destinations)
        if (req.destinations != null && !req.destinations.isEmpty()) {
            for (com.triply.tripapp.controller.TripPlanningController.ItineraryItemRequest itemReq : req.destinations) {
                if (itemReq.destinationName == null || itemReq.destinationName.trim().isEmpty()) {
                    throw new IllegalArgumentException("Destination name cannot be null or empty");
                }
                
                String destName = itemReq.destinationName.trim();
                
                // Find or create destination
                Destination destination = destinationRepository.findByName(destName)
                        .orElseGet(() -> {
                            Destination newDest = new Destination();
                            newDest.setName(destName);
                            newDest.setCreatedAt(java.time.LocalDateTime.now());
                            return destinationRepository.save(newDest);
                        });

                // Create itinerary item
                ItineraryItem item = new ItineraryItem();
                item.setTripId(tripId);
                item.setDestinationId(destination.getDestinationId());
                item.setArrivalDate(itemReq.arrivalDate);
                item.setDepartureDate(itemReq.departureDate);
                item.setNotes(itemReq.notes);
                itineraryItemRepository.save(item);
            }
        }

        // 4. Save Flight information
        if (req.flight != null) {
            TripFlight flight = new TripFlight();
            flight.setTripId(tripId);
            flight.setDepartureId(req.flight.departureId);
            flight.setArrivalId(req.flight.arrivalId);
            flight.setAirline(req.flight.airline);
            flight.setAirlineId(req.flight.airlineId);
            flight.setPriceVnd(req.flight.priceVnd);
            flight.setFlightDuration(req.flight.flightDuration);
            flight.setDepartureTime(req.flight.departureTime);
            flight.setArrivalTime(req.flight.arrivalTime);
            tripFlightRepository.save(flight);
        }

        // 5. Save Hotel information
        if (req.hotel != null) {
            TripHotel hotel = new TripHotel();
            hotel.setTripId(tripId);
            hotel.setName(req.hotel.name);
            hotel.setAddress(req.hotel.address);
            hotel.setLatitude(req.hotel.latitude);
            hotel.setLongitude(req.hotel.longitude);
            hotel.setPriceTotalVnd(req.hotel.priceTotalVnd);
            tripHotelRepository.save(hotel);
        }

        // 6. Save daily attractions
        if (req.dailyAttractions != null && !req.dailyAttractions.isEmpty()) {
            for (com.triply.tripapp.controller.TripPlanningController.TripAttractionRequest attrReq : req.dailyAttractions) {
                TripAttraction attraction = new TripAttraction();
                attraction.setTripId(tripId);
                attraction.setDate(attrReq.date);
                attraction.setTime(attrReq.time);
                attraction.setActivity(attrReq.activity);
                attraction.setLocation(attrReq.location);
                attraction.setReason(attrReq.reason);
                tripAttractionRepository.save(attraction);
            }
        }

        // 7. Save expenses
        if (req.expenses != null && !req.expenses.isEmpty()) {
            for (com.triply.tripapp.controller.TripPlanningController.ExpenseRequest expReq : req.expenses) {
                Expense expense = new Expense();
                expense.setBudgetId(savedBudget.getBudgetId());
                expense.setAmount(expReq.amount);
                expense.setCategory(expReq.category);
                expense.setDate(expReq.date);
                expenseRepository.save(expense);
            }
        }

        return tripId;
    }
}



