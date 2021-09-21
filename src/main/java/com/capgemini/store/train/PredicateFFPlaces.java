package com.capgemini.store.train;

import com.capgemini.cdao.train.SeatState;
import com.capgemini.entity.train.SeatKey;
import com.hazelcast.query.Predicate;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class PredicateFFPlaces implements Serializable, Predicate<SeatKey, SeatState> {
    private static final long serialVersionUID = 6729306607327375908L;
    private final RailroadCarTravelKey railroadCarTravelKey;
    private final List<String> stations;

    public PredicateFFPlaces(RailroadCarTravelKey railroadCarTravelKey, List<String> stations) {
        this.railroadCarTravelKey = railroadCarTravelKey;
        this.stations = stations;
    }

    @Override
    public boolean apply(Map.Entry<SeatKey, SeatState> entry) {
        return entry.getKey().getRailroadCar() == railroadCarTravelKey.getRailroadCar() &&
                entry.getKey().getRoute() == railroadCarTravelKey.getRoute() &&
                entry.getKey().getStart().equals(railroadCarTravelKey.getStart()) &&
                (entry.getKey().getSeatPlace().startsWith("RF") || entry.getKey().getSeatPlace().startsWith("LF")) &&
                entry.getValue() == SeatState.AVAILABLE &&
                stations.contains(entry.getKey().getStation());
    }
}
