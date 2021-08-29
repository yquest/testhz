package com.capgemini.rest;

import java.util.UUID;

public class AddClientAccountRequest {
    private final UUID accountId;
    private final UUID clientId;
    private final Long initialAmount;

    public AddClientAccountRequest(UUID clientId, UUID accountId, Long initialAmount) {
        this.accountId = accountId;
        this.clientId = clientId;
        this.initialAmount = initialAmount;
    }

    public UUID getClientId() {
        return clientId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public Long getInitialAmount() {
        return initialAmount;
    }
}
