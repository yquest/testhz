package com.capgemini.mdao;

import java.io.Serializable;

public class TransferResponse implements Serializable {
    private static final long serialVersionUID = -4128142530970348177L;
    private final Long sourceAmount;
    private final Long destAmount;
    private final String error;

    public TransferResponse(Long sourceAmount, Long destAmount) {
        this.sourceAmount = sourceAmount;
        this.destAmount = destAmount;
        this.error = null;
    }

    public TransferResponse(String error) {
        this.error = error;
        this.destAmount = null;
        this.sourceAmount = null;
    }

    public Long getSourceAmount() {
        return sourceAmount;
    }

    public Long getDestAmount() {
        return destAmount;
    }

    public String getError() {
        return error;
    }
}
