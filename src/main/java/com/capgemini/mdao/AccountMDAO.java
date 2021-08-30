package com.capgemini.mdao;

import com.hazelcast.core.HazelcastInstance;

import java.util.Map;

public interface AccountMDAO {
    void addAccountPermission(int clientId, int accountId);

    void removeAccountPermission(int clientId, int accountId);

    Map.Entry<Long,String> addAmount(int accountId, int clientId, long amount);

    enum Selector {
        LOCK, SUBMIT_TO_OWNER, TRANSACTION;

        public AccountMDAO createAccountMDAO(HazelcastInstance hazelcast) {
            switch (this) {
                case LOCK:
                    return new AccountMDAOLock(hazelcast);
                case SUBMIT_TO_OWNER:
                    return new AccountMDAOSubmitToOwner(hazelcast);
                case TRANSACTION:
                    return new AccountMDAOTransaction(hazelcast);
                default:
                    throw new IllegalStateException("unexpected");
            }
        }
    }
}
