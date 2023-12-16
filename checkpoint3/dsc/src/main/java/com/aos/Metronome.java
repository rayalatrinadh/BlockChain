package com.aos;

import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
//import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Metronome extends UnicastRemoteObject implements MetronomeInf {
    // set difficulty to 30
    // send difficulty to validator when requested
    private ConcurrentHashMap<String, ValidatorInfo> validators;
    static double rewards = 1024;
    private List<String> winners = new ArrayList<>();
    // create empty block for every 6 sec
    ExecutorService executor = Executors.newFixedThreadPool(200);
    private int difficulty;
    private int blockID = 1;
    private byte[] currenthash = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 };
    private static byte[] pkBytes;
    static String shapublicKey_base58;
    static int winnercount=0;
    Block b;

    public Metronome() throws RemoteException {
        validators = new ConcurrentHashMap<>();
        setDifficulty(30);
        // serverstart
        genPubKey();
        MetronomeStart();

    }

    public static String genPubKey() {
        try {
            KeyPair keys = generateKeyPair();
            pkBytes = keys.getPublic().getEncoded();

            shapublicKey_base58 = getSHA256Hash(pkBytes);

            return shapublicKey_base58;

        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }
    }

    static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048, new SecureRandom());
        return generator.generateKeyPair();
    }

    private static String getSHA256Hash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // digest.update(data);
            byte[] hash = digest.digest(data);
            System.out.println("sha value" + hash);
            return Base58.encode(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Couldn't find SHA-256 algorithm", e);
        }
    }

    public void getprevioushash() {
        String port = YamlConfigManager.getConfigValue("blockchain", "port");
        String IP = YamlConfigManager.getConfigValue("blockchain", "server");
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(IP, Integer.valueOf(port));
            BlockChainInf stub = (BlockChainInf) registry.lookup("BcService");
            Block prev = stub.previousblock();
            this.currenthash = prev.calhash();
            System.out.println("previous hash: " + Base58.encode(prev.calhash()) + " blockID" + prev.getblockID()+" difficulty: "+ difficulty);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.err.print("BlockChain Server unreachable");
        }

    }
