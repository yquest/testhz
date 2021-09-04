package com.capgemini.rest.bank;

public class AddAmountRequest {
    private final int idAccount;
    private final int idClient;
    private final long amount;

    public AddAmountRequest(int idAccount, int idClient, long amount) {
        this.idAccount = idAccount;
        this.idClient = idClient;
        this.amount = amount;
    }

    public int getIdAccount() {
        return idAccount;
    }

    public long getAmount() {
        return amount;
    }

    public int getIdClient() {
        return idClient;
    }
}
