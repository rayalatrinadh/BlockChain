package com.aos;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.rmi.AccessException;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Validator extends UnicastRemoteObject implements ValidatorInf {
    private static final AtomicBoolean foundMatch = new AtomicBoolean(false);
    // private static final AtomicBoolean createB = new AtomicBoolean(false);
    // private static final List<String> results = Collections.synchronizedList(new
    // ArrayList<>());
    int threads = 2;
    boolean pom;
    boolean pow;
    static int difficulty = 03;
    static byte[] search;
    static int fnonce = 9898;

    public Validator() throws Exception {
        System.out.println("DSC v-1.0");

        String enableValue2 = YamlConfigManager.getConfigValue("validator", "proof_pom", "enable");
        pom = "true".equals(enableValue2);
        System.out.println("pom" + pom);
        getDifficulty();
        System.out.println("Difficulty:" + difficulty);
        String enableValue1 = YamlConfigManager.getConfigValue("validator", "proof_pow", "enable");
        pow = "true".equals(enableValue1);
        System.out.println("pow" + pow);
        System.out.println("public key: " + getPublicKey());
        System.out.println("FingerPrint " + getFingerprint());
        System.out.println(register());

        if (pom) {
            this.threads = Integer.valueOf(YamlConfigManager.getConfigValue("validator", "proof_pom", "threads_hash"));
            System.out.println("Proof of Memory (" + threads + "-threads, 1GB RAM)");
            try {
                pom1(this.threads + 8);
            } catch (NoSuchAlgorithmException | InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else if (pow) {
            this.threads = Integer.valueOf(YamlConfigManager.getConfigValue("validator", "proof_pow", "threads_hash"));
            // "threads_hash"));
            System.out.println("Proof of work (" + threads + "-threads, 1GB RAM)");
            pow(this.threads);

        } else {
            System.out.println("Validator Problem with .Yaml file");
        }

    }

    // get finger print
    // generate fingerprint of 16 bytes
    public static String getFingerprint() {
        String fingerprint_58 = YamlConfigManager.getConfigValue("validator", "fingerprint");
        if (fingerprint_58.isBlank() || fingerprint_58.isEmpty()) {
            fingerprint_58 = genFingerPrint();
            try {
                YamlConfigManager.updateConfigValue("validator", "fingerprint", fingerprint_58);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return fingerprint_58;

    }

    public static String register() {
        String port = YamlConfigManager.getConfigValue("metronome", "port");
        String IP = YamlConfigManager.getConfigValue("metronome", "server");
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(IP, Integer.valueOf(port));
            MetronomeInf stub = (MetronomeInf) registry.lookup("MetronomeService");
            stub.registerV(currentIP(), Integer.valueOf(YamlConfigManager.getConfigValue("validator", "port")),
                    getPublicKey());
            return " Success";
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println(e);
            return ("Failed: Metronome offline");

        }
    }

    static String currentIP() throws UnknownHostException {
        String currentIP = InetAddress.getLocalHost().getHostAddress(); // Always server starts on the current host
        if (currentIP.startsWith("127."))
            try {
                currentIP = getLocalIPv4Address();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                System.err.println("Unable to get IP Address of Validator");
            }
        return currentIP;
    }

    public void createBlock() {
        String port = YamlConfigManager.getConfigValue("blockchain", "port");
        String IP = YamlConfigManager.getConfigValue("blockchain", "server");
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(IP, Integer.valueOf(port));
            BlockChainInf stub = (BlockChainInf) registry.lookup("BcService");
            Block prev = stub.previousblock();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    List<Transaction> fl = validateT();
                    Block b = new Block(1, prev.calhash(), prev.getblockID(), System.currentTimeMillis(),
                            difficulty, fnonce);
                    b.addTransaction(fl);
                    try {
                        stub.addblock(b, getPublicKey());
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }).start();
        } catch (Exception e) {

        }
    }

    List<Transaction> validateT() {
        List<Transaction> finallist = new ArrayList<>();
        try {
            String port = YamlConfigManager.getConfigValue("pool", "port");
            String IP = YamlConfigManager.getConfigValue("pool", "server");

            Registry registry = LocateRegistry.getRegistry(IP, Integer.valueOf(port));
            PoolInf stub = (PoolInf) registry.lookup("PoolService");
            List<Transaction> transactions = stub.getTransactionsForValidator();

            for (Transaction transaction : transactions) {
                if (isSenderEmpty(transaction)) {
                    // Handle valid transaction (metronome-given transaction)
                    finallist.add(transaction);
                } else {
                    // Apply usual validation logic
                    if (validateTransaction(transaction)) {
                        finallist.add(transaction);
                    }

                }
            }
            return finallist;

        } catch (Exception e) {
            System.err.println("Unable to connect to Pool server");
            return finallist;
        }

    }

    boolean validateTransaction(Transaction transaction) {
        String port = YamlConfigManager.getConfigValue("blockchain", "port");
        String IP = YamlConfigManager.getConfigValue("blockchain", "server");
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(IP, Integer.valueOf(port));
            BlockChainInf stub = (BlockChainInf) registry.lookup("BcService");
            double response = stub.getBalance(Base58.encode(transaction.getSender()));
            if (response > transaction.getAmount())
                return true;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("Ubable to reach Block-chain Server");
            return false;
        }
        return false;
    }

    private boolean isSenderEmpty(Transaction transaction) {
        byte[] sender = transaction.getSender();
        return sender == null || sender.length == 0;
    }

    public static String getLocalIPv4Address() throws Exception {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            if (iface.isLoopback() || !iface.isUp())
                continue;

            Enumeration<InetAddress> addresses = iface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();

                if (addr.isLoopbackAddress() || !(addr.getHostAddress().contains(".")))
                    continue; // This filters out IPv6 addresses

                return addr.getHostAddress();
            }
        }
        return null;
    }

    // Get difficulty from metronome
    public static void getDifficulty() {
        String port = YamlConfigManager.getConfigValue("metronome", "port");
        String IP = YamlConfigManager.getConfigValue("metronome", "server");
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(IP, Integer.valueOf(port));
            MetronomeInf stub = (MetronomeInf) registry.lookup("MetronomeService");
            int prev = stub.getDifficulty();
            difficulty = prev;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("Metronome Server is offline");

        }

    }

    public static String genFingerPrint() {
        UUID uuid = UUID.randomUUID();
        byte[] bytes = ByteBuffer.wrap(new byte[16])
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits())
                .array();
        return Base58.encode(bytes);
        // System.out.println(bytes.length);
        // System.out.println(new String(bytes));
        // System.out.println(Base58.encode(bytes));
        // System.out.println(new String(Base58.decode(Base58.encode(bytes))));
        // System.out.println(new String(Base58.decode(Base58.encode(bytes))).length());

    }
    // update fingerprint

    // get public key
    public static String getPublicKey() {
        String publicKey_58 = YamlConfigManager.getConfigValue("validator", "public_key");
        if (publicKey_58.isBlank() || publicKey_58.isEmpty()) {
            publicKey_58 = genPubKey();
            try {
                YamlConfigManager.updateConfigValue("validator", "public_key", publicKey_58);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return publicKey_58;
        }
        return publicKey_58;
    }

    public void changeWorkMode() {
        search = getSearchHash();
        foundMatch.set(true);
    }

    // generate public key
    public static String genPubKey() {
        try {
            KeyPair keys = generateKeyPair();
            byte[] publicKey = keys.getPublic().getEncoded();

            String shapublicKey_base58 = getSHA256Hash(publicKey);

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

    @SuppressWarnings(value = { "unused" })
    @Deprecated
    private static byte[] getSHA256modified(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // digest.update(data);
            byte[] hash = digest.digest(data);
            // System.out.println("sha value" + hash);
            return hash;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Couldn't find SHA-256 algorithm", e);
        }
    }
    // update public key

    // connect to block get hash

    // connect to metronum get difficulty

    // get pow or pom?

    // implement pow
    public static void pow(int numThreads) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(numThreads);
        search = getSearchHash();
        byte[] fpBytes = Base58.decode(getFingerprint());
        byte[] pkBytes = Base58.decode(getPublicKey());
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            // Schedule the task with a fixed delay
            executor.scheduleWithFixedDelay(() -> {
                try {
                    foundMatch.set(false);

                    generateHashes(threadId, search, fpBytes, pkBytes);
                } catch (Exception e) {
                    // Handle general exceptions
                    e.printStackTrace();
                }
            }, 0, 6, TimeUnit.SECONDS); // Start immediately, repeat every 6 seconds
        }

        // Shutdown logic can be handled as per your application's lifecycle
        // requirements
        // Note: The shutdown logic may need to be modified since tasks are now
        // scheduled to run repeatedly
    }
    /*
     * public static void pow(int numThreads) { //only for 6 seconds
     * ExecutorService executor = Executors.newFixedThreadPool(numThreads);
     * 
     * for (int i = 0; i < numThreads; i++) {
     * final int threadId = i;
     * executor.submit(() -> {
     * try {
     * generateHashes(threadId);
     * } catch (InvalidNativeOutput e) {
     * // TODO Auto-generated catch block
     * e.printStackTrace();
     * } catch (Exception e) {
     * // TODO Auto-generated catch block
     * e.printStackTrace();
     * }
     * });
     * }
     * 
     * executor.shutdown();
     * try {
     * if (!executor.awaitTermination(6, TimeUnit.SECONDS)) {
     * executor.shutdownNow();
     * }
     * } catch (InterruptedException e) {
     * executor.shutdownNow();
     * }
     * }
     */

    // connect to block chain and get hash
    public static byte[] getSearchHash() {
        {
            String port = YamlConfigManager.getConfigValue("blockchain", "port");
            String IP = YamlConfigManager.getConfigValue("blockchain", "server");
            Registry registry;
            try {
                registry = LocateRegistry.getRegistry(IP, Integer.valueOf(port));
                BlockChainInf stub = (BlockChainInf) registry.lookup("BcService");
                Block prev = stub.previousblock();
                return prev.calhash();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                System.out.println("Blockchain Server is offline");
                return new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, };
            }

        }
    }

    private static void generateHashes(int threadId, byte[] compare, byte[] fpBytes, byte[] pkBytes) throws Exception {

        // Allocate buffer with additional space for the thread ID
        ByteBuffer buffer = ByteBuffer.allocate(fpBytes.length + pkBytes.length + Integer.BYTES + Integer.BYTES);

        // Pre-fill the buffer with fp, pk bytes and thread ID
        buffer.put(fpBytes);
        buffer.put(pkBytes);
        buffer.putInt(fpBytes.length + pkBytes.length, threadId);

        long startTime = System.currentTimeMillis();
        long duration = 6000;
        byte[] toCompare = compare;// { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                                   // 21, 22, 23, 24 };
        System.out.println("searching for " + Base58.encode(toCompare));
        int nonce = 0;
        Blake3 blake = Blake3.newInstance();

        while (System.currentTimeMillis() - startTime < duration && !foundMatch.get()) {
            buffer.putInt(fpBytes.length + pkBytes.length + Integer.BYTES, nonce);
            blake.update(buffer.array());
            byte[] hash = blake.digest();
            // byte[] hash=getSHA256modified(buffer.array());
            // compareByteArrays(hash, toCompare, 30);
            if (compareByteArrays(hash, toCompare, difficulty)) {
                foundMatch.set(true);
                // results.add("Thread:" + threadId + "Nonce:" + nonce);
                fnonce = nonce;
                sendResultsToM(pkBytes, fpBytes, nonce, threadId);
                // long timeLeft = duration - (System.currentTimeMillis() - startTime);
                // if (timeLeft > 0) {
                // try {
                // Thread.sleep(timeLeft);
                // } catch (InterruptedException e) {
                // // TODO Auto-generated catch block
                // e.printStackTrace();
                // }
                // }
            }
            /*
             * NativeBLAKE3 blake3 = new NativeBLAKE3();
             * blake3.initDefault();
             * blake3.update(buffer.array());
             * byte[] hash = blake3.getOutput();
             */
            // compareByteArrays(hash, toCompare, 30);
            // System.out.println((blake.hexdigest()));
            // System.out.println(Arrays.toString(hash));
            nonce++;
        }

        System.out.println("Thread " + threadId + " Nonce: " + nonce);
    }

    // Implement or include your compareByteArrays, getFingerprint, getPublicKey,
    // and Base58.decode methods

    // private static void pow()
    // {
    // byte[] fpBytes = Base58.decode(getFingerprint());
    // byte[] pkBytes = Base58.decode(getPublicKey());

    // ByteBuffer buffer = ByteBuffer.allocate(fpBytes.length + pkBytes.length +
    // Integer.BYTES);

    // // Pre-fill the buffer with fp and pk bytes
    // buffer.put(fpBytes);
    // buffer.put(pkBytes);

    // // Start time
    // long startTime = System.currentTimeMillis();
    // // Duration to run the loop (6 seconds)
    // long duration = 6000;
    // byte[]
    // toCompare={1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24};
    // int nonce = 0;
    // Blake3 blake= Blake3.newInstance();
    // while (System.currentTimeMillis() - startTime < duration) {
    // // Update only the nonce part of the buffer
    // buffer.putInt(fpBytes.length + pkBytes.length, nonce);
    // blake.update(buffer.array());
    // byte[] hash= blake.digest();
    // // Do something with the hash
    // //System.out.println((blake.hexdigest()));
    // compareByteArrays(hash,toCompare,30);
    // nonce++;
    // }
    // System.out.println(nonce);

    // }

    private static boolean compareByteArrays(byte[] array1, byte[] array2, int numberOfBits) {

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

    // implement pom
    @Deprecated
    public static void pom(int numThreads) throws NoSuchAlgorithmException { // need to upgrade
        final int hashSize = 32; // Size of each SHA-256 hash in bytes
        final long totalSize = 1L * 1024 * 1024 * 1024; // 1GB in bytes
        final long numberOfHashes = totalSize / hashSize;
        final long hashesPerThread = numberOfHashes / numThreads;

        List<byte[]> hashes = Collections.synchronizedList(new ArrayList<>());
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    if (hashes.size() == 0) {
                        generateHashes(hashes, hashesPerThread, threadId);
                    }

                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Collections.sort(hashes, (a, b) -> {
            for (int j = 0; j < hashSize; j++) {
                int diff = Byte.compare(a[j], b[j]);
                if (diff != 0) {
                    return diff;
                }
            }
            return 0;
        });

        System.out.println("Generated and sorted " + hashes.size() + " hashes.");
    }

    public static void pom1(int numThreads) throws NoSuchAlgorithmException, InterruptedException { // latest pom use
                                                                                                    // pom1
        final int hashSize = 32; // Size of each SHA-256 hash in bytes
        final long totalSize = 1L * 1024 * 1024 * 1024; // 1GB in bytes
        final long numberOfHashes = totalSize / hashSize;
        final long hashesPerThread = numberOfHashes / numThreads;

        List<byte[]> hashes = Collections.synchronizedList(new ArrayList<>());
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        // Generate hashes
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    generateHashes(hashes, hashesPerThread, threadId);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            });
        }

        // Wait for hash generation to complete
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        // Sort the hashes
        Collections.sort(hashes, new Comparator<byte[]>() {
            @Override
            public int compare(byte[] o1, byte[] o2) {
                for (int i = 0; i < hashSize; i++) {
                    int diff = Byte.compare(o1[i], o2[i]);
                    if (diff != 0) {
                        return diff;
                    }
                }
                return 0;
            }
        });

        System.out.println("Generated and sorted " + hashes.size() + " hashes.");

        // Periodically search for a matching hash
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            search = getSearchHash(); // Assume getHash() is a method that provides a new hash.
            Optional<byte[]> found = findMatchingHash(hashes, search, difficulty);
            found.ifPresent(value -> System.out.println("Hash found: " + Arrays.toString(value)));
            found.ifPresent(value -> sendResultsToM(getPublicKey()));
        }, 0, 6, TimeUnit.SECONDS);
    }

    private static void generateHashes(List<byte[]> hashes, long hashesPerThread, int threadId)
            throws NoSuchAlgorithmException {
        byte[] fpBytes = Base58.decode(getFingerprint());
        byte[] pkBytes = Base58.decode(getPublicKey());
        ByteBuffer buffer = ByteBuffer.allocate(fpBytes.length + pkBytes.length + Integer.BYTES + Integer.BYTES);
        buffer.put(fpBytes);
        buffer.put(pkBytes);
        buffer.putInt(fpBytes.length + pkBytes.length, threadId);
        int nonce = 0;
        Blake3 blake = Blake3.newInstance();
        for (long i = 0; i < hashesPerThread; i++) {
            buffer.putInt(fpBytes.length + pkBytes.length + Integer.BYTES, nonce);
            blake.update(buffer.array());
            byte[] hash = blake.digest();
            hashes.add(hash);
        }
    }

    public static Optional<byte[]> findMatchingHash(List<byte[]> hashes, byte[] targetHash, int bitsToMatch) {

        if (hashes == null || targetHash == null || hashes.isEmpty()) {
            return Optional.empty();
        }

        int low = 0;
        int high = hashes.size() - 1;

        while (low <= high) {

            int mid = low + (high - low) / 2;
            byte[] midHash = hashes.get(mid);

            int comp = compareHashes(midHash, targetHash, bitsToMatch);

            if (comp == 0) {
                return Optional.of(midHash);
            } else if (comp < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return Optional.empty();
    }

    private static int compareHashes(byte[] hash1, byte[] hash2, int bitsToMatch) {

        int bitsCompared = 0;

        for (int i = hash1.length - 1; i >= 0 && bitsCompared < bitsToMatch; i--) {

            int byte1 = hash1[i] & 0xFF;
            int byte2 = hash2[i] & 0xFF;

            // Compare bits in each byte
            for (int bit = 7; bit >= 0 && bitsCompared < bitsToMatch; bit--) {

                int bit1 = byte1 & (1 << bit);
                int bit2 = byte2 & (1 << bit);

                if (bit1 != bit2) {
                    return bit1 - bit2;
                }

                bitsCompared++;
            }
        }

        return 0;
    }

    static boolean sendResultsToM(byte[] pkBytes, byte[] fpBytes, int nonce, int threadId) {
        String port = YamlConfigManager.getConfigValue("metronome", "port");
        String IP = YamlConfigManager.getConfigValue("metronome", "server");
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(IP, Integer.valueOf(port));
            MetronomeInf stub = (MetronomeInf) registry.lookup("MetronomeService");
            boolean prev = stub.winner(pkBytes, fpBytes, nonce, threadId);
            return prev;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("error while sending winner Metronome Server is offline, nounce= " + nonce
                    + " threadid= " + threadId);
            return false;

        }
    }

    private static boolean sendResultsToM(String publicKey) {
        String port = YamlConfigManager.getConfigValue("metronome", "port");
        String IP = YamlConfigManager.getConfigValue("metronome", "server");
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(IP, Integer.valueOf(port));
            MetronomeInf stub = (MetronomeInf) registry.lookup("MetronomeService");
            boolean prev = stub.winner(publicKey);
            return prev;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("error while sending winner Metronome Server is offline");
            return false;

        }
    }
    // send hash, nounce result to

    public static void main(String args[]) throws Exception {
        // System.out.println(Validator.getPublicKey());
        // Validator.genFingerPrint();
        String port = YamlConfigManager.getConfigValue("validator", "port");
        String IP = YamlConfigManager.getConfigValue("validator", "server");
        // System.out.println(IP + " > "+ port);

        Registry registry = LocateRegistry.createRegistry(Integer.valueOf(port));

        Validator v = new Validator();

        registry.rebind("validator", v);
        System.out.println("validator server is ready.on IP= " + IP + " Port " + port);

    }

    @Override
    public String Validatortest() throws RemoteException {
        // TODO Auto-generated method stub
        return "Hello this is validator";
    }
}
