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
        Map<TravelKey,List<Long>> map = new HashMap<>();
        for(TravelKey travelKey:collection){
            final List<Long> list = railroadCarTravelCDAO
                    .getRailroadCarIds(travelKey.getRoute(), travelKey.getStart())
                    .collect(Collectors.toList());
            map.put(travelKey, list);
        }
        return map;
    }

    @Override
    synchronized public Iterable<TravelKey> loadAllKeys() {
        mapLoaderPrinter.loadAllKeys();

        final List<TravelKey> list = railroadCarTravelCDAO.getTravelKeys().collect(Collectors.toList());
        System.out.printf("printed list of keys: %s%n", list);

        return list;
    }

}
