package com.capgemini.mdao.account;

import com.capgemini.dto.bank.client.AccountException;
import com.capgemini.store.bank.ClientAccount;
import com.capgemini.testhz.bank.BankConstants;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionalMap;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;

import static com.capgemini.dto.bank.client.AccountException.Code.CLIENT_NOT_ALLOWED;
import static com.capgemini.dto.bank.client.AccountException.Code.NEGATIVE_AMOUNT;

public class AccountMDAOTransaction implements AccountMDAO {
    private final HazelcastInstance hazelcast;

    public AccountMDAOTransaction(HazelcastInstance hazelcast) {
        System.out.println("initialize " + getClass().getName());
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
        IMap<ClientAccount, Boolean> mapAccountClients = hazelcast.getMap(BankConstants.ACCOUNT_CLIENTS);
        Boolean isAuthorized = Optional.ofNullable(mapAccountClients.get(new ClientAccount(accountId, clientId)))
                .orElse(false);
        if (!isAuthorized) {
            throw new AccountException(CLIENT_NOT_ALLOWED);
        }

        TransactionContext tx = hazelcast.newTransactionContext();
        tx.beginTransaction();
        TransactionalMap<Integer, Long> map = tx.getMap(BankConstants.ACCOUNT_AMOUNT);
        Long v = map.get(accountId);
        try {
            final long total = v + amount;
            System.out.printf("account:%3d current:%4d amount:%4d total:%4d", accountId, v, amount, total);
            if (total < 0) {
                System.out.printf(" rollbacked :%4d\n", v);
                tx.rollbackTransaction();
                return new AbstractMap.SimpleEntry<>(null, NEGATIVE_AMOUNT.name());
            }
            System.out.printf(" commited   :%4d\n", total);
            map.set(accountId, total);
            tx.commitTransaction();
            return new AbstractMap.SimpleEntry<>(v, null);
        } catch (Exception e) {
            System.out.printf(" rollbacked :%4d\n", v);
            tx.rollbackTransaction();
            throw e;
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

        TransactionContext tx = hazelcast.newTransactionContext();
        tx.beginTransaction();
        TransactionalMap<Integer, Long> map = tx.getMap(BankConstants.ACCOUNT_AMOUNT);
        Long sourceAmount = map.get(accountSource);
        Long destAmount = map.get(accountDestination);
        try {
            System.out.printf("source:%3d dest:%4d amount:%4d amountSource:%4d amountDest:%4d",
                    accountSource,
                    accountDestination,
                    amount,
                    sourceAmount,
                    destAmount
            );
            final long totalSource = sourceAmount - amount;
            if (totalSource < 0) {
                System.out.printf(" rollbacked :%4d\n", totalSource);
                tx.rollbackTransaction();
                return new TransferResponse(NEGATIVE_AMOUNT.name());
            }
            final long totalDestination = destAmount + amount;
            System.out.printf(" commited   dest:%4d\n", totalDestination);
            map.set(accountSource, totalSource);
            map.set(accountDestination, totalDestination);
            tx.commitTransaction();
            return new TransferResponse(totalSource, totalDestination);
        } catch (Exception e) {
            System.out.printf(" rollbacked source:%4d dest:%4d\n", sourceAmount, destAmount);
            tx.rollbackTransaction();
            throw e;
        }
    }

}
