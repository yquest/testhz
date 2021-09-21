package com.capgemini.rest.train;

import java.time.Instant;
import java.util.Objects;

public class TravelRequest {
    private final Instant start;
    private final Long route;
    private final String trainType;

    public TravelRequest(Instant start, Long route, String trainType) {
        this.start = start;
        this.route = route;
        this.trainType = trainType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TravelRequest that = (TravelRequest) o;
        return Objects.equals(start, that.start) && Objects.equals(route, that.route) && Objects.equals(trainType, that.trainType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, route, trainType);
    }

    public Instant getStart() {
        return start;
    }

    public Long getRoute() {
        return route;
    }

    public String getTrainType() {
        return trainType;
    }
}
