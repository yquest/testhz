package com.capgemini.mdao;

import com.capgemini.client.AccountException;
import com.capgemini.store.ClientAccount;
import com.capgemini.testhz.BankConstants;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import java.util.Optional;

import static com.capgemini.client.AccountException.Code.CLIENT_NOT_ALLOWED;
import static com.capgemini.client.AccountException.Code.NEGATIVE_AMOUNT;

public class AccountMDAOLock implements AccountMDAO {
    private final HazelcastInstance hazelcast;

    public AccountMDAOLock(HazelcastInstance hazelcast) {
        this.hazelcast = hazelcast;
    }

    @Override
    public void addAccountPermission(int clientId, int accountId) {
        IMap<ClientAccount, Boolean> mapAccountClients = hazelcast.getMap(BankConstants.ACCOUNT_CLIENTS);
        mapAccountClients.set(new ClientAccount(accountId, clientId), true);
    }

    @Override
    public void removeAccountPermission(int clientId, int accountId) {
        IMap<ClientAccount, Boolean> mapAccountClients = hazelcast.getMap(BankConstants.ACCOUNT_CLIENTS);
        mapAccountClients.remove(new ClientAccount(accountId, clientId));
    }

    @Override
    public Long addAmount(int accountId, int clientId, long amount) {
        IMap<Integer, Long> mapAmount = hazelcast.getMap(BankConstants.ACCOUNT_AMOUNT);
        IMap<ClientAccount, Boolean> mapAccountClients = hazelcast.getMap(BankConstants.ACCOUNT_CLIENTS);
        Boolean isAuthorized = Optional.ofNullable(mapAccountClients.get(new ClientAccount(accountId, clientId)))
                .orElse(false);
        if (!isAuthorized) {
            throw new AccountException(CLIENT_NOT_ALLOWED);
        }
        mapAmount.lock(accountId);
        try {
            return mapAmount.computeIfPresent(accountId, (Integer k, Long v) -> {
                final long total = v + amount;
                System.out.printf("account:%3d current:%4d amount:%4d total:%4d\n", accountId, v, amount, total);
                if (total < 0) {
                    throw new AccountException(NEGATIVE_AMOUNT, total);
                }
                return total;
            });
        } finally {
            mapAmount.unlock(accountId);
        }
    }

}
