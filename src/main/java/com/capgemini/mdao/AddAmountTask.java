package com.capgemini.mdao;

import com.capgemini.client.AccountException;
import com.capgemini.testhz.BankConstants;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.map.IMap;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class AddAmountTask implements Callable<Map.Entry<Long,String>>, Serializable, HazelcastInstanceAware {
    private static final long serialVersionUID = -304471841936978820L;
    private final int account;
    private final long amount;
    private HazelcastInstance hazelcast;

    public AddAmountTask(int account, long amount) {
        this.account = account;
        this.amount = amount;
    }

    @Override
    public Map.Entry<Long,String> call() {
        IMap<Integer, Long> map = hazelcast.getMap(BankConstants.ACCOUNT_AMOUNT);
        Long current = map.get(account);
        if (current == null) {
            return new AbstractMap.SimpleEntry<>(null,AccountException.Code.ACCOUNT_NOT_EXISTS.name());
        }
        long total = current + amount;
        if (total < 0) {
            return new AbstractMap.SimpleEntry<>(null,AccountException.Code.NEGATIVE_AMOUNT.name());
        }
        System.out.println(account + ":" + hazelcast.getName());
        map.set(account, total);
        return new AbstractMap.SimpleEntry<>(total,null);
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcast = hazelcastInstance;
    }
}
