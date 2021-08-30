package com.capgemini.mdao;

import com.capgemini.client.AccountException;
import com.capgemini.store.ClientAccount;
import com.capgemini.testhz.BankConstants;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;

import static com.capgemini.client.AccountException.Code.*;

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
    public Map.Entry<Long, String> addAmount(int accountId, int clientId, long amount) {
        IMap<Integer, Long> mapAmount = hazelcast.getMap(BankConstants.ACCOUNT_AMOUNT);
        IMap<ClientAccount, Boolean> mapAccountClients = hazelcast.getMap(BankConstants.ACCOUNT_CLIENTS);
        Boolean isAuthorized = Optional.ofNullable(mapAccountClients.get(new ClientAccount(accountId, clientId)))
                .orElse(false);
        if (!isAuthorized) {
            throw new AccountException(CLIENT_NOT_ALLOWED);
        }
        mapAmount.lock(accountId);
        try {
            Long computed;
            try {
                computed = mapAmount.computeIfPresent(accountId, (Integer k, Long v) -> {
                    final long total = v + amount;
                    System.out.printf("account:%3d current:%4d amount:%4d total:%4d\n", accountId, v, amount, total);
                    if (total < 0) {
                        throw new AccountException(NEGATIVE_AMOUNT, total);
                    }
                    return total;
                });
            } catch (AccountException e) {
                return new AbstractMap.SimpleEntry<>(null, e.getCode().name());
            }
            return new AbstractMap.SimpleEntry<>(computed, null);
        } finally {
            mapAmount.unlock(accountId);
        }
    }

    @Override
    public TransferResponse transferAmount(int accountSource, int clientId, int accountDestination, long amount) {
        IMap<Integer, Long> mapAmount = hazelcast.getMap(BankConstants.ACCOUNT_AMOUNT);
        IMap<ClientAccount, Boolean> mapAccountClients = hazelcast.getMap(BankConstants.ACCOUNT_CLIENTS);
        Boolean isAuthorized = Optional.ofNullable(mapAccountClients.get(new ClientAccount(accountSource, clientId)))
                .orElse(false);
        if (!isAuthorized) {
            return new TransferResponse(CLIENT_NOT_ALLOWED.name());
        }
        mapAmount.lock(accountSource);
        mapAmount.lock(accountDestination);
        try {
            Long sourceAmount = mapAmount.get(accountSource);
            Long destAmount = mapAmount.get(accountDestination);
            if (sourceAmount == null || destAmount == null) {
                return new TransferResponse(ACCOUNT_NOT_EXISTS.name());
            }
            if (sourceAmount - amount < 0) {
                return new TransferResponse(NEGATIVE_AMOUNT.name());
            }
            mapAmount.set(accountSource, sourceAmount - amount);
            mapAmount.set(accountDestination, destAmount + amount);
            return new TransferResponse(sourceAmount, destAmount);
        } finally {
            mapAmount.unlock(accountDestination);
            mapAmount.unlock(accountSource);
        }
    }

}
