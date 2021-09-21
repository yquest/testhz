package com.capgemini.entity.train;

import com.capgemini.store.train.RailroadCarTravelKey;
import com.hazelcast.partition.PartitionAware;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public class TravelKey implements PartitionAware<Long>, Serializable {
    private static final long serialVersionUID = -4017344805359117625L;
    private final long route;
    private final Instant start;

    public TravelKey(long route, Instant start) {
        this.route = route;
        this.start = start;
    }

    @Override
    public Long getPartitionKey() {
        return route;
    }

    public Instant getStart() {
        return start;
    }

    public long getRoute() {
        return route;
    }

    @Override
    public String toString() {
        return "TravelKey{" +
                "route=" + route +
                ", start=" + start +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TravelKey travelKey = (TravelKey) o;
        return route == travelKey.route && Objects.equals(start, travelKey.start);
    }

    @Override
    public int hashCode() {
        return Objects.hash(route, start);
    }

    public RailroadCarTravelKey createRailroadCarTravelKey(long railroadCar) {
        return new RailroadCarTravelKey(route, start, railroadCar);
    }
}
