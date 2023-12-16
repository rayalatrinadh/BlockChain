package com.aos;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Pool extends UnicastRemoteObject implements PoolInf {
    static Queue<Transaction> unprocessed;
    static Map<String, Transaction> unConfirmed;
    static Map<String, Transaction> inValid;

    public Pool() throws RemoteException {
        unprocessed = new ConcurrentLinkedQueue<>();
        unConfirmed = new ConcurrentHashMap<String, Transaction>();
        inValid = new ConcurrentHashMap<String, Transaction>();
    }

    public String addTransaction(Transaction Trx) {
        unprocessed.add(Trx);
        // System.out.println(Base58.encode(Trx.getTransitionId()) + " Queue size:" +
        // unprocessed.size());
        return "unProcessed";
    }

    public String Status(String transactionId) {
        for (Transaction T : unprocessed) {
            if (Base58.encode(T.getTransitionId()).equals(transactionId)) {
                return "unProcessed";
            }
        }

        if (unConfirmed.containsKey(transactionId))
            return "unConfirmed";
        if (inValid.containsKey(transactionId))
            return "inValid";

        return "unKnown";
    }

    public List<Transaction> getTransactionsForValidator() {
        List<Transaction> transactionsForValidator = new ArrayList<>();
        int maxTransactions = 8191;

        while (!unprocessed.isEmpty() && transactionsForValidator.size() < maxTransactions) {
            Transaction trx = unprocessed.poll(); // This also removes the transaction from the queue
            if (trx != null) {

                unConfirmed.put(Base58.encode(trx.getTransitionId()), trx);
                transactionsForValidator.add(trx);
            }
        }

        return transactionsForValidator;
    }

    public static void main(String args[]) {
        String port = YamlConfigManager.getConfigValue("pool", "port");
        // String IP = YamlConfigManager.getConfigValue("pool", "server");
        // System.out.println(IP + " > "+ port);
        Pool pserv;
        try {
            pserv = new Pool();
            Registry registry;
            registry = LocateRegistry.createRegistry(Integer.valueOf(port));
            registry.rebind("PoolService", pserv);
            System.out.println("Pool server is ready.");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println(
                    "Check the dsc-configfile existance and port details\n or the port mentioned is already in use");
        }

    }

    public void removedConfirmedTransactions(Block B) {
        Runnable task = () -> {
            for (Transaction t : B.getTransactions()) {
                String encodedId = Base58.encode(t.getTransitionId());
                if (unConfirmed.containsKey(encodedId)) {
                    unConfirmed.remove(encodedId);
                }
            }
        };

        // Start the task in a new thread
        new Thread(task).start();

    }

}
