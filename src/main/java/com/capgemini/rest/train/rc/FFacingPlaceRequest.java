package com.capgemini.rest.train.rc;

import java.time.Instant;
import java.util.Objects;

/**
 * Forward facing place request in travel between two stations
 */
public class FFacingPlaceRequest {
    private final String startStation;
    private final String endStation;
    private final Instant start;
    private final long route;

    public FFacingPlaceRequest(String startStation, String endStation, Instant start, long route) {
        this.startStation = startStation;
        this.endStation = endStation;
        this.start = start;
        this.route = route;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FFacingPlaceRequest that = (FFacingPlaceRequest) o;
        return route == that.route && Objects.equals(startStation, that.startStation) && Objects.equals(endStation, that.endStation) && Objects.equals(start, that.start);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startStation, endStation, start, route);
    }

    public String getStartStation() {
        return startStation;
    }

    public String getEndStation() {
        return endStation;
    }

    public Instant getStart() {
        return start;
    }

    public long getRoute() {
        return route;
    }

    @Override
    public String toString() {
        return "FFacingPlaceRequest{" +
                "startStation='" + startStation + '\'' +
                ", endStation='" + endStation + '\'' +
                ", start=" + start +
                ", route=" + route +
                '}';
    }
}
