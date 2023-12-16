package com.aos;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockChain extends UnicastRemoteObject implements BlockChainInf {
    private byte[] currenthash = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 };
    List<Block> b;
    static int metronomePort = 10003;
    static String metronomeIP = "127.0.0.1";
    Block Genesisblock;
    private Map<String, Double> balanceCache = new HashMap<>();
    private Map<String, String> validTransaction = new HashMap<>();
    private Map<String, String> inValidTransaction = new HashMap<>();

    public BlockChain() throws RemoteException {
        b = new ArrayList<>();
        metronomePort = Integer.valueOf(YamlConfigManager.getConfigValue("metronome", "port"));
        metronomeIP = YamlConfigManager.getConfigValue("metronome", "server");
        if (b.size() == 0) {

            Genesisblock = new Block(1, currenthash, 0, System.currentTimeMillis(), 0, 0);
            b.add(Genesisblock);

            currenthash = Genesisblock.calhash();
            System.out.println("genesis block created");
            System.out.println("hash: genesis hash " + Base58.encode(currenthash));
        }
    }

    public void addblock(Block nb) {
        b.add(nb);
        System.out.println(
                "New block received from metronome, Block " + nb.getblockID() + " hash " + Base58.encode(nb.calhash()));
        sendSignalM();
    }

    public void addblock(Block nb, String publickey, List<Transaction> inValidTransactions) {
        b.add(nb);
        long blockID = nb.getblockID();
        long blockTime = nb.getBlockCreationTime();
        System.out.println(
                "New block received from validator, Block " + blockID + " hash " + Base58.encode(nb.calhash()));
        for (Transaction t : nb.getTransactions()) {
            String sender = Base58.encode(t.getSender());
            String receiver = Base58.encode(t.getRecipient());
            double amount = t.getAmount();

            // Update sender's balance
            balanceCache.put(sender, balanceCache.getOrDefault(sender, 100.0) - amount);

            // Update receiver's balance
            balanceCache.put(receiver, balanceCache.getOrDefault(receiver, 100.0) + amount);

            String valid = "valid";
            long txcreation = t.getTxCreationTime();
            double txTime = (blockTime - txcreation) / 1000.0;

            validTransaction.put(Base58.encode(t.getTransitionId()), valid + "_" + blockID + "_" + txcreation + "_"
                    + blockTime + "_" + String.format("%.6f", txTime) + " seconds");

        }
        for (Transaction inValid : inValidTransactions) {
            long txcreation = inValid.getTxCreationTime();
            double txTime = (blockTime - txcreation) / 1000.0;
            inValidTransaction.put(Base58.encode(inValid.getTransitionId()), "inValid" + "_" + blockID + "_"
                    + txcreation + "_" + blockTime + "_" + String.format("%.6f", txTime) + " seconds");
        }
        sendSignalPool(nb, inValidTransactions);
        sendSignalV(publickey);

    }

    public void sendSignalV(String publicKey) {
        try {
            Registry registry = LocateRegistry.getRegistry(metronomeIP, Integer.valueOf(metronomePort));
            MetronomeInf stub = (MetronomeInf) registry.lookup("MetronomeService");

            stub.sendSignalV(publicKey);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("Error in connection with metronome");
        }

    }

    public void sendSignalM() {
        try {
            Registry registry = LocateRegistry.getRegistry(metronomeIP, Integer.valueOf(metronomePort));
            MetronomeInf stub = (MetronomeInf) registry.lookup("MetronomeService");

            stub.sendSignalM();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("Error in connection with metronome");
        }

    }

    void sendSignalPool(Block nb, List<Transaction> inValidTransactions) {
        try {
            String port = YamlConfigManager.getConfigValue("pool", "port");
            String IP = YamlConfigManager.getConfigValue("pool", "server");

            Registry registry = LocateRegistry.getRegistry(IP, Integer.valueOf(port));
            PoolInf stub = (PoolInf) registry.lookup("PoolService");
            stub.removedConfirmedTransactions(nb, inValidTransactions);

        } catch (Exception e) {
            System.err.println("Unable to call Remove unconfirmed transactions in pool");
            // e.printStackTrace();
        }

    }

    public Block previousblock() {
        Block x = b.get(b.size() - 1);
        System.out.println("Block request from validator " + Base58.encode(x.calhash()));
        return x;
    }

    public double getBalance1(byte[] public_key) {
        return 7.8;
    }

    /*
     * public double getBalance(byte[] public_key) {
     * double balance = 0.0;
     * 
     * // Iterate through all blocks
     * for (Block block : this.b) {
     * // Iterate through all transactions in the block
     * for (Transaction transaction : block.getTransactions()) {
     * // Check if the public key is the recipient of the transaction
     * if (Arrays.equals(transaction.getRecipient(), public_key)) {
     * balance += transaction.getAmount(); // Add to balance
     * }
     * 
     * // Check if the public key is the sender of the transaction
     * if (Arrays.equals(transaction.getSender(), public_key)) {
     * balance -= transaction.getAmount(); // Subtract from balance
     * }
     * }
     * }
     * 
     * return balance;
     * }
     */

    public double getBalance(String walletAddress) {
        return balanceCache.getOrDefault(walletAddress, 100.0);
    }

    public String getTxStatus(String transactionID) {
        String res = validTransaction.get(transactionID);
        return res;
    }

    public String getInvalidTXStatus(String transactionID) {
        String res = inValidTransaction.get(transactionID);
        return res;
    }

    public static void main(String args[]) throws NumberFormatException, RemoteException {
        String port = YamlConfigManager.getConfigValue("blockchain", "port");
        String IP = YamlConfigManager.getConfigValue("blockchain", "server");
        BlockChain BcServ = new BlockChain();
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.rmi.server.hostname", IP);
        Registry registry = LocateRegistry.createRegistry(Integer.valueOf(port));
        registry.rebind("BcService", BcServ);
        System.out.println("BlockChain server is ready. IP: " + IP);
    }
}
