package com.capgemini.dto.bank.client;

public class Address {
    private final int postalCode;
    private final String street;
    private final String door;

    public Address(int postalCode, String street, String door) {
        this.postalCode = postalCode;
        this.street = street;
        this.door = door;
    }

    public int getPostalCode() {
        return postalCode;
    }

    public String getStreet() {
        return street;
    }

    public String getDoor() {
        return door;
    }

    @Override
    public String toString() {
        return "Address{" +
                "postalCode=" + postalCode +
                ", street='" + street + '\'' +
                ", door='" + door + '\'' +
                '}';
    }
}
