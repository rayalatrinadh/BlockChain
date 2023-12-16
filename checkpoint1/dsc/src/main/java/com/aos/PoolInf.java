package com.aos;

import java.rmi.Remote;

public interface PoolInf extends Remote{
    public String addTransaction(Transaction Trx) throws Exception;
    public String Status(String transactionId) throws Exception;
}
