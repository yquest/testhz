package com.capgemini.mdao;

import com.capgemini.client.AccountException;
import com.capgemini.testhz.BankConstants;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.map.IMap;

import java.io.Serializable;
import java.util.concurrent.Callable;

public class AddAmountTask implements Callable<Long>, Serializable, HazelcastInstanceAware {
    private static final long serialVersionUID = -304471841936978820L;
    private final int account;
    private final long amount;
    private HazelcastInstance hazelcast;

    public AddAmountTask(int account, long amount) {
        this.account = account;
        this.amount = amount;
    }

    @Override
    public Long call() {
        IMap<Integer, Long> map = hazelcast.getMap(BankConstants.ACCOUNT_AMOUNT);
        Long current = map.get(account);
        if (current == null) {
            throw new AccountException(AccountException.Code.ACCOUNT_NOT_EXISTS);
        }
        long total = current + amount;
        if (total < 0) {
            throw new AccountException(AccountException.Code.NEGATIVE_AMOUNT);
        }
        System.out.println(account + ":" + hazelcast.getName());
        map.set(account, total);
        return total;
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcast = hazelcastInstance;
    }
}
