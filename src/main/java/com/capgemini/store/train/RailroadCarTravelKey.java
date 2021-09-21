package com.capgemini.store.train;

import com.hazelcast.partition.PartitionAware;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public class RailroadCarTravelKey implements Serializable, PartitionAware<Integer> {
    private static final long serialVersionUID = -1051939928729642224L;
    private final long route;
    private final Instant start;
    private final long railroadCar;

    public RailroadCarTravelKey(long route, Instant start, long railroadCar) {
        this.route = route;
        this.start = start;
        this.railroadCar = railroadCar;
    }

    public long getRoute() {
        return route;
    }

    public Instant getStart() {
        return start;
    }

    public long getRailroadCar() {
        return railroadCar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RailroadCarTravelKey that = (RailroadCarTravelKey) o;
        return route == that.route && railroadCar == that.railroadCar && Objects.equals(start, that.start);
    }

    @Override
    public int hashCode() {
        return Objects.hash(route, start, railroadCar);
    }

    @Override
    public String toString() {
        return "RailroadCarSeatsKey{" +
                "route=" + route +
                ", start=" + start +
                ", railroad_car=" + railroadCar +
                '}';
    }

    @Override
    public Integer getPartitionKey() {
        return Objects.hash(route, start);
    }
}
