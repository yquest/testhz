package com.capgemini.rest.train;

import com.capgemini.entity.train.RailroadCar;

import java.util.List;

public class AddRailroadCars {
    private List<RailroadCar> railroadCars;

    public AddRailroadCars(List<RailroadCar> railroadCars) {
        this.railroadCars = railroadCars;
    }

    public List<RailroadCar> getRailroadCars() {
        return railroadCars;
    }
}
