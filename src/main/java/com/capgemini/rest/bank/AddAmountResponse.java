package com.capgemini.rest.bank;

public class AddAmountResponse {
    private final Long balance;
    private final String error;

    public AddAmountResponse(Long balance, String error) {
        this.balance = balance;
        this.error = error;
    }

    public Long getBalance() {
        return balance;
    }

    public String getError() {
        return error;
    }
}
