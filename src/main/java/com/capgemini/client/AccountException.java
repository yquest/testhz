package com.capgemini.client;

public class AccountException extends RuntimeException{
    public enum Code{
        ACCOUNT_NOT_EXISTS, NEGATIVE_AMOUNT, CLIENT_NOT_ALLOWED, UNEXPECTED
    }
    private final Code code;
    private final Object[] args;
    public AccountException(Code code, Object...args) {
        this.code = code;
        this.args = args;
    }

    public Code getCode() {
        return code;
    }

    public Object[] getArgs() {
        return args;
    }
}
