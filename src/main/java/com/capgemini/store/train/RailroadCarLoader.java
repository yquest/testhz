package com.capgemini.store.train;

import com.capgemini.cdao.train.RailroadCarCDAO;
import com.capgemini.entity.train.RailroadCar;
import com.hazelcast.map.MapLoader;

import java.util.*;


public class RailroadCarLoader implements MapLoader<Long, RailroadCar> {
    private final RailroadCarCDAO railroadCarCDAO;
    private final MapLoaderPrinter<Long, RailroadCar> mapLoaderPrinter = new MapLoaderPrinter<>(this.getClass());

    public RailroadCarLoader(RailroadCarCDAO railroadCarCDAO) {
        this.railroadCarCDAO = railroadCarCDAO;
    }

    /**
     * select travel_type, seat_places, travel from railroad_car where id = ?
     *
     * @param id railroad_car id
     * @return set of strings
     */
    @Override
    synchronized public RailroadCar load(Long id) {
        mapLoaderPrinter.load(id);
        return railroadCarCDAO.getRailroadCar(id).orElse(null);
    }

    @Override
    synchronized public Map<Long, RailroadCar> loadAll(Collection<Long> collection) {
        mapLoaderPrinter.loadAll(collection);
        return new HashMap<>();
    }

    @Override
    synchronized public Iterable<Long> loadAllKeys() {
        mapLoaderPrinter.loadAllKeys();
        return Collections.emptyList();
    }

}
