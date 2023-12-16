package com.aos;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    private final byte[] senderAddress; // 32 bytes
    private final byte[] recipientAddress; // 32 bytes
    private final double amount; // 8 bytes
    private final long timestamp; // 8 bytes
    private final byte[] transactionId; // 16 bytes
    private byte[] signature; // 32 bytes

    public Transaction(byte[] senderAddress, byte[] recipientAddress, byte[] transactionId, double amount,
            long timestamp) {
        this.senderAddress = Arrays.copyOf(senderAddress, 32);
        this.recipientAddress = Arrays.copyOf(recipientAddress, 32);
        this.amount = amount;
        this.timestamp = timestamp;
        this.transactionId = transactionId;// generateTransactionID();
        this.signature = new byte[32]; // Placeholder for actual signature
        generateSignature();
    }

    /*
     * @Deprecated
     * private byte[] genHashTransactionID() {
     * try {
     * MessageDigest digest = MessageDigest.getInstance("SHA-256");
     * byte[] id = digest.digest((Arrays.toString(senderAddress) +
     * Arrays.toString(recipientAddress) + amount + timestamp).getBytes());
     * return Arrays.copyOf(id, 16); // Truncate to 16 bytes
     * } catch (NoSuchAlgorithmException e) {
     * throw new RuntimeException("Unable to generate transaction ID", e);
     * }
     * }
     */
    @SuppressWarnings(value = { "unused" })
    private byte[] generateTransactionID() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        System.out.println("tan gen ID" + Base58.encode(bb.array()));
        return bb.array();
    }

    private void generateSignature() {
        try {
            // Signature signatureInstance = Signature.getInstance("SHA256withRSA");
            String privatekey_base58encoded = YamlConfigManager.getPrivateKeyValue("wallet", "private_key");
            byte[] privateKeyBytes = Base58.decode(privatekey_base58encoded);
            // PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            // KeyFactory keyFactory = KeyFactory.getInstance("RSA"); // Adjust the
            // algorithm as needed

            // PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            // signatureInstance.initSign(privateKey);
            // signatureInstance.update(getDataToSign());
            this.signature = privateKeyBytes;// signatureInstance.sign();
        } catch (Exception e) {
            throw new RuntimeException("Could not generate the signature", e);
        }
    }

    @SuppressWarnings(value = { "unused" })
    private byte[] getDataToSign() {

        ByteBuffer buffer = ByteBuffer.allocate(32 + 32 + 8 + 16); // Adjust the size as needed
        buffer.put(senderAddress);
        buffer.put(recipientAddress);
        buffer.putDouble(amount);
        buffer.put(transactionId);
        return buffer.array();
    }

    public byte[] getTransitionId() {
        return this.transactionId;
    }

    public byte[] getSignature() {
        return this.signature;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public byte[] getRecipient() {
        return this.recipientAddress;
    }

    public double getAmount() {
        return this.amount;
    }

    public byte[] getSender() {
        return this.senderAddress;
    }
}
