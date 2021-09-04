package com.capgemini.mdao.account;

import com.capgemini.testhz.bank.BankConstants;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.map.IMap;

import java.io.Serializable;
import java.util.concurrent.Callable;

import static com.capgemini.dto.bank.client.AccountException.Code.ACCOUNT_NOT_EXISTS;
import static com.capgemini.dto.bank.client.AccountException.Code.NEGATIVE_AMOUNT;

public class TransferAmountTask implements Callable<TransferResponse>, Serializable, HazelcastInstanceAware {
    private static final long serialVersionUID = -304471841936978820L;
    private final int sourceAccount;
    private final int destAccount;
    private final long amount;
    private HazelcastInstance hazelcast;

    public TransferAmountTask(int sourceAccount, int destAccount, long amount) {
        this.sourceAccount = sourceAccount;
        this.destAccount = destAccount;
        this.amount = amount;
    }

    @Override
    public TransferResponse call() {
        IMap<Integer, Long> mapAmount = hazelcast.getMap(BankConstants.ACCOUNT_AMOUNT);
        mapAmount.lock(destAccount);
        mapAmount.lock(sourceAccount);
        try {
            Long sourceAmount = mapAmount.get(sourceAccount);
            Long destAmount = mapAmount.get(destAccount);
            if (sourceAmount == null || destAmount == null) {
                return new TransferResponse(ACCOUNT_NOT_EXISTS.name());
            }
            final long totalSource = sourceAmount - amount;
            if (totalSource < 0) {
                return new TransferResponse(NEGATIVE_AMOUNT.name());
            }
            mapAmount.set(sourceAccount, totalSource);
            final long totalDest = destAmount + amount;
            mapAmount.set(destAccount, totalDest);
            return new TransferResponse(totalSource, totalDest);
        } finally {
            mapAmount.unlock(sourceAccount);
            mapAmount.unlock(destAccount);
        }
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcast = hazelcastInstance;
    }
}
