package com.capgemini.store.train;

import com.capgemini.cdao.train.RouteCDAO;
import com.hazelcast.map.MapLoader;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class RouteLoaderPrices implements MapLoader<Long, List<Integer>> {
    private final RouteCDAO routeCDAO;
    private final MapLoaderPrinter<Long,List<Integer>> mapLoaderPrinter = new MapLoaderPrinter<>(this.getClass());

    public RouteLoaderPrices(RouteCDAO routeCDAO) {
        this.routeCDAO = routeCDAO;
    }

    @Override
    synchronized public List<Integer> load(Long id) {
        mapLoaderPrinter.load(id);
        return routeCDAO.getPrices(id).orElse(null);
    }

    @Override
    synchronized public Map<Long, List<Integer>> loadAll(Collection<Long> collection) {
        mapLoaderPrinter.loadAll(collection);
        return Collections.emptyMap();
    }

    @Override
    synchronized public Iterable<Long> loadAllKeys() {
        mapLoaderPrinter.loadAllKeys();
        return Collections.emptyList();
    }

}
