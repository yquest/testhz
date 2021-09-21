package com.capgemini.store.train;

import com.capgemini.cdao.train.RouteCDAO;
import com.hazelcast.map.MapLoader;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class RouteLoaderDelays implements MapLoader<Long, List<Long>> {
    private final RouteCDAO routeCDAO;
    private final MapLoaderPrinter<Long,List<Long>> mapLoaderPrinter = new MapLoaderPrinter<>(this.getClass());

    public RouteLoaderDelays(RouteCDAO routeCDAO) {
        this.routeCDAO = routeCDAO;
    }

    @Override
    synchronized public List<Long> load(Long key) {
        mapLoaderPrinter.load(key);
        return routeCDAO.getDelays(key).orElse(Collections.emptyList());
    }

    @Override
    synchronized public Map<Long, List<Long>> loadAll(Collection<Long> collection) {
        mapLoaderPrinter.loadAll(collection);
        return Collections.emptyMap();
    }

    @Override
    synchronized public Iterable<Long> loadAllKeys() {
        mapLoaderPrinter.loadAllKeys();
        return Collections.emptyList();
    }

}
