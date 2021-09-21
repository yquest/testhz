package com.capgemini.entity.train;

import com.capgemini.cdao.train.SeatState;

import java.io.Serializable;
import java.util.Objects;

public class SeatPlace implements Serializable {
    private static final long serialVersionUID = -2904428047216059075L;
    private final String station;
    private final String seatPlace;
    private final SeatState seatState;

    public SeatPlace(String station, String seatPlace, SeatState seatState) {
        this.station = station;
        this.seatPlace = seatPlace;
        this.seatState = seatState;
    }

    public String getStation() {
        return station;
    }

    public String getSeatPlace() {
        return seatPlace;
    }

    public SeatState getSeatState() {
        return seatState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SeatPlace seatPlace1 = (SeatPlace) o;
        return Objects.equals(station, seatPlace1.station) && Objects.equals(seatPlace, seatPlace1.seatPlace) && seatState == seatPlace1.seatState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(station, seatPlace, seatState);
    }

    @Override
    public String toString() {
        return "SeatPlace{" +
                "station='" + station + '\'' +
                ", seatPlace='" + seatPlace + '\'' +
                ", seatState=" + seatState +
                '}';
    }

    public SeatPlace createSamePlaceWithState(SeatState seatState){
        return new SeatPlace(this.station, this.getSeatPlace(), seatState);
    }
}
