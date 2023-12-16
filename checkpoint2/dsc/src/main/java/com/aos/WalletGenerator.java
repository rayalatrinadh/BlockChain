package com.aos;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.IOException;

public class WalletGenerator {
    public static void main(String args[]) throws Exception {
        WalletGenerator w = new WalletGenerator();
        // w.send(34.0, "HtBTNpCt5fmPrvESqVp1UFsiX5wnMCtmgt7Cxi85MFiF");
        // w.send(34.0, "HtBTNpCt5fmPrvESqVp1UFsiX5wnMCtmgt7Cxi85MFkF");
        // w.trxstatus("KP7UkkZz6jrfbG5Qy9ZFdk");
        // WalletGenerator.key();
        // w.key();
        // w.getBalance();
    }

    private static byte[] publickey;
    private byte[] privatekey;
    // private String transactionId;
    List<Transaction> transactionList = new ArrayList<>();

    public WalletGenerator() {
        new YamlConfigManager();
        // load the public and private key
        String publickey_base58encoded = YamlConfigManager.getConfigValue("wallet", "public_key");
        String privatekey_base58encoded = YamlConfigManager.getPrivateKeyValue("wallet", "private_key");
        this.publickey = Base58.decode(publickey_base58encoded);
        this.privatekey = Base58.decode(privatekey_base58encoded);
        // if (publickey_base58encoded == null
        // ||publickey_base58encoded.isEmpty()||publickey_base58encoded.isBlank() ) {
        // System.out.println("public key unidentified");
        // goKeyPair();

        // } else if (privatekey_base58encoded == null ||
        // privatekey_base58encoded.isEmpty() || privatekey_base58encoded.isBlank()) {
        // System.out.println("private key unidentified");
        // goKeyPair();
        // } else {
        // //System.out.println("Wallet already exists at dsc-key.yaml, wallet create
        // aborted");
        // // System.out.println("publicKey:"+publickey_base58encoded);
        // // System.out.println("privateKey:"+privatekey_base58encoded);
        // }
        // load public key from dsc-config.yaml

    }

    public void create() {
        String publickey_base58encoded = YamlConfigManager.getConfigValue("wallet", "public_key");
        String privatekey_base58encoded = YamlConfigManager.getPrivateKeyValue("wallet", "private_key");
        // System.out.println("line 28 public key:"+publickey_base58encoded);
        // System.out.println("line 29 public key:"+privatekey_base58encoded);
        if (publickey_base58encoded == null || publickey_base58encoded.isEmpty() || publickey_base58encoded.isBlank()) {
            // System.out.println("public key unidentified");
            goKeyPair();

        } else if (privatekey_base58encoded == null || privatekey_base58encoded.isEmpty()
                || privatekey_base58encoded.isBlank()) {
            // System.out.println("private key unidentified");
            goKeyPair();
        } else {
            System.out.println("Wallet already exists at dsc-key.yaml, wallet create aborted");
            // System.out.println("publicKey:"+publickey_base58encoded);
            // System.out.println("privateKey:"+privatekey_base58encoded);
        }
        // load private key from dsc-key.yaml
    }

    public void key() {
        System.out.println("Reading dsc-config.yaml and dsc-key.yaml...");
        String publickey_base58encoded = YamlConfigManager.getConfigValue("wallet", "public_key");
        String privatekey_base58encoded = YamlConfigManager.getPrivateKeyValue("wallet", "private_key");
        if (publickey_base58encoded == null || publickey_base58encoded == "") {
            System.out.println("Error in finding key information, ensure that dsc-config.yaml and dsckey.\r\n" + //
                    "yaml exist and that they contain the correct information. You may need to run “./dsc wallet\r\n" + //
                    "create");

        } else if (privatekey_base58encoded == null || privatekey_base58encoded == "") {
            System.out.println("Error in finding key information, ensure that dsc-config.yaml and dsckey.\r\n" + //
                    "yaml exist and that they contain the correct information. You may need to run “./dsc wallet\r\n" + //
                    "create");

        } else {
            // System.out.println("Key already exist in .yaml files reading");
            System.out.println("DSC Public Address : " + publickey_base58encoded);
            System.out.println("DSC Private Address: " + privatekey_base58encoded);
        }

    }

