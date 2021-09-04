package com.capgemini.mdao.account;

import com.capgemini.dto.bank.client.AccountException;
import com.hazelcast.map.EntryProcessor;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Map;

public class AddAmountTask implements EntryProcessor<Integer, Long, Map.Entry<Long, String>>, Serializable {
    private static final long serialVersionUID = -304471841936978820L;
    private final long amount;

    public AddAmountTask(long amount) {
        this.amount = amount;
    }

    @Override
    public Map.Entry<Long, String> process(Map.Entry<Integer, Long> entry) {
        long total = entry.getValue() + amount;
        if (total < 0) {
            return new AbstractMap.SimpleEntry<>(null, AccountException.Code.NEGATIVE_AMOUNT.name());
        }
        System.out.println(entry.getKey() + ":" + total);
        entry.setValue(total);
        return new AbstractMap.SimpleEntry<>(total, null);
    }
}
