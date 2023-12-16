package com.aos;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Testlatency {

    static String fileName1 = "experiment_results_latency.txt";
    static String fileName2 = "experiment_results_throutput.txt";
    static Map<String, String> result = new HashMap<>();
    static Queue<String> transactionQueue = new LinkedList<>();

    static void send128() throws Exception {
        System.out.println("Starting Latency test for value 128");
        WalletGenerator w = new WalletGenerator();
        String destination = "AAoGJPd4A5SP7Vu8urKoT3SfYYu1ojk9ezaQdxjpbhif";

        double amount = 0.5;

        for (int i = 0; i < 128; i++) {
            String txId = w.send1(amount, destination);
            result.put(txId, "generated");
            System.out.println(i + ": sendig " + amount + " to " + destination);
            while (!w.trxstatus(txId).contains("valid")) {
                Thread.sleep(4000);
            }
            String temp = w.trxstatus(txId);
            // System.out.println(temp+">>>>>>>>>here temp");
            int tempsize = temp.split("_").length;
            // System.out.println(">>>>> txID "+txId+"
            // extracted"+temp.split("_")[tempsize-1]);
            result.put(txId, temp.split("_")[tempsize - 1]);
            System.out.println("res" + result.get(txId));
        }
        System.out.println("Writing results");
        printresults(fileName1);
        System.out.println("Test completed results stored in :"+fileName1);
    }

    static void send128(int loopvalue) throws Exception {
        System.out.println("Starting Latency test for value "+loopvalue);
        WalletGenerator w = new WalletGenerator();
        String destination = "AAoGJPd4A5SP7Vu8urKoT3SfYYu1ojk9ezaQdxjpbhif";

        double amount = 0.5;

        for (int i = 0; i < loopvalue; i++) {
            String txId = w.send1(amount, destination);
            result.put(txId, "generated");
            System.out.println(i + ": sendig " + amount + " to " + destination);
            while (!w.trxstatus(txId).contains("valid")) {
                Thread.sleep(4000);
            }
            String temp = w.trxstatus(txId);
            // System.out.println(temp+">>>>>>>>>here temp");
            int tempsize = temp.split("_").length;
            // System.out.println(">>>>> txID "+txId+"
            // extracted"+temp.split("_")[tempsize-1]);
            result.put(txId, temp.split("_")[tempsize - 1]);
            System.out.println("res" + result.get(txId));
        }
        System.out.println("Writing results");
        printresults(fileName1+loopvalue);
        System.out.println("Test completed results stored in : "+fileName1+loopvalue);
    }

    static void send128K() throws Exception {
        System.out.println("Starting throutput test for value 128K");
        WalletGenerator w = new WalletGenerator();
        String destination = "AAoGJPd4A5SP7Vu8urKoT3SfYYu1ojk9ezaQdxjpbhif";
        double amount = 0.0007;
        for (int i = 0; i < 128000; i++) {
            String txId = w.send1(amount, destination);
            transactionQueue.add(txId);
        }
        System.out.println("Checking tranaction status...");
        while (!transactionQueue.isEmpty()) {
            String txId = transactionQueue.poll(); // Retrieve and remove the head of the queue
            String status = w.trxstatus(txId); // Implement this method

            if (status.contains("valid")) {

                int tempsize = status.split("_").length; // Implement this method
                result.put(txId, status.split("_")[tempsize - 1]);
                System.out.println("res" + result.get(txId));
            } else {
                transactionQueue.add(txId); // Re-enqueue the transaction ID
                // Optional: introduce a delay here to avoid hammering the blockchain with
                // requests
            }
        }
        System.out.println("Writing results");
        printresults(fileName2);
        System.out.println("Test completed results stored in :"+fileName2);

    }

     static void send128K(int loopvalue) throws Exception {
        System.out.println("Starting throutput test for value "+loopvalue);
        WalletGenerator w = new WalletGenerator();
        String destination = "AAoGJPd4A5SP7Vu8urKoT3SfYYu1ojk9ezaQdxjpbhif";
        double amount = 0.0007;
        for (int i = 0; i < loopvalue; i++) {
            String txId = w.send1(amount, destination);
            transactionQueue.add(txId);
        }
        System.out.println("Checking tranaction status...");
        while (!transactionQueue.isEmpty()) {
            String txId = transactionQueue.poll(); // Retrieve and remove the head of the queue
            String status = w.trxstatus(txId); // Implement this method

            if (status.contains("valid")) {

                int tempsize = status.split("_").length; // Implement this method
                result.put(txId, status.split("_")[tempsize - 1]);
                System.out.println("res" + result.get(txId));
            } else {
                transactionQueue.add(txId); // Re-enqueue the transaction ID
                // Optional: introduce a delay here to avoid hammering the blockchain with
                // requests
            }
        }
        System.out.println("Writing results");
        printresults(fileName2+loopvalue);
        System.out.println("Test completed results stored in :"+fileName2+loopvalue);

    }

    static void printresults(String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (Map.Entry<String, String> entry : result.entrySet()) {
                writer.write(entry.getKey() + ": " + entry.getValue() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        Testlatency tl = new Testlatency();

        if (args.length > 1) {
            if (args[0].equals("1"))
                Testlatency.send128(Integer.valueOf(args[1]));
            if (args[0].equals("2"))
                Testlatency.send128K(Integer.valueOf(args[1]));
        } else {
            System.err.println("invalid arguments ");
            System.err.println(" '1' for Latency ");
            System.err.println(" '2' for throutput ");

        }

    }
}
