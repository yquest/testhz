package com.capgemini.store;

import com.capgemini.cdao.AccountCDAO;
import com.hazelcast.map.MapStore;

import java.util.*;

public class MapAccountStore implements MapStore<Integer, Long> {
    private final AccountCDAO accountCDAO;

    public MapAccountStore(AccountCDAO accountCDAO) {
        this.accountCDAO = accountCDAO;
    }

    @Override
    public void store(Integer key, Long balance) {
        System.out.println("store balance " + key);
        accountCDAO.updateBalanceAccount(key, balance);
    }

    @Override
    public void storeAll(Map<Integer, Long> map) {
        System.out.println("storeAll balances call");
        for (Map.Entry<Integer, Long> balanceEntry : map.entrySet()) {
            System.out.println("  the balance in " + balanceEntry.getKey() + " account is " + balanceEntry.getValue());
            accountCDAO.updateBalanceAccount(balanceEntry.getKey(), balanceEntry.getValue());
        }
    }

    @Override
    public void delete(Integer key) {
        System.out.println("  delete the account balance ignored" + key);
    }

    @Override
    public void deleteAll(Collection<Integer> keys) {
        System.out.println("deleteAll balances call");
        for (Integer key : keys) {
            System.out.println("  delete the account " + key);
        }
    }

    @Override
    public Long load(Integer key) {
        System.out.println("  loading balance:" + key);
        return accountCDAO.getBalance(key).orElse(null);
    }

    @Override
    public Map<Integer, Long> loadAll(Collection<Integer> keys) {
        System.out.println("  loadingAll balances:" + keys);
        return new HashMap<>();
    }

    @Override
    public Iterable<Integer> loadAllKeys() {
        System.out.println("  loading all keys iterable");
        return new HashSet<>();
    }
}
