package com.capgemini.entity.train;

import java.io.Serializable;
import java.util.Objects;

public class Ticket implements Serializable {
    private static final long serialVersionUID = 6357997379843378415L;
    private final Long userId;
    private final TicketState state;
    private final String endStation;
    private final int price;

    public Ticket(Long userId, TicketState state, String endStation, int price) {
        this.userId = userId;
        this.state = state;
        this.endStation = endStation;
        this.price = price;
    }

    public Long getUserId() {
        return userId;
    }

    public TicketState getState() {
        return state;
    }

    public String getEndStation() {
        return endStation;
    }

    public int getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ticket ticket = (Ticket) o;
        return price == ticket.price && Objects.equals(userId, ticket.userId) && state == ticket.state && Objects.equals(endStation, ticket.endStation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, state, endStation, price);
    }

    public Ticket createClone(TicketState state, int price){
        return new Ticket(this.getUserId(), state, this.getEndStation(), price);
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "userId=" + userId +
                ", state=" + state +
                ", endStation='" + endStation + '\'' +
                ", price=" + price +
                '}';
    }
}