    public void goKeyPair() {
        try {
            // Generate a new key pair
            KeyPair keys = generateKeyPair();

            // Get the encoded version of the public and private key
            byte[] publicKey = keys.getPublic().getEncoded();
            byte[] privateKey = keys.getPrivate().getEncoded();
            // System.out.println("here"+publicKey.length);
            // Print the keys to console
            // System.out.println("Public Key: " + publicKey);
            // System.out.println("Private Key: " + privateKey);

            String shapublicKey_base58 = getSHA256Hash(publicKey);

            String shaprivatekey_base58 = getSHA256Hash(privateKey);
            // Store the keys in a file
            // storeKeys(publicKeyBase58, privateKeyBase58, "wallet.cfg");

            byte[] b = Base58.decode(shaprivatekey_base58);
            // System.out.println("testPrivate Key: " +b+" >"+b.length );
            // System.out.println("testPrivate Key: " + shaprivatekey_base58.getBytes());
            // store the public and private keys to yaml files
            YamlConfigManager.updateConfigValue("wallet", "public_key", shapublicKey_base58);
            YamlConfigManager.updatePrivateKeyValue("wallet", "private_key", shaprivatekey_base58);
            System.out.println(
                    "New keys generated and stored, public key to dsc-config.yaml and private key to dsc-key.yaml in\r\n"
                            + //
                            "local folder");
            System.out.println("Public Key: " + shapublicKey_base58);
            System.out.println("Private Key: " + shaprivatekey_base58);
            this.publickey = Base58.decode(shapublicKey_base58);
            this.privatekey = Base58.decode(shaprivatekey_base58);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double getBalance() {
        // This would involve querying the blockchain, which requires a connection to
        // the blockchain network
        String port = YamlConfigManager.getConfigValue("blockchain", "port");
        String IP = YamlConfigManager.getConfigValue("blockchain", "server");
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(IP, Integer.valueOf(port));
            BlockChainInf stub = (BlockChainInf) registry.lookup("BcService");
            double response = stub.getBalance(publickey);
            System.out.println("Your Wallet Balce is: " + response + "for your Address:" + Base58.encode(publickey));
            return response;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("Ubable to reach Block-chain Server");
            return 0.00d;
        }

    }

    static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048, new SecureRandom());
        return generator.generateKeyPair();
    }

    @Deprecated
    private static void storeKeys(String publicKey, String privateKey, String fileName) throws IOException {
        String content = "public_key: " + publicKey + "\nprivate_key: " + privateKey;
        Files.write(Paths.get(fileName), content.getBytes());
    }

    private static String getSHA256Hash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // digest.update(data);
            byte[] hash = digest.digest(data);
            // System.out.println("sha value"+hash);
            return Base58.encode(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Couldn't find SHA-256 algorithm", e);
        }
    }

    public String send(double amount, String recipientAddress) throws Exception {
        double balance = getBalance();
        Transaction t = new Transaction(publickey, Base58.decode(recipientAddress), generateTransactionID(), balance,
                System.currentTimeMillis());
        String transactionId = Base58.encode(t.getTransitionId());
        System.out.println("Your Transaction ID: " + transactionId);
        // this.transactionId = transactionId;

        try {
            calltopool(t);
            this.transactionList.add(t);
            for (int i = 0; i < 3; i++) {
                Transaction tx = transactionList.get(transactionList.size() - 1);
                String transactionIdx = Base58.encode(tx.getTransitionId());
                Main.Print(trxstatus(transactionIdx));
                TimeUnit.SECONDS.sleep(1);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("Pool server Offline");
        }
        return transactionId;
        //
    }

    private byte[] generateTransactionID() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        // System.out.println("tan gen ID"+Base58.encode(bb.array()));
        return bb.array();
    }

    public void recentTransactions() {
        int i = 1;
        for (Transaction t : transactionList) {
            System.out.println("Transaction #" + i);
            String transactionIdx = Base58.encode(t.getTransitionId());
            System.out.print(": id" + transactionIdx);
            System.out.print(", status=" + trxstatus(transactionIdx));
            System.out.print(", timeStamp=" + t.getTimestamp());
            System.out.print(", coin = " + t.getAmount());
            System.out.print(", Source= " + Base58.encode(t.getSender()));
            System.out.print(", Receiver= " + Base58.encode(t.getRecipient()));
        }
    }

    public String calltopool(Transaction trx) throws Exception {
        try {
            String port = YamlConfigManager.getConfigValue("pool", "port");
            String IP = YamlConfigManager.getConfigValue("pool", "server");

            Registry registry = LocateRegistry.getRegistry(IP, Integer.valueOf(port));
            PoolInf stub = (PoolInf) registry.lookup("PoolService");
            String response = stub.addTransaction(trx);

            System.out.println("Response from server: " + response);

            // response = stub.Status("Ji4EiG2dhfMHjvEypkyV8H");
            // System.out.println("Response from server: " + response);
            // response = stub.Status("ScEz6JHXfLgzG57cKMGvh7");
            // System.out.println("Response from server: " + response);
            // return response;
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            // e.printStackTrace();
        }
        return "Error in calltoPool Server";
    }

    public String trxstatus(String transactionId) {
        String port = YamlConfigManager.getConfigValue("pool", "port");
        String IP = YamlConfigManager.getConfigValue("pool", "server");
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(IP, Integer.valueOf(port));
            PoolInf stub = (PoolInf) registry.lookup("PoolService");
            String response = stub.Status(transactionId);

            System.out.println("Response from server: " + response);
            return response;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println(e);// "Pool server Offline");
            return "empty";
        }

    }
}
