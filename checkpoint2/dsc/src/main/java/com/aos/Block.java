package com.aos;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Block implements Serializable {
    private static final long serialVersionUID = 1L;
    // Assuming the Transaction class is defined with a fixed size of 128 bytes
    public static final int TRANSACTION_SIZE = 128;

    // Block Size (unsigned integer 4B) - Java does not support unsigned types
    // natively
    private long blockSize; // Using long to cover the unsigned int range

    // Block Header (56B)
    private int version; // Version (unsigned short integer 2B) - Java short is 2 bytes
    private byte[] previousBlockHash; // Previous Block Hash (32B)
    private long blockID; // BlockID (unsigned integer 4B) - Using long for unsigned int
    private long timestamp; // Timestamp (signed integer 8B)
    private int difficultyTarget; // Difficulty Target (unsigned short integer 2B)
    private long nonce; // Nonce (unsigned integer 8B)

    // Transaction Counter (unsigned integer 4B)
    private long transactionCounter; // Using long for unsigned int

    // Reserved (64B)
    private byte[] reserved;

    // Array of Transactions (variable)
    private List<Transaction> transactions = new ArrayList<>();

    // Constructor
    public Block(int version, byte[] previousBlockHash, long blockID, long timestamp,
            int difficultyTarget, long nonce) {
        this.version = version;
        this.previousBlockHash = previousBlockHash;
        this.blockID = blockID;
        this.timestamp = timestamp;
        this.difficultyTarget = difficultyTarget;
        this.nonce = nonce;
        this.reserved = new byte[64]; // Initialize reserved space

        this.blockSize = calculateBlockSize();
    }

    public long getblockID() {
        return this.blockID;
    }

    // Method to add a transaction to the block
    public void addTransaction(List<Transaction> transaction) {
        for (Transaction t : transaction) {
            this.transactions.add(t);
        }

        transactionCounter = transactions.size();
        blockSize = calculateBlockSize();
    }

    public byte[] calhash1() {
        String x = new String(String.valueOf(this.version) + String.valueOf(this.previousBlockHash)
                + String.valueOf(this.blockID) + String.valueOf(this.timestamp) + String.valueOf(this.difficultyTarget)
                + String.valueOf(this.nonce) + String.valueOf(this.reserved.length)
                + String.valueOf(this.transactions.size()) + String.valueOf(this.blockSize));

        byte[] hash = new byte[32];
        Blake3 blake = Blake3.newInstance();
        blake.update(x.getBytes());
        hash = blake.digest();
        return hash;
    }

    public byte[] calhash() {
        try {
            // MessageDigest digest = MessageDigest.getInstance("SHA-256");
            Blake3 digest = Blake3.newInstance();
            // Properly encode each field into bytes
            digest.update(intToBytes(this.version));
            digest.update(this.previousBlockHash);
            digest.update(longToBytes(this.blockID));
            digest.update(longToBytes(this.timestamp));
            digest.update(intToBytes(this.difficultyTarget));
            digest.update(longToBytes(this.nonce));
            digest.update(this.reserved);

            // Handle transactions
            for (Transaction transaction : this.transactions) {
                digest.update(transaction.toString().getBytes(StandardCharsets.UTF_8));
            }

            // Return the hash as a byte array
            return digest.digest();
        } catch (Exception e) {
            throw new RuntimeException("Unable to find hash algorithm", e);
        }
    }

    private byte[] intToBytes(int value) {
        return new byte[] {
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value };
    }

    private byte[] longToBytes(long value) {
        return new byte[] {
                (byte) (value >>> 56),
                (byte) (value >>> 48),
                (byte) (value >>> 40),
                (byte) (value >>> 32),
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value };
    }

    // Method to calculate the block size
    private long calculateBlockSize() {
        // Header size (56B) + Transaction counter size (4B) + Reserved size (64B) +
        // Transactions size
        return 56 + 4 + 64 + (transactionCounter * TRANSACTION_SIZE);
    }

    public List<Transaction> getTransactions() {
        return this.transactions;
    }
}
