package com.capgemini.rest.train;

import com.capgemini.entity.train.TravelState;

import java.time.Instant;

public class UpdateTravelStateRequest {
    private final Instant time;
    private final Long route;
    private final TravelState state;

    public UpdateTravelStateRequest(Instant time, Long route, TravelState state) {
        this.time = time;
        this.route = route;
        this.state = state;
    }

    public Instant getTime() {
        return time;
    }

    public Long getRoute() {
        return route;
    }

    public TravelState getState() {
        return state;
    }
}
