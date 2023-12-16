package com.aos;

package com.aos;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class ThroughputCalculator {

    private static final int TRANSACTIONS_PER_BLOCK = 8191;

    public static void main(String[] args) {
        String filePath = "experiment_results_throutput.txt"; // Replace with your file path
        try {
            Queue<Double> timestamps = readTimestampsFromFile(filePath);
            calculateThroughput(timestamps);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Queue<Double> readTimestampsFromFile(String filePath) throws IOException {
        Queue<Double> timestamps = new LinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Assuming each line contains a transaction ID followed by a timestamp
                String[] parts = line.split(" "); // Adjust the delimiter based on your file format
                double timestamp = Double.parseDouble(parts[1]); // Adjust index based on your file format
                timestamps.add(timestamp);
            }
        }
        return timestamps;
    }

    private static void calculateThroughput(Queue<Double> timestamps) {
        int blockCount = 0;
        double totalThroughput = 0;

        while (!timestamps.isEmpty()) {
            double blockStartTime = timestamps.poll();
            double blockEndTime = blockStartTime;
            int transactions = 1;

            while (transactions < TRANSACTIONS_PER_BLOCK && !timestamps.isEmpty()) {
                blockEndTime = timestamps.poll();
                transactions++;
            }

            double timeTakenSeconds = blockEndTime - blockStartTime;
            double throughput = timeTakenSeconds > 0 ? transactions / timeTakenSeconds : 0;
            totalThroughput += throughput;
            blockCount++;

            System.out.println("Throughput for Block " + blockCount + ": " + throughput + " TPS");
        }

        double averageThroughput = blockCount > 0 ? totalThroughput / blockCount : 0;
        System.out.println("Average Throughput: " + averageThroughput + " TPS");
    }
}
