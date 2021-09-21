package com.capgemini.entity.train;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hazelcast.partition.PartitionAware;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public class SeatKey implements PartitionAware<Integer>, Serializable {
    private static final long serialVersionUID = 9044635413136048400L;
    private final long route;
    private final Instant start;
    private final long railroadCar;
    private final String seatPlace;
    private final String station;

    public SeatKey(long route, Instant start, long railroadCar, String seatPlace, String station) {
        this.route = route;
        this.start = start;
        this.railroadCar = railroadCar;
        this.seatPlace = seatPlace;
        this.station = station;
    }

    @Override
    @JsonIgnore
    public Integer getPartitionKey() {
        return Objects.hash( route,start, railroadCar);
    }

    public Instant getStart() {
        return start;
    }

    public long getRoute() {
        return route;
    }

    public long getRailroadCar() {
        return railroadCar;
    }

    public String getSeatPlace() {
        return seatPlace;
    }

    public String getStation() {
        return station;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SeatKey seatKey = (SeatKey) o;
        return route == seatKey.route && railroadCar == seatKey.railroadCar && Objects.equals(start, seatKey.start) && Objects.equals(seatPlace, seatKey.seatPlace) && Objects.equals(station, seatKey.station);
    }

    @Override
    public int hashCode() {
        return Objects.hash(route, start, railroadCar, seatPlace, station);
    }

    @Override
    public String toString() {
        return "SeatKey{" +
                "route=" + route +
                ", start=" + start +
                ", railroadCar=" + railroadCar +
                ", seatPlace='" + seatPlace + '\'' +
                ", station='" + station + '\'' +
                '}';
    }
}
