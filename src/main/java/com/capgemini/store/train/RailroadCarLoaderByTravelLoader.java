package com.capgemini.store.train;

import com.capgemini.cdao.train.RailroadCarTravelCDAO;
import com.capgemini.entity.train.TravelKey;
import com.hazelcast.map.MapLoader;

import java.util.*;
import java.util.stream.Collectors;


public class RailroadCarLoaderByTravelLoader implements MapLoader<TravelKey, List<Long>> {
    private final RailroadCarTravelCDAO railroadCarTravelCDAO;
    private final MapLoaderPrinter<TravelKey,List<Long>> mapLoaderPrinter = new MapLoaderPrinter<>(this.getClass());

    public RailroadCarLoaderByTravelLoader(RailroadCarTravelCDAO railroadCarTravelCDAO) {
        this.railroadCarTravelCDAO = railroadCarTravelCDAO;
    }

    @Override
    synchronized public List<Long> load(TravelKey key) {
        mapLoaderPrinter.load(key);
        return railroadCarTravelCDAO.getRailroadCarIds(key.getRoute(), key.getStart())
                .collect(Collectors.toList());
    }

    @Override
    synchronized public Map<TravelKey, List<Long>> loadAll(Collection<TravelKey> collection) {
        mapLoaderPrinter.loadAll(collection);
        return Collections.emptyMap();
    }

    @Override
    synchronized public Iterable<TravelKey> loadAllKeys() {
        mapLoaderPrinter.loadAllKeys();
        return Collections.emptyList();
    }

}
