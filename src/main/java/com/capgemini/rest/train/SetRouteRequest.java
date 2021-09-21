package com.capgemini.rest.train;

import java.util.List;
import java.util.Objects;

public class SetRouteRequest {
    private final List<Integer> delays;
    private final List<Integer> prices;
    private final List<String> stations;

    public SetRouteRequest(List<Integer> delays, List<Integer> prices, List<String> stations) {
        this.delays = delays;
        this.prices = prices;
        this.stations = stations;
    }

    public List<Integer> getDelays() {
        return delays;
    }

    public List<String> getStations() {
        return stations;
    }

    public List<Integer> getPrices() {
        return prices;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SetRouteRequest that = (SetRouteRequest) o;
        return Objects.equals(delays, that.delays) && Objects.equals(prices, that.prices) && Objects.equals(stations, that.stations);
    }

    @Override
    public String toString() {
        return "SetRouteRequest{" +
                "delays=" + delays +
                ", prices=" + prices +
                ", stations=" + stations +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(delays, prices, stations);
    }
}