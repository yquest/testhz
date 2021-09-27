package com.capgemini.store.train;

import com.capgemini.cdao.train.SeatState;
import com.capgemini.entity.train.SeatKey;
import com.capgemini.entity.train.SeatPlace;
import com.capgemini.testhz.train.DataResolver;
import com.hazelcast.core.HazelcastInstance;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SeatMultiStore implements DataResolver {
    private final HazelcastInstance hazelcast;

    public SeatMultiStore(HazelcastInstance hazelcast) {
        this.hazelcast = hazelcast;
    }

    public static RailroadCarTravelKey toRailroadTravelKey(SeatKey seatKey) {
        return new RailroadCarTravelKey(
                seatKey.getRoute(),
                seatKey.getStart(),
                seatKey.getRailroadCar()
        );
    }

    public static SeatPlace toSeatPlace(SeatKey seatKey, SeatState seatState) {
        return new SeatPlace(seatKey.getStation(), seatKey.getSeatPlace(), seatState);
    }

    public static SeatKey toSeatKey(RailroadCarTravelKey railroadCarTravelKey, SeatPlace seatPlace) {
        return new SeatKey(railroadCarTravelKey.getRoute(), railroadCarTravelKey.getStart(), railroadCarTravelKey.getRailroadCar(), seatPlace.getSeatPlace(), seatPlace.getStation());
    }

    public static Predicate<SeatPlace> removeBySeatKeyIterable(Iterable<SeatKey> seatKeyIterable) {
        return seatPlace -> {
            for (SeatKey seatKey : seatKeyIterable) {
                if (seatKey.getSeatPlace().equals(seatPlace.getSeatPlace()) && seatKey.getStation().equals(seatPlace.getStation())) {
                    return true;
                }
            }
            return false;
        };
    }

    @Override
    public HazelcastInstance getHazelcast() {
        return hazelcast;
    }

    public void deleteCarOnTravel(RailroadCarTravelKey railroadCarTravelKey) {
        List<SeatKey> seatKeys = getSeatsByRailroadCarMap().get(railroadCarTravelKey).stream()
                .map(e -> toSeatKey(railroadCarTravelKey, e))
                .collect(Collectors.toList());
        seatKeys.forEach(getSeatStateMap()::lock);
        getSeatsByRailroadCarMap().lock(railroadCarTravelKey);
        try {
            seatKeys.forEach(getSeatStateMap()::delete);
        } finally {
            getSeatsByRailroadCarMap().unlock(railroadCarTravelKey);
            seatKeys.forEach(getSeatStateMap()::unlock);
        }
    }

}
