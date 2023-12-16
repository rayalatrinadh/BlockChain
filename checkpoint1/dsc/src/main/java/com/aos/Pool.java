package com.aos;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Pool extends UnicastRemoteObject implements PoolInf{
    static Queue<Transaction> trx;
    static Map<String,Transaction> status;

    public Pool() throws RemoteException 
    {
        trx = new LinkedList<>();
        status = new HashMap<String,Transaction>();
    }

    public String addTransaction(Transaction Trx)
    {
        trx.add(Trx);
        System.out.println(Base58.encode(Trx.getTransitionId()) +" Queue size:"+ trx.size());
        return "unProcessed";
    }

    public String Status(String transactionId)
    {
        Transaction T;
        try{T= trx.poll();
        if (T!=null)
        {
            String ID= Base58.encode(T.getTransitionId());
        status.put(ID,T);
        System.out.println("Status: rec: "+ transactionId+" extrcted: "+ID+" mapsize: "+status.size());
        }}catch (Exception e){}
        
        
        //
        if(status.containsKey(transactionId))
        {
            return "confirmed";

        }else
        {
            return "not confirmed";
        }
    }

    public static void main(String args[]) 
    {
        String port = YamlConfigManager.getConfigValue("pool", "port");
        // String IP = YamlConfigManager.getConfigValue("pool", "server");
        //System.out.println(IP + " > "+ port);
        Pool pserv;
        try {
            pserv = new Pool();
            Registry registry;
                registry = LocateRegistry.createRegistry(Integer.valueOf(port));
            registry.rebind("PoolService", pserv);
            System.out.println("Pool server is ready.");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("Check the dsc-configfile existance and port details\n or the port mentioned is already in use");
        }
         
        
         
         
    }



}
