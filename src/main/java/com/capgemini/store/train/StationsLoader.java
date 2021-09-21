package com.capgemini.store.train;

import com.capgemini.cdao.train.StationsCDAO;
import com.hazelcast.map.MapLoader;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class StationsLoader implements MapLoader<String, String> {
    private final StationsCDAO stationsCDAO;
    private final MapLoaderPrinter<String,String> mapLoaderPrinter = new MapLoaderPrinter<>(this.getClass());

    public StationsLoader(StationsCDAO stationsCDAO) {
        this.stationsCDAO = stationsCDAO;
    }

    @Override
    synchronized public String load(String name) {
        mapLoaderPrinter.load(name);
        return stationsCDAO.getLabel(name).orElse(null);
    }

    @Override
    synchronized public Map<String, String> loadAll(Collection<String> collection) {
        mapLoaderPrinter.loadAll(collection);
        return new HashMap<>();
    }

    @Override
    synchronized public Iterable<String> loadAllKeys() {
        mapLoaderPrinter.loadAllKeys();
        return Collections.emptyList();
    }

}
