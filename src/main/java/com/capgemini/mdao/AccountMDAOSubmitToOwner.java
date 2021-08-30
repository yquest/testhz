package com.capgemini.mdao;

import com.capgemini.client.AccountException;
import com.capgemini.store.ClientAccount;
import com.capgemini.testhz.BankConstants;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static com.capgemini.client.AccountException.Code.*;

public class AccountMDAOSubmitToOwner implements AccountMDAO {
    private final HazelcastInstance hazelcast;

    public AccountMDAOSubmitToOwner(HazelcastInstance hazelcast) {
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
    public Map.Entry<Long,String> addAmount(int accountId, int clientId, long amount) {
        IMap<ClientAccount, Boolean> mapAccountClients = hazelcast.getMap(BankConstants.ACCOUNT_CLIENTS);
        Boolean isAuthorized = Optional.ofNullable(mapAccountClients.get(new ClientAccount(accountId, clientId)))
                .orElse(false);
        if (!isAuthorized) {
            throw new AccountException(CLIENT_NOT_ALLOWED);
        }

        try {
            return hazelcast.getExecutorService(BankConstants.ACCOUNT_AMOUNT)
                    .submitToKeyOwner(new AddAmountTask(accountId, amount), accountId)
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            throw new AccountException(UNEXPECTED, e);
        }
    }

}
