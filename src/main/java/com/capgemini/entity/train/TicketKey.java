package com.capgemini.entity.train;

import com.capgemini.store.train.RailroadCarTravelKey;

import java.time.Instant;
import java.util.Objects;

public class TicketKey extends RailroadCarTravelKey {
    private static final long serialVersionUID = -7080611128787351168L;
    private final String startStation;
    private final String seat;

    public TicketKey(long route, Instant start, long railroadCar, String startStation, String seat) {
        super(route, start, railroadCar);
        this.startStation = startStation;
        this.seat = seat;
    }

    public String getStartStation() {
        return startStation;
    }

    public String getSeat() {
        return seat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TicketKey ticketKey = (TicketKey) o;
        return Objects.equals(startStation, ticketKey.startStation) && Objects.equals(seat, ticketKey.seat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), startStation, seat);
    }

    @Override
    public String toString() {
        return "TicketKey{" +
                "startStation='" + startStation + '\'' +
                ", seat='" + seat + '\'' +
                "} " + super.toString();
    }

    @Override
    public TicketKey clone() {
        TicketKey clone;
        try {
            clone = (TicketKey) super.clone();
        } catch (CloneNotSupportedException e) {
            clone = new TicketKey(this.getRoute(), this.getStart(), this.getRailroadCar(), this.getStartStation(), this.getSeat());
        }
        return clone;
    }
}