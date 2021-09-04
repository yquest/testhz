package com.capgemini.mdao.account;

import com.capgemini.dto.bank.client.AccountException;
import com.capgemini.store.bank.ClientAccount;
import com.capgemini.testhz.TestHZConstants;
import com.capgemini.testhz.bank.BankConstants;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static com.capgemini.dto.bank.client.AccountException.Code.*;

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
            return hazelcast.<Integer,Long>getMap(BankConstants.ACCOUNT_AMOUNT)
                    .submitToKey(accountId, new AddAmountTask(amount))
                    .toCompletableFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new AccountException(UNEXPECTED, e);
        }
    }

    @Override
    public TransferResponse transferAmount(int accountSource, int clientId, int accountDestination, long amount) {
        IMap<ClientAccount, Boolean> mapAccountClients = hazelcast.getMap(BankConstants.ACCOUNT_CLIENTS);
        Boolean isAuthorized = Optional.ofNullable(mapAccountClients.get(new ClientAccount(accountSource, clientId)))
                .orElse(false);
        if (!isAuthorized) {
            throw new AccountException(CLIENT_NOT_ALLOWED);
        }

        try {
            return hazelcast.getExecutorService(TestHZConstants.DEFAULT)
                    .submitToKeyOwner(new TransferAmountTask(accountSource, accountDestination, amount), accountSource)
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            throw new AccountException(UNEXPECTED, e);
        }
    }

}
