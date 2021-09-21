package com.capgemini.store.train;

import com.capgemini.cdao.train.RouteCDAO;
import com.hazelcast.map.MapLoader;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class RouteLoaderStations implements MapLoader<Long, List<String>> {
    private final RouteCDAO routeCDAO;
    private final MapLoaderPrinter<Long,List<String>> mapLoaderPrinter = new MapLoaderPrinter<>(this.getClass());

    public RouteLoaderStations(RouteCDAO routeCDAO) {
        this.routeCDAO = routeCDAO;
    }

    @Override
    synchronized public List<String> load(Long id) {
        mapLoaderPrinter.load(id);
        return routeCDAO.getStations(id).orElse(null);
    }

    @Override
    synchronized public Map<Long, List<String>> loadAll(Collection<Long> collection) {
        mapLoaderPrinter.loadAll(collection);
        return Collections.emptyMap();
    }

    @Override
    synchronized public Iterable<Long> loadAllKeys() {
        mapLoaderPrinter.loadAllKeys();
        return Collections.emptyList();
    }

}
