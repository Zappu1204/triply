package com.triply.tripapp.entity;

import java.io.Serializable;
import java.util.Objects;

public class ItineraryItemId implements Serializable {
    private Integer tripId;
    private Integer destinationId;

    public ItineraryItemId() {}

    public ItineraryItemId(Integer tripId, Integer destinationId) {
        this.tripId = tripId;
        this.destinationId = destinationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItineraryItemId that = (ItineraryItemId) o;
        return Objects.equals(tripId, that.tripId) && Objects.equals(destinationId, that.destinationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tripId, destinationId);
    }
}



