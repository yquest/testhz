package com.capgemini.rest.train;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class AddRailroadCarTravelRequest {
    private final Instant start;
    private final Long route;
    private final List<Long> railroadCars;

    public AddRailroadCarTravelRequest(Instant start, Long route, List<Long> railroadCars) {
        this.start = start;
        this.route = route;
        this.railroadCars = railroadCars;
    }

    public Instant getStart() {
        return start;
    }

    public List<Long> getRailroadCars() {
        return railroadCars;
    }

    public Long getRoute() {
        return route;
    }

    @Override
    public String toString() {
        return "AddRailroadCarTravel{" +
                "start=" + start +
                ", route=" + route +
                ", railroadCars=" + railroadCars +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddRailroadCarTravelRequest that = (AddRailroadCarTravelRequest) o;
        return Objects.equals(start, that.start) && Objects.equals(route, that.route) && Objects.equals(railroadCars, that.railroadCars);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, route, railroadCars);
    }
}
