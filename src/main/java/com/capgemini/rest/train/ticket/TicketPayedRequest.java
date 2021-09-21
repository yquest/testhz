package com.capgemini.rest.train.ticket;

import com.capgemini.entity.train.TicketKey;

import java.time.Instant;
import java.util.Objects;

public class TicketPayedRequest extends TicketKey {
    private static final long serialVersionUID = -697608440171954846L;
    private final String endStation;

    public TicketPayedRequest(long route, Instant start, long railroadCar, String startStation, String seat, String endStation) {
        super(route, start, railroadCar, startStation, seat);
        this.endStation = endStation;
    }

    public String getEndStation() {
        return endStation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TicketPayedRequest that = (TicketPayedRequest) o;
        return Objects.equals(endStation, that.endStation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), endStation);
    }

    @Override
    public String toString() {
        return "TicketPayedRequest{" +
                "endStation='" + endStation + '\'' +
                "} " + super.toString();
    }
}
