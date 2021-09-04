package com.capgemini.rest.bank;

public class TransferAmountRequest {
    private final int sourceAccount;
    private final int destAccount;
    private final int clientAccount;

    private final long amount;

    public TransferAmountRequest(int sourceAccount, int destAccount, int clientAccount, long amount) {
        this.sourceAccount = sourceAccount;
        this.destAccount = destAccount;
        this.clientAccount = clientAccount;
        this.amount = amount;
    }

    public int getClientAccount() {
        return clientAccount;
    }

    public int getSourceAccount() {
        return sourceAccount;
    }

    public int getDestAccount() {
        return destAccount;
    }

    public long getAmount() {
        return amount;
    }

}
