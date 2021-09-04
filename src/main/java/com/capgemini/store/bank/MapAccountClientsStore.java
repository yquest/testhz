package com.capgemini.store.bank;


import com.capgemini.cdao.bank.AccountCDAO;
import com.capgemini.cdao.bank.ClientCDAO;
import com.hazelcast.map.MapStore;

import java.util.*;

public class MapAccountClientsStore implements MapStore<ClientAccount, Boolean> {
    private final ClientCDAO clientCDAO;
    private final AccountCDAO accountCDAO;

    public MapAccountClientsStore(ClientCDAO clientCDAO, AccountCDAO accountCDAO) {
        this.clientCDAO = clientCDAO;
        this.accountCDAO = accountCDAO;
    }

    @Override
    public void store(ClientAccount clientAccount, Boolean value) {
        System.out.println("store client account:" + clientAccount + " value:" + value);
        if (value) {
            clientCDAO.addAccountPermission(clientAccount.getClientId(), clientAccount.getAccountId());
            accountCDAO.addClient(clientAccount.getClientId(), clientAccount.getAccountId());
        } else {
            clientCDAO.deleteAccountPermission(clientAccount.getClientId(), clientAccount.getAccountId());
            accountCDAO.removeClient(clientAccount.getClientId(), clientAccount.getAccountId());
        }
    }

    @Override
    public void storeAll(Map<ClientAccount, Boolean> map) {
        System.out.println("store all clients accounts:" + map);
        Map<Integer, Set<Integer>> clientsToAdd = new HashMap<>();
        Map<Integer, Set<Integer>> clientsToDelete = new HashMap<>();
        for (Map.Entry<ClientAccount, Boolean> entry : map.entrySet()) {
            if (entry.getValue()) {
                clientCDAO.addAccountPermission(entry.getKey().getClientId(), entry.getKey().getAccountId());
                clientsToAdd.computeIfAbsent(entry.getKey().getAccountId(), e -> new HashSet<>())
                        .add(entry.getKey().getClientId());
            } else {
                clientsToDelete.computeIfAbsent(entry.getKey().getAccountId(), e -> new HashSet<>())
                        .add(entry.getKey().getClientId());
            }
        }
        for (Map.Entry<Integer, Set<Integer>> entry : clientsToAdd.entrySet()) {
            accountCDAO.addClients(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Integer, Set<Integer>> entry : clientsToDelete.entrySet()) {
            accountCDAO.removeClients(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void delete(ClientAccount clientAccount) {
        System.out.println("delete client account:" + clientAccount);
        accountCDAO.removeClients(clientAccount.getClientId(), Collections.singleton(clientAccount.getAccountId()));
        clientCDAO.deleteAccountPermission(clientAccount.getClientId(), clientAccount.getAccountId());
    }

    @Override
    public void deleteAll(Collection<ClientAccount> set) {
        System.out.println("delete all clients accounts:" + set);
        Map<Integer, Set<Integer>> clients = new HashMap<>();
        for (ClientAccount clientAccount : set) {
            clientCDAO.addAccountPermission(clientAccount.getClientId(), clientAccount.getAccountId());
            clients.computeIfAbsent(clientAccount.getAccountId(), (e) -> new HashSet<>())
                    .add(clientAccount.getClientId());
        }
        for (Map.Entry<Integer, Set<Integer>> entry : clients.entrySet()) {
            accountCDAO.addClients(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Boolean load(ClientAccount key) {
        final Integer accountId = key.getAccountId();
        boolean matchInDB = clientCDAO.getClientAccountsById(key.getClientId())
                .anyMatch(id -> accountId.hashCode() == id.hashCode() && accountId.equals(id));
        System.out.printf("load client account %3d: client %d %s\n", key.getAccountId(), key.getClientId(), matchInDB);
        if (matchInDB) {
            return true;
        }
        return null;
    }

    @Override
    public Map<ClientAccount, Boolean> loadAll(Collection<ClientAccount> keys) {
        return Collections.emptyMap();
    }

    @Override
    public Iterable<ClientAccount> loadAllKeys() {
        return Collections.emptySet();
    }
}
