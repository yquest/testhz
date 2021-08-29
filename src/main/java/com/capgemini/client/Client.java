package com.capgemini.client;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.time.LocalDate;

public class Client {
    private final int id;
    private final String name;
    private final LocalDate birthDate;
    private final Address address;

    @JsonCreator
    public Client(int id, String name, LocalDate birthDate, Address address) {
        this.id = id;
        this.name = name;
        this.birthDate = birthDate;
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Address getAddress() {
        return address;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    @Override
    public String toString() {
        return "Client{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", birthDate=" + birthDate +
                ", address=" + address +
                '}';
    }
}
