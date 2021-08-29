package com.capgemini.store;

import com.hazelcast.partition.PartitionAware;

import java.io.Serializable;
import java.util.Objects;

public class ClientAccount implements PartitionAware<Integer>, Serializable {

    private static final long serialVersionUID = 4186998070256898355L;

    private final int accountId;
    private final int clientId;

    public ClientAccount(int accountId, int clientId) {
        this.accountId = accountId;
        this.clientId = clientId;
    }

    @Override
    public Integer getPartitionKey() {
        return clientId;
    }

    public int getAccountId() {
        return accountId;
    }

    public int getClientId() {
        return clientId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientAccount that = (ClientAccount) o;
        return accountId == that.accountId && clientId == that.clientId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, clientId);
    }

    @Override
    public String toString() {
        return "ClientAccount{" +
                "accountId=" + accountId +
                ", clientId=" + clientId +
                '}';
    }
}
