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
                Print("\t |balance ~ 'wallet balance'");
                Print("\t |send transaction ~ 'wallet send <amount> <Address>'");
                Print("\t |transaction status ~ 'wallet transaction <ID>'");
                Print("\t |transaction history ~'Wallet history'");
                break;
            case "history":
                w.recentTransactions();
                break;
            default:
                Print("invalid command try help or wallet help for more info");
                break;

        }
    }

    public static void config(String[] input) {
        switch (input[1]) {
            case "update":
                if (input.length != 5) {
                    Main.Print("Inavalid Config Update command");
                } else {
                    try {
                        YamlConfigManager.updateConfigValue(input[2], input[3], input[4]);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        System.out.println("Unable to update value");
                    }
                    break;

                }
            case "get":
                if (input.length != 4) {
                    Print("Invalid Config get command");

                } else {
                    Print(YamlConfigManager.getConfigValue(input[2], input[3]));
                }
                break;
            case "help":
                Print("\t |update ~ 'config update <key> <value> <NewValue>' ");
                Print("\t |get ~ 'config get <key> <value> <NewValue>' ");
                break;
        }
    }

    public static void Print(String in) {
        System.out.println(in);
    }

    public static void main(String[] args) throws IOException {

        // Check and generate config files.
        @SuppressWarnings(value = { "unused" })
        YamlConfigManager y = new YamlConfigManager();
        WalletGenerator w = new WalletGenerator();

        if(args.length>0)
        {
            
            String input="";
            for(String x:args)
            {
                input+=x+" ";
            }
            
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
                        Print("\t |Balance ~ 'wallet balance'");
                        Print("\t |send transaction ~ 'wallet send <amount> <Address>'");
                        Print("\t |Transaction ~ 'wallet transaction <ID>'");
                        Print("\t |transaction history ~'Wallet history'");
    
                        Print("\nBlock Chain :");
                        Print("\topen new terminal and run \n'java -cp target/dsc-1.0-SNAPSHOT-jar-with-dependencies.jar com.aos.BlockChain'");
    
                        Print("\nPool :");
                        Print("\topen new terminal and run \n'java -cp target/dsc-1.0-SNAPSHOT-jar-with-dependencies.jar com.aos.Pool'");
    
                        Print("\nMetronome :");
                        Print("\topen new terminal and run \n'java -cp target/dsc-1.0-SNAPSHOT-jar-with-dependencies.jar com.aos.Metronome'");
    
                        Print("\nValidator :");
                        Print("\topen new terminal and run \n'java -cp target/dsc-1.0-SNAPSHOT-jar-with-dependencies.jar com.aos.Validator'");
                        Print("Config operations :");
                        break;
                    case "config":
                        if (input.split(" ").length >= 2)
                            config(input.split(" "));
                        // Print("\t |update ~ 'config update <key> <value> <NewValue>' ");
                        // Print("\t |get ~ 'config get <key> <value>' ");
                        else
                            Print("Config");
                        break;
                    case "test":
                        if (input.split(" ").length >= 2)
                            test(input.split(" "));
                        else {
                            System.out.println("| default latency ~ test 1");
                            System.out.println("| default throutput ~ test 2");
                            System.out.println("| sized laency ~ test 1 <value>");
                            System.out.println("| sized throutput ~ test 2 <value>");
                        }
                        break;
                }
               // input = input();
            
            Print("succeeded");

        }else{

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
                    Print("\t |Balance ~ 'wallet balance'");
                    Print("\t |send transaction ~ 'wallet send <amount> <Address>'");
                    Print("\t |Transaction ~ 'wallet transaction <ID>'");
                    Print("\t |transaction history ~'Wallet history'");

                    Print("\nBlock Chain :");
                    Print("\topen new terminal and run \n'java -cp target/dsc-1.0-SNAPSHOT-jar-with-dependencies.jar com.aos.BlockChain'");

                    Print("\nPool :");
                    Print("\topen new terminal and run \n'java -cp target/dsc-1.0-SNAPSHOT-jar-with-dependencies.jar com.aos.Pool'");

                    Print("\nMetronome :");
                    Print("\topen new terminal and run \n'java -cp target/dsc-1.0-SNAPSHOT-jar-with-dependencies.jar com.aos.Metronome'");

                    Print("\nValidator :");
                    Print("\topen new terminal and run \n'java -cp target/dsc-1.0-SNAPSHOT-jar-with-dependencies.jar com.aos.Validator'");
                    Print("Config operations :");
                    break;
                case "config":
                    if (input.split(" ").length >= 2)
                        config(input.split(" "));
                    // Print("\t |update ~ 'config update <key> <value> <NewValue>' ");
                    // Print("\t |get ~ 'config get <key> <value>' ");
                    else
                        Print("Config");
                    break;
                case "test":
                    if (input.split(" ").length >= 2)
                        test(input.split(" "));
                    else {
                        System.out.println("| default latency ~ test 1");
                        System.out.println("| default throutput ~ test 2");
                        System.out.println("| sized laency ~ test 1 <value>");
                        System.out.println("| sized throutput ~ test 2 <value>");
                    }
                    break;
            }
            input = input();
        }
        Print("Exited");
    }

    }

    private static void test(String[] input) {
        Testlatency tl = new Testlatency();
        switch (input[1]) {
            case "1":
                try {
                    if (input.length==3)
                    tl.send128(Integer.valueOf(input[2]));
                    else
                    tl.send128();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
            case "2":
                try {
                    if (input.length==3)
                    tl.send128K(Integer.valueOf(input[2]));
                    else
                    tl.send128K();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
            default:
                System.out.println("| test 1");
                System.out.println("| test 2");

        }

    }
}