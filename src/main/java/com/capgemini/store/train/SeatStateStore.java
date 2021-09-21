package com.capgemini.store.train;

import com.capgemini.cdao.train.SeatState;
import com.capgemini.cdao.train.SeatStateCDAO;
import com.capgemini.entity.train.SeatKey;
import com.hazelcast.map.MapStore;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


public class SeatStateStore implements MapStore<SeatKey, SeatState> {
    private final SeatStateCDAO seatStateCDAO;
    private final MapLoaderPrinter<SeatKey,SeatState> mapLoaderPrinter = new MapLoaderPrinter<>(this.getClass());

    public SeatStateStore(SeatStateCDAO seatStateCDAO) {
        this.seatStateCDAO = seatStateCDAO;
    }

    @Override
    synchronized public SeatState load(SeatKey key) {
        mapLoaderPrinter.load(key);
        return seatStateCDAO.getSeatState(key).orElse(null);
    }

    @Override
    synchronized public Map<SeatKey, SeatState> loadAll(Collection<SeatKey> collection) {
        mapLoaderPrinter.loadAll(collection);
        try {
            return collection.stream().map(e ->
                            seatStateCDAO.getSeatState(e)
                                    .map(s -> Pair.of(e, s))
                                    .orElse(null)
                    ).filter(Objects::nonNull)
                    .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    synchronized public Iterable<SeatKey> loadAllKeys() {
        mapLoaderPrinter.loadAllKeys();
        return Collections.emptyList();
    }

    @Override
    synchronized public void store(SeatKey seatKey, SeatState seatState) {
        mapLoaderPrinter.store(seatKey,seatState);
        seatStateCDAO.updateState(seatKey,seatState);
    }

    @Override
    synchronized public void storeAll(Map<SeatKey, SeatState> map) {
        mapLoaderPrinter.store(map);
        seatStateCDAO.updateStates(map);
    }

    @Override
    synchronized public void delete(SeatKey seatKey) {
        mapLoaderPrinter.deleteKey(seatKey);
        seatStateCDAO.delete(seatKey);
    }

    @Override
    synchronized public void deleteAll(Collection<SeatKey> collection) {
        mapLoaderPrinter.deleteKeys(collection);
        seatStateCDAO.delete(collection);
    }
}
