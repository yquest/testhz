package com.capgemini.cdao.train;

public enum SeatState {
    AVAILABLE, OCCUPIED, RESERVED;
    public static SeatState valueOfOrNull(String value){
        for (SeatState seatState:SeatState.values()){
            if(seatState.name().equals(value)){
                return seatState;
            }
        }
        return null;
    }
}