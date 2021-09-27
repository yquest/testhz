package com.capgemini.testhz.train;

import com.capgemini.cdao.train.SeatState;
import com.capgemini.entity.train.*;
import com.capgemini.store.train.RailroadCarTravelKey;
import com.capgemini.store.train.SeatMultiStore;
import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.flakeidgen.FlakeIdGenerator;
import com.hazelcast.map.IMap;
import com.hazelcast.topic.ITopic;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public interface DataResolver {

    HazelcastInstance getHazelcast();

    default IMap<TicketKey, Ticket> getTicketMap() {
        return getHazelcast().getMap(TrainMapConstants.TICKET);
    }

    default IMap<Long, List<String>> getRouteStationsMap() {
        return getHazelcast().getMap(TrainMapConstants.ROUTE_STATIONS);
    }

    default IMap<Long, List<Integer>> getRoutePricesMap() {
        return getHazelcast().getMap(TrainMapConstants.ROUTE_PRICES);
    }

    default IMap<TravelKey, List<Long>> getRailroadCarTravelMap() {
        return getHazelcast().getMap(TrainMapConstants.RAILROAD_CAR_TRAVEL);
    }

    default IMap<Long, List<Integer>> getRouteDelaysMap() {
        return getHazelcast().getMap(TrainMapConstants.ROUTE_DELAYS);
    }

    default int getRidePrice(long route, long railroadCar, int idxStart, int idxEnd) {
        List<Integer> prices = getRoutePricesMap().get(route);
        int sum = prices.subList(idxStart, idxEnd).stream().mapToInt(e -> e).sum();
        if (getRailroadCarMap().get(railroadCar).getTravelType().equals("economic")) {
            return (int) (sum * (1 - 0.1));
        }
        return sum;
    }

    default IMap<Long, RailroadCar> getRailroadCarMap() {
        return getHazelcast().getMap(TrainMapConstants.RAILROAD_CAR);
    }

    default IMap<String, String> getStationsMap() {
        return getHazelcast().getMap(TrainMapConstants.STATIONS);
    }

    default IMap<TravelKey, TravelState> getTravelMap() {
        return getHazelcast().getMap(TrainMapConstants.TRAVEL);
    }

    default IMap<SeatKey, SeatState> getSeatStateMap() {
        return getHazelcast().getMap(TrainMapConstants.SEAT_STATE);
    }

    default IMap<RailroadCarTravelKey, Set<SeatPlace>> getSeatsByRailroadCarMap() {
        return getHazelcast().getMap(TrainMapConstants.SEATS_BY_RAILROAD_CAR);
    }

    default FlakeIdGenerator getRailroadCarFlakeId() {
        return getHazelcast().getFlakeIdGenerator(TrainMapConstants.RAILROAD_CAR);
    }

    default FlakeIdGenerator getIdGenUser() {
        return getHazelcast().getFlakeIdGenerator(TrainMapConstants.USER);
    }

    default String replaceAllSeatState(List<SeatKey> seats, SeatState seatState) {
        final SeatState expectedPrevious;
        if (seatState == SeatState.OCCUPIED) {
            expectedPrevious = SeatState.RESERVED;
        } else if (seatState == SeatState.RESERVED) {
            expectedPrevious = SeatState.AVAILABLE;
        } else if (seatState == SeatState.AVAILABLE) {
            expectedPrevious = SeatState.RESERVED;
        } else {
            throw new IllegalStateException(String.format("Illegal seat state %s", seatState));
        }

        Map<RailroadCarTravelKey, List<SeatKey>> groupedByRCK = seats.stream()
                .collect(Collectors.groupingBy(SeatMultiStore::toRailroadTravelKey));

        final IMap<SeatKey, SeatState> seatStateMap = getSeatStateMap();
        for (Map.Entry<RailroadCarTravelKey, List<SeatKey>> keys : groupedByRCK.entrySet()) {
            for (SeatKey seatKey : keys.getValue()) {
                seatStateMap.lock(seatKey);
            }
        }
        try {
            for (Map.Entry<RailroadCarTravelKey, List<SeatKey>> keys : groupedByRCK.entrySet()) {
                for (SeatKey seatKey : keys.getValue()) {
                    SeatState currentState = seatStateMap.get(seatKey);
                    if (currentState != expectedPrevious) {
                        return String.format("to update the state to %s the old state must be %s", seatState, expectedPrevious);
                    }
                }
            }
            for (Map.Entry<RailroadCarTravelKey, List<SeatKey>> keys : groupedByRCK.entrySet()) {
                for (SeatKey seatKey : keys.getValue()) {
                    seatStateMap.set(seatKey, seatState);
                }
            }
        } finally {
            for (Map.Entry<RailroadCarTravelKey, List<SeatKey>> keys : groupedByRCK.entrySet()) {
                for (SeatKey seatKey : keys.getValue()) {
                    seatStateMap.unlock(seatKey);
                }
            }
        }

        for (Map.Entry<RailroadCarTravelKey, List<SeatKey>> entry : groupedByRCK.entrySet()) {
            Set<SeatPlace> set = getSeatsByRailroadCarMap().get(entry.getKey());
            set.removeIf(SeatMultiStore.removeBySeatKeyIterable(entry.getValue()));
            entry.getValue().forEach(e -> set.add(SeatMultiStore.toSeatPlace(e, seatState)));
            getSeatsByRailroadCarMap().set(entry.getKey(), set);
        }
        return "ok";
    }

}
