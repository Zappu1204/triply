package com.triply.tripapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.triply.tripapp.entity.*;
import com.triply.tripapp.integration.PerplexityClient;
import com.triply.tripapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        BigDecimal flightCapPerPerson = req.budget.multiply(BigDecimal.valueOf(0.3)).divide(BigDecimal.valueOf(req.people));
        BigDecimal lodgingCap = req.budget.multiply(BigDecimal.valueOf(0.3));
        BigDecimal attractionsCap = req.budget.multiply(BigDecimal.valueOf(0.1));

        String promptFlights = "Find round-trip air tickets from " + (req.origin == null ? "origin city" : req.origin)
                + " to " + req.destination + " for under " + flightCapPerPerson + " VND per person with departure date "
                + req.startDate + " and return date " + req.endDate + ". Only return top 5 results with destination, airline, price_vnd, flight_duration.";
        String flightsSchema = schemaArray("flights", new String[]{"destination","airline","price_vnd","flight_duration"});
        JsonNode flights = extractArray(perplexityClient.chatCompletionsJson(promptFlights, flightsSchema), "flights");

        String promptHotels = "Find 3 hotels in " + req.destination + " whose total stay price between " + req.startDate + " and " + req.endDate
                + " is under " + lodgingCap + " VND, sorted ascending by total price. Return name, total_price_vnd, address.";
        String hotelsSchema = schemaArray("hotels", new String[]{"name","total_price_vnd","address"});
        JsonNode hotels = extractArray(perplexityClient.chatCompletionsJson(promptHotels, hotelsSchema), "hotels");

        String promptAttractions = "List popular attractions within 50km of " + req.destination + " with estimated cost per person under "
                + attractionsCap + ". Return top 5 with name, est_cost_vnd, category.";
        String attractionsSchema = schemaArray("attractions", new String[]{"name","est_cost_vnd","category"});
        JsonNode attractions = extractArray(perplexityClient.chatCompletionsJson(promptAttractions, attractionsSchema), "attractions");

        PlanResponse response = new PlanResponse();
        addAll(response.flights, flights);
        addAll(response.hotels, hotels);
        addAll(response.attractions, attractions);
        response.suggestedTitle = "Trip to " + req.destination;
        response.estimatedTotal = req.budget; // rough
        return response;
    }

    private PlanResponse planScenario2(PlanRequest req) throws IOException {
        BigDecimal flightCapPerPerson = req.budget.multiply(BigDecimal.valueOf(0.3)).divide(BigDecimal.valueOf(req.people));

        String promptCandidates = "Suggest 5 nearby destinations from " + (req.origin == null ? "origin city" : req.origin)
                + " with round-trip flights under " + flightCapPerPerson + " VND per person. Return destination, airline, price_vnd, flight_duration.";
        String schema = schemaArray("flights", new String[]{"destination","airline","price_vnd","flight_duration"});
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
            sb.append("\"").append(field).append("\": { \"type\": \"string\" }");
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
        JsonNode node = objectMapper.readTree(json);
        JsonNode contentNode =  node.path("choices").path(0).path("message").path("content");
        String content = contentNode.isMissingNode() ? "" : contentNode.asText();
        if (content.isBlank()) {
            return objectMapper.createArrayNode();
        }
        node = objectMapper.readTree(content);
        return node.path(field);
    }

    private void addAll(List<JsonNode> target, JsonNode array) {
        if (array != null && array.isArray()) {
            array.forEach(target::add);
        }
    }

    @Transactional
    public Trip savePlannedTrip(Integer customerId, String title, LocalDate start, LocalDate end, BigDecimal totalBudget,
                                String currency, List<ItineraryItem> items, List<Expense> estimatedExpenses) {
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
            for (ItineraryItem item : items) {
                item.setTripId(savedTrip.getTripId());
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
}



