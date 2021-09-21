package com.capgemini.mdao.train;

import com.capgemini.cdao.train.SeatState;
import com.capgemini.entity.train.SeatKey;
import com.capgemini.entity.train.SeatPlace;
import com.capgemini.store.train.RailroadCarTravelKey;
import com.capgemini.testhz.train.TrainMapConstants;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.map.IMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

public class CallableFFSeatsAvailable implements HazelcastInstanceAware, Serializable, Callable<List<SeatKey>> {
    private static final long serialVersionUID = 6774854649986868429L;
    private final RailroadCarTravelKey key;
    private final List<String> stations;
    transient private IMap<RailroadCarTravelKey, Set<SeatPlace>> seatsByRailroadCar;

    public CallableFFSeatsAvailable(RailroadCarTravelKey key, List<String> stations) {
        this.key = key;
        this.stations = stations;
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcast) {
        seatsByRailroadCar = hazelcast.getMap(TrainMapConstants.SEATS_BY_RAILROAD_CAR);
    }

    @Override
    public List<SeatKey> call() {
        Set<SeatPlace> list = seatsByRailroadCar.get(key);
        System.out.printf("seatPlaces in key:%s %n %s%n", key, list);
        List<SeatKey> result = new ArrayList<>();
        for (SeatPlace seatPlace : list) {
            if ((seatPlace.getSeatPlace().startsWith("LF") ||
                    seatPlace.getSeatPlace().startsWith("RF")) &&
                    stations.contains(seatPlace.getStation()) &&
                    seatPlace.getSeatState() == SeatState.AVAILABLE
            ) {
                result.add(new SeatKey(
                        key.getRoute(),
                        key.getStart(),
                        key.getRailroadCar(),
                        seatPlace.getSeatPlace(),
                        seatPlace.getStation())
                );
            }
        }
        return result;
    }
}
