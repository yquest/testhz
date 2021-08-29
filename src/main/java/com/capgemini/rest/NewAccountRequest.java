package com.capgemini.rest;

import java.util.Set;

public class NewAccountRequest {
    private final int account;
    private final long amount;
    private final Set<Integer> clientIds;

    public NewAccountRequest(int account, long amount, Set<Integer> clientIds) {
        this.account = account;
        this.amount = amount;
        this.clientIds = clientIds;
    }

    public long getAmount() {
        return amount;
    }

    public Set<Integer> getClientIds() {
        return clientIds;
    }

    public int getAccount() {
        return account;
    }
}
