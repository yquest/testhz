package com.capgemini.store.train;

import com.capgemini.entity.train.SeatPlace;
import com.capgemini.cdao.train.SeatStateCDAO;
import com.hazelcast.map.MapLoader;

import java.util.*;
import java.util.stream.Collectors;


public class RailroadCarSeatsStatesLoader implements MapLoader<RailroadCarTravelKey, Set<SeatPlace>> {
    private final SeatStateCDAO seatStateCDAO;
    private final MapLoaderPrinter<RailroadCarTravelKey,Set<SeatPlace>> mapLoaderPrinter = new MapLoaderPrinter<>(this.getClass());

    public RailroadCarSeatsStatesLoader(SeatStateCDAO seatStateCDAO) {
        this.seatStateCDAO = seatStateCDAO;
    }

    @Override
    synchronized public Set<SeatPlace> load(RailroadCarTravelKey key) {
        mapLoaderPrinter.load(key);
        return seatStateCDAO.getSeatPlaces(key)
                .collect(Collectors.toSet());
    }

    @Override
    synchronized public Map<RailroadCarTravelKey, Set<SeatPlace>> loadAll(Collection<RailroadCarTravelKey> keys) {
        mapLoaderPrinter.loadAll(keys);
        return Collections.emptyMap();
    }

    @Override
    synchronized public Iterable<RailroadCarTravelKey> loadAllKeys() {
        mapLoaderPrinter.loadAllKeys();
        return Collections.emptyList();
    }

}
