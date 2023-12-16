package com.aos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Block implements Serializable{
    private static final long serialVersionUID = 1L;
    // Assuming the Transaction class is defined with a fixed size of 128 bytes
    public static final int TRANSACTION_SIZE = 128;

    // Block Size (unsigned integer 4B) - Java does not support unsigned types natively
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
    private List<Transaction> transactions;

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
        this.transactions = new ArrayList<>();
        this.blockSize = calculateBlockSize();
    }
    public long getblockID()
    {
        return this.blockID;
    }
    // Method to add a transaction to the block
    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        transactionCounter = transactions.size();
        blockSize = calculateBlockSize();
    }

    public byte[] calhash()
    {
        String x= new String(String.valueOf(this.version)+String.valueOf(this.previousBlockHash)+String.valueOf(this.blockID)+String.valueOf( this.timestamp)+String.valueOf( this.difficultyTarget) + String.valueOf(this.nonce)+String.valueOf(this.reserved)+String.valueOf( this.transactions)+String.valueOf(this.blockSize));
       
        byte []hash = new byte[32];
        Blake3 blake = Blake3.newInstance();
        blake.update(x.getBytes());
        hash = blake.digest();
        return hash;
    }

    // Method to calculate the block size
    private long calculateBlockSize() {
        // Header size (56B) + Transaction counter size (4B) + Reserved size (64B) + Transactions size
        return 56 + 4 + 64 + (transactionCounter * TRANSACTION_SIZE);
    }

    // Transaction class
    public static class Transaction {
        // Transaction details (not fully implemented)
        // Assuming each Transaction is 128 bytes
        private byte[] data;

        public Transaction(byte[] data) {
            if (data.length > TRANSACTION_SIZE) {
                throw new IllegalArgumentException("Transaction data exceeds the allowed limit of " + TRANSACTION_SIZE + " bytes");
            }
            this.data = data;
        }

        // ... Other transaction-related methods
    }

    // Getters and setters for block fields
    // ...

    // Main method just for the sake of demonstration
    public static void main(String[] args) {
        // Example usage
        byte[] previousHash = new byte[32]; // Dummy hash for demonstration
        Block block = new Block(1, previousHash, 12345L, System.currentTimeMillis(), 1, 0L);

        // Add transactions to the block
        block.addTransaction(new Transaction(new byte[128])); // Add a transaction with dummy data
        block.addTransaction(new Transaction(new byte[128])); // Add another transaction

        // The block now contains two transactions and the size will reflect this
        System.out.println("Block size: " + block.blockSize + " bytes");
        System.out.println("Transaction count: " + block.transactionCounter);
    }
}
