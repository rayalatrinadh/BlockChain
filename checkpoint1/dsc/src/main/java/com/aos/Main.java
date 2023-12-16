package com.aos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    static BufferedReader kb = new BufferedReader(new InputStreamReader(System.in));

    static String input() throws IOException {
        System.out.print("dsc ~ ");
        String input = kb.readLine();
        return input;
    }

    public static void wallet(WalletGenerator w, String[] input) {
        switch (input[1]) {
            case "create":
                w.create();
                break;
            case "key":
                w.key();
                break;
            case "balance":
                w.getBalance();
                break;

            case "send":
                if (input.length != 4) {
                    Print("invalid send command");
                } else {
                    try {
                        w.send(Double.valueOf(input[2]), input[3]);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        System.out.println("Invalid send format");
                    }
                }
                break;

            case "transaction":
                if (input.length != 3)
                    Print("invalid transaction command");
                else
                    w.trxstatus(input[2]);
                break;
            case "help":
                Print("wallet operations :");
                Print("\t |create ~ 'wallet create' ");
                Print("\t |key ~ 'wallet key'");
                Print("\t |key ~ 'wallet balance'");
                Print("\t |send transaction ~ 'wallet send <amount> <Address>'");
                Print("\t |key ~ 'wallet transaction <ID>'");
                break;
            default:
                Print("invalid command try help or wallet help for more info");
                break;

        }
    }

    public static void Print(String in) {
        System.out.println(in);
    }

    public static void main(String[] args) throws IOException {

        // Check and generate config files.
        YamlConfigManager y = new YamlConfigManager();
        WalletGenerator w = new WalletGenerator();
        String input = input();
        while (!"exit".equals(input)) {
            switch (input.split(" ")[0]) {
                case "wallet":
                    Main.Print("wallet");
                    if (input.split(" ").length >= 2)
                        wallet(w, input.split(" "));
                    break;
                case "help":
                    Print("wallet operations :");
                    Print("\t |create ~ 'wallet create' ");
                    Print("\t |key ~ 'wallet key'");
                    Print("\t |key ~ 'wallet balance'");
                    Print("\t |send transaction ~ 'wallet send <amount> <Address>'");
                    Print("\t |key ~ 'wallet transaction <ID>'");

                    Print("\nBlock Chain :");
                    Print("\topen new terminal and run \n'java -cp target/dsc-1.0-SNAPSHOT-jar-with-dependencies.jar com.aos.BlockChain'");

                    Print("\nPool :");
                    Print("\topen new terminal and run \n'java -cp target/dsc-1.0-SNAPSHOT-jar-with-dependencies.jar com.aos.Pool'");

                    Print("\nMetronome :");
                    Print("\topen new terminal and run \n'java -cp target/dsc-1.0-SNAPSHOT-jar-with-dependencies.jar com.aos.Metronome'");

                    Print("\nValidator :");
                    Print("\topen new terminal and run \n'java -cp target/dsc-1.0-SNAPSHOT-jar-with-dependencies.jar com.aos.Validator'");
                    break;
            }
            input = input();
        }
        Print("Exited");

    }
}