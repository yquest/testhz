package com.capgemini.entity.train;

import java.io.Serializable;
import java.util.Set;

public class RailroadCar implements Serializable {

    private static final long serialVersionUID = -4011799692478820823L;
    /**
     * defines type of travel touristic, economic or executive
     */
    private final String travelType;
    private final TravelKey travelKey;
    private final Set<String> seats;

    public RailroadCar(TravelKey travelKey, String travelType, Set<String> seats) {
        this.travelType = travelType;
        this.travelKey = travelKey;
        this.seats = seats;
    }

    public Set<String> getSeats() {
        return seats;
    }

    public String getTravelType() {
        return travelType;
    }

    public TravelKey getTravelKey() {
        return travelKey;
    }
}