static boolean winnerf=false;
    public void MetronomeStart() throws RemoteException {

        executor.submit(() -> {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    try {
                        if (winners.size() > 0 && !winnerf) {
                            winnercount++;
                            for (String winner : winners) {
                                // Connect to the winner and ask it to create a block
                                System.err.println("Winner" + winner + " IP: " + validators.get(winner).getIp()
                                        + "Port +" + validators.get(winner).getPort());
                                Registry registry = LocateRegistry.getRegistry(validators.get(winner).getIp(),
                                        validators.get(winner).getPort());
                                ValidatorInf validatorStub = (ValidatorInf) registry.lookup("validator");
                                validatorStub.createBlock();
                                if(winnercount>5){
                                difficulty = (difficulty + 1 > 30 ? difficulty + 1 : 30);
                                winnercount=0;
                                
                                }
                                winnerf = true;
                                break;
                            }
                        } else {
                            genBlock();
                            difficulty = (difficulty - 1 > 0 ? difficulty - 1 : 1);
                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            };

            Timer timer = new Timer();
            // Schedule the task to run every 6000 milliseconds (6 seconds)
            timer.scheduleAtFixedRate(task, 0, 6000);
        });

    }

    public boolean winner(byte[] pkBytes, byte[] fpBytes, int nonce, int threadId, int ndifficulty) {
        Blake3 blake = Blake3.newInstance();
        ByteBuffer buffer = ByteBuffer.allocate(fpBytes.length + pkBytes.length + 2 * Integer.BYTES); // space for

        // Pre-fill the buffer with fp, pk bytes and thread ID
        buffer.put(fpBytes);
        buffer.put(pkBytes);
        buffer.putInt(threadId); // Include threadId as part of the hash input
        buffer.putInt(nonce);
        byte[] toCompare = currenthash;

        blake.update(buffer.array());
        byte[] hash = blake.digest();
        if (compareByteArrays(hash, toCompare, ndifficulty)) {
            winners.add(Base58.encode(pkBytes));
            System.err.println("Winner " + true + " nonce " + nonce + " difficulty" + ndifficulty);
            return true;
        }
        System.err.println("Winner " + false);
        return false;
    }

    public boolean winner(String pk) {
        // pom
        winners.add(pk);
        return true;

    }

    public boolean compareByteArrays(byte[] array1, byte[] array2, int numberOfBits) {

        if (array1 == null || array2 == null) {
            return false;
        }

        if (numberOfBits < 0 || numberOfBits > array1.length * 8 || numberOfBits > array2.length * 8) {
            throw new IllegalArgumentException("Invalid number of bits");
        }

        int fullBytes = numberOfBits / 8;
        int remainingBits = numberOfBits % 8;

        // Compare full bytes
        for (int i = 0; i < fullBytes; i++) {
            if (array1[i] != array2[i]) {
                return false;
            }
        }

        // Compare remaining bits
        if (remainingBits > 0) {
            int mask = (1 << remainingBits) - 1;
            return (array1[fullBytes] & mask) == (array2[fullBytes] & mask);
        }

        return true;
    }

    public void setDifficulty(int diff) {
        this.difficulty = diff;
    }

    public int getDifficulty() {
        return this.difficulty;
    }

    public void genBlock() throws Exception {
        getprevioushash();
        b = new Block(1, currenthash, blockID++, System.currentTimeMillis(), this.difficulty, 0);
        currenthash = b.calhash();
        // System.out.println("New block created, hash: " + Base58.encode(currenthash));

        executor.submit(() -> {

            try {
                calltoBc(b);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        });

    }

    public void calltoBc(Block b2) {
        String port = YamlConfigManager.getConfigValue("blockchain", "port");
        String IP = YamlConfigManager.getConfigValue("blockchain", "server");
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(IP, Integer.valueOf(port));
            BlockChainInf stub = (BlockChainInf) registry.lookup("BcService");
            stub.addblock(b2);
            System.out.println("sent to blockchain");
            ;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // System.out.println("Blockchain Server Off-Line, Block is thrown away");
        }

    }

    public byte[] getCurrentHash() {
        return this.currenthash;
    }

    public void registerV(String ip, int port, String publicKey) {
        System.out.println("++++++++++Register v: +" + ip + " port " + port + " public Key " + publicKey);
        ValidatorInfo validatorInfo = new ValidatorInfo(ip, port);
        validators.put(publicKey, validatorInfo);
    }

    // new block created in metronome
    public void sendSignalV(String publicKey) throws RemoteException {
        sendSignalM();
        try {
            rewards(publicKey);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("Error at rewards");
        }
    }

    private void rewards(String publickey) throws Exception {
        if (rewards > 1) {
            double amount = rewards / winners.size();
            for (String w : winners) {
                Transaction t = new Transaction((new byte[]{ }), Base58.decode(w), generateTransactionID(),
                        amount, System.currentTimeMillis());
                calltopool(t);
            }
            winners.clear();
            winnerf=false;
        } else {
            double amount = 0.008;
            Transaction t = new Transaction(pkBytes, Base58.decode(publickey), generateTransactionID(), amount,
                    System.currentTimeMillis());
            calltopool(t);
            winners.clear();
            winnerf=false;
        }
        winners.clear();
        winnerf=false;
    }

    private byte[] generateTransactionID() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        // System.out.println("tan gen ID"+Base58.encode(bb.array()));
        return bb.array();
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
            System.err.println("Error in Pool Service");
            // e.printStackTrace();
            return "error in pool";
        }
        return "success";
    }

    public void sendSignalM() {
        rewards= 1024/Math.pow(2,(b.getblockID()/10));
        for (Map.Entry<String, ValidatorInfo> entry : validators.entrySet()) {
            try {
                // Todo: implement validator
                // String publicKey = entry.getKey();
                ValidatorInfo validatorInfo = entry.getValue();
                // Assuming a remote interface ValidatorInf and a method changeWorkMode
                Registry registry = LocateRegistry.getRegistry(validatorInfo.getIp(), validatorInfo.getPort());
                //System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+validatorInfo.getIp());
                ValidatorInf validatorStub = (ValidatorInf) registry.lookup("validator");
                validatorStub.changeWorkMode();

            } catch (Exception e) {
                // Handle exceptions, possibly logging them or taking other actions
                System.err.println("Error connecting to validator: " + e.getMessage());

            }
        }
    }

    public static void main(String args[]) throws RemoteException {
        Metronome mServ = new Metronome();
        // System.out.println(mServ.getDifficulty());

        String port = YamlConfigManager.getConfigValue("metronome", "port");
        String IP = YamlConfigManager.getConfigValue("metronome", "server");
        System.out.println(IP + " > " + port);
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.rmi.server.hostname", IP);

        Registry registry = LocateRegistry.createRegistry(Integer.valueOf(port));
        registry.rebind("MetronomeService", mServ);
        System.out.println("Metronome server is ready IP: " + IP);

    }

}

class ValidatorInfo {
    private String ip;
    private int port;

    public ValidatorInfo(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    // Getter for ip
    public String getIp() {
        return ip;
    }

    // Setter for ip
    public void setIp(String ip) {
        this.ip = ip;
    }

    // Getter for port
    public int getPort() {
        return port;
    }

    // Setter for port
    public void setPort(int port) {
        this.port = port;
    }
}
