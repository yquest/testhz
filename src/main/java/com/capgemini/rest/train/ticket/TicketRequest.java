package com.capgemini.rest.train.ticket;

import java.time.Instant;
import java.util.Objects;

public class TicketRequest {
    private final Instant start;
    private final Long route;
    private final Long railroadCar;
    private final String startStation;
    private final String endStation;
    private final Long userId;
    private final String seatPlace;

    public TicketRequest(Instant start, Long route, Long railroadCar, String startStation, String endStation, Long userId, String seatPlace) {
        this.start = start;
        this.route = route;
        this.railroadCar = railroadCar;
        this.startStation = startStation;
        this.endStation = endStation;
        this.userId = userId;
        this.seatPlace = seatPlace;
    }

    public Instant getStart() {
        return start;
    }

    public Long getRoute() {
        return route;
    }

    public Long getRailroadCar() {
        return railroadCar;
    }

    public String getStartStation() {
        return startStation;
    }

    public String getEndStation() {
        return endStation;
    }

    public Long getUserId() {
        return userId;
    }

    public String getSeatPlace() {
        return seatPlace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TicketRequest that = (TicketRequest) o;
        return Objects.equals(start, that.start) &&
                Objects.equals(route, that.route) &&
                Objects.equals(railroadCar, that.railroadCar) &&
                Objects.equals(startStation, that.startStation) &&
                Objects.equals(endStation, that.endStation) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(seatPlace, that.seatPlace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, route, railroadCar, startStation, endStation, userId, seatPlace);
    }

    @Override
    public String toString() {
        return "TicketRequest{" +
                "start=" + start +
                ", route=" + route +
                ", railroadCar=" + railroadCar +
                ", startStation='" + startStation + '\'' +
                ", endStation='" + endStation + '\'' +
                ", userId=" + userId +
                ", seatPlace='" + seatPlace + '\'' +
                '}';
    }
}
