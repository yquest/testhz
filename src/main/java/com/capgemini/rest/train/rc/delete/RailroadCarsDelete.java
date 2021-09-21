package com.capgemini.rest.train.rc.delete;

import com.capgemini.rest.train.seat.OnConflict;

import java.util.Map;

public class RailroadCarsDelete {
    private final OnConflict onConflict;
    private final Map<Long, OnConflict> railroadCarMap;
    private final boolean ignoreTravelState;

    public RailroadCarsDelete(OnConflict onConflict, Map<Long, OnConflict> railroadCarMap, boolean forceMaintenanceState) {
        this.onConflict = onConflict;
        this.railroadCarMap = railroadCarMap;
        this.ignoreTravelState = forceMaintenanceState;
    }

    public Map<Long, OnConflict> getRailroadCarMap() {
        return railroadCarMap;
    }

    public OnConflict getOnConflict() {
        return onConflict;
    }

    public boolean isIgnoreTravelState() {
        return ignoreTravelState;
    }
}