package com.capgemini.entity.train;

import com.capgemini.dto.Address;

import java.time.LocalDate;
import java.util.Objects;

public class User {
    private final Address address;
    private final Long id;
    private final LocalDate birthDate;
    private final String name;

    public User(Address address, Long id, LocalDate birthDate, String name) {
        this.address = address;
        this.id = id;
        this.birthDate = birthDate;
        this.name = name;
    }

    public Address getAddress() {
        return address;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(address, user.address) && Objects.equals(id, user.id) && Objects.equals(birthDate, user.birthDate) && Objects.equals(name, user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, id, birthDate, name);
    }

    @Override
    public String toString() {
        return "User{" +
                "address=" + address +
                ", id=" + id +
                ", birthDate=" + birthDate +
                ", name='" + name + '\'' +
                '}';
    }
}
