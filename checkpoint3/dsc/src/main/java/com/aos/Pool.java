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
            {
                return calltoBCInvid(transactionId);
            }
        // check in block chain

        String res= calltoBC(transactionId);

        return res;
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

    public String calltoBC(String transactionID) {
        String port = YamlConfigManager.getConfigValue("blockchain", "port");
        String IP = YamlConfigManager.getConfigValue("blockchain", "server");
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(IP, Integer.valueOf(port));
            BlockChainInf stub = (BlockChainInf) registry.lookup("BcService");
            String res =stub.getTxStatus(transactionID);
            return res;
            
        } catch (Exception e) {
            return "unable to connect to Block Chain";
        }

    }

    public String calltoBCInvid(String transactionID) {
        String port = YamlConfigManager.getConfigValue("blockchain", "port");
        String IP = YamlConfigManager.getConfigValue("blockchain", "server");
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(IP, Integer.valueOf(port));
            BlockChainInf stub = (BlockChainInf) registry.lookup("BcService");
            String res =stub.getInvalidTXStatus(transactionID);
            return res;
            
        } catch (Exception e) {
            return "unable to connect to Block Chain trx Invalid";
        }

    }

    public static void main(String args[]) {
        String port = YamlConfigManager.getConfigValue("pool", "port");
        // String IP = YamlConfigManager.getConfigValue("pool", "server");
        // System.out.println(IP + " > "+ port);
        Pool pserv;
        try {
            pserv = new Pool();
            String IP = YamlConfigManager.getConfigValue("pool", "server");
            System.setProperty("java.net.preferIPv4Stack", "true");
            System.setProperty("java.rmi.server.hostname",IP );
            Registry registry;
            registry = LocateRegistry.createRegistry(Integer.valueOf(port));
            registry.rebind("PoolService", pserv);
            System.out.println("Pool server is ready on IP "+IP);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println(
                    "Check the dsc-configfile existance and port details\n or the port mentioned is already in use");
        }

    }

    public void removedConfirmedTransactions(Block B, List<Transaction>inValidTransactions) {
        Runnable task = () -> {
            for (Transaction t : B.getTransactions()) {
                String encodedId = Base58.encode(t.getTransitionId());
                if (unConfirmed.containsKey(encodedId)) {
                    unConfirmed.remove(encodedId);
                }
            }
            for (Transaction t1 : inValidTransactions) {
                String encodedId = Base58.encode(t1.getTransitionId());
                if (unConfirmed.containsKey(encodedId)) {
                    unConfirmed.remove(encodedId);
                }
                inValid.put(encodedId,t1);
            }
        };

        // Start the task in a new thread
        new Thread(task).start();

    }

}
