package com.aos;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//import io.lktk.NativeBLAKE3Util.InvalidNativeOutput;

public class Metronome extends UnicastRemoteObject implements MetronomeInf{
    // set difficulty to 30
    //send difficulty to validator when requested

    //create empty block for every 6 sec
     ExecutorService executor = Executors.newFixedThreadPool(2);
    private int difficulty;
    private int blockID=1;
    private byte[] currenthash={1,2,3,4,5,6,7,8,9,0};
    Block b;

    public Metronome() throws RemoteException 
    {
        setDifficulty(30);
        //serverstart

        MetronomeStart();

    }

    public void getprevioushash() 
    {
         String port = YamlConfigManager.getConfigValue("blockchain", "port");
        String IP = YamlConfigManager.getConfigValue("blockchain", "server");
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(IP, Integer.valueOf(port));
            BlockChainInf stub = (BlockChainInf) registry.lookup("BcService");
        Block prev = stub.previousblock();
        this.currenthash=prev.calhash();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.err.print("BlockChain Server unreachable");
        }
        
    }
    public void MetronomeStart() throws RemoteException 
    {
       
        executor.submit(() -> {
                TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    genBlock();
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
    public void setDifficulty(int diff)
    {
        this.difficulty = diff;
    }

    public int getDifficulty()
    {
        return this.difficulty;
    }

    public void genBlock() throws Exception
    {
        getprevioushash();
         b =new Block(1,currenthash,blockID++,System.currentTimeMillis(),this.difficulty,0);
        currenthash=b.calhash();
        System.out.println("New block created, hash: "+Base58.encode(currenthash));

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
         System.out.println("sent to blockchain");;
        } catch ( Exception e) {
            // TODO Auto-generated catch block
            System.out.println("Blockchain Server Off-Line, Block is thrown away");
        }
        
        

        
    }

    public byte[] getCurrentHash()
    {
        return this.currenthash;
    }

    public static void main(String args[]) throws RemoteException
    {
        Metronome mServ= new Metronome();
        System.out.println(mServ.getDifficulty());

        String port = YamlConfigManager.getConfigValue("metronome", "port");
        // String IP = YamlConfigManager.getConfigValue("metronome", "server");
        //System.out.println(IP + " > "+ port);
       
         Registry registry = LocateRegistry.createRegistry(Integer.valueOf(port));
         registry.rebind("MetronomeService", mServ);
         System.out.println("Metronome server is ready.");

    }


}
