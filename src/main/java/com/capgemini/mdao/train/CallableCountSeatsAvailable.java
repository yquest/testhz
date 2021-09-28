package com.capgemini.mdao.train;

import com.capgemini.cdao.train.SeatState;
import com.capgemini.entity.train.SeatPlace;
import com.capgemini.store.train.RailroadCarTravelKey;
import com.capgemini.testhz.train.DataResolver;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.Callable;

public class CallableCountSeatsAvailable implements HazelcastInstanceAware, Serializable, Callable<Integer> {
    private static final long serialVersionUID = 6971189715621739672L;
    private final RailroadCarTravelKey key;
    private final String station;
    transient private DataResolver dataResolver;

    public CallableCountSeatsAvailable(RailroadCarTravelKey key, String station) {
        this.key = key;
        this.station = station;
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcast) {
        dataResolver = () -> hazelcast;
    }

    @Override
    public Integer call() {
        Set<SeatPlace> seatPlaces = dataResolver.getSeatsByRailroadCarMap().get(key);
        if(seatPlaces == null){
            return 0;
        }
        return Math.toIntExact(seatPlaces.stream()
                .filter(e -> e.getStation().equals(station) && e.getSeatState() == SeatState.AVAILABLE)
                .count());
    }
}
