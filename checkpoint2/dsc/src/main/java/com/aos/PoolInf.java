package com.aos;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface PoolInf extends Remote {
    public String addTransaction(Transaction Trx) throws Exception;

    public String Status(String transactionId) throws Exception;

    public List<Transaction> getTransactionsForValidator() throws Exception;

    public void removedConfirmedTransactions(Block B) throws RemoteException;
}
