package com.capgemini.store.train;

import com.capgemini.cdao.train.TravelCDAO;
import com.capgemini.entity.train.TravelKey;
import com.capgemini.entity.train.TravelState;
import com.hazelcast.map.MapLoader;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;


public class TravelStateLoader implements MapLoader<TravelKey, TravelState> {
    private final TravelCDAO travelCDAO;
    private final MapLoaderPrinter<TravelKey,TravelState> mapLoaderPrinter = new MapLoaderPrinter<>(this.getClass());

    public TravelStateLoader(TravelCDAO routeCDAO) {
        this.travelCDAO = routeCDAO;
    }

    /**
     * select route, start, type_of_train from travel where route = ? and start = ?
     * @param key travel key
     * @return travel state
     */
    @Override
    synchronized public TravelState load(TravelKey key) {
        mapLoaderPrinter.load(key);
        return travelCDAO.getState(key.getRoute(), key.getStart()).orElse(null);
    }

    @Override
    synchronized public Map<TravelKey, TravelState> loadAll(Collection<TravelKey> collection) {
        mapLoaderPrinter.loadAll(collection);
        return Collections.emptyMap();
    }

    @Override
    synchronized public Iterable<TravelKey> loadAllKeys() {
        mapLoaderPrinter.loadAllKeys();
        return Collections.emptyList();
    }
}
