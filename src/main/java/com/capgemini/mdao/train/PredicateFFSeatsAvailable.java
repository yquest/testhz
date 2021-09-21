package com.capgemini.mdao.train;

import com.capgemini.cdao.train.SeatState;
import com.capgemini.entity.train.SeatKey;
import com.capgemini.store.train.RailroadCarTravelKey;
import com.hazelcast.query.Predicate;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class PredicateFFSeatsAvailable implements Serializable, Predicate<SeatKey, SeatState> {
    private static final long serialVersionUID = 6774854649986868429L;
    private final List<String> stations;

    public PredicateFFSeatsAvailable(List<String> stations) {
        this.stations = stations;
    }


    @Override
    public boolean apply(Map.Entry<SeatKey, SeatState> mapEntry) {
        final SeatKey key = mapEntry.getKey();
        return (key.getSeatPlace().startsWith("LF") ||
                key.getSeatPlace().startsWith("RF")) &&
                stations.contains(key.getStation()) &&
                mapEntry.getValue() == SeatState.AVAILABLE;
    }
}
