package com.aos;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class BlockChain extends UnicastRemoteObject implements BlockChainInf{
    private byte[] currenthash={1,2,3,4,5,6,7,8,9,0};
    List<Block> b;

    Block Genesisblock =new Block(1,currenthash,0,System.currentTimeMillis(),21,0);

    public BlockChain() throws RemoteException 
    {
        b=new ArrayList<>();
        b.add(Genesisblock);
    }

    public void addblock(Block nb)
    {
        b.add(nb);
        System.out.println("New block received from metronome, Block "+nb.getblockID()+ "hash "+Base58.encode(nb.calhash()));
    }

    public Block previousblock()
    {
        Block x= b.get(b.size()-1);
        System.out.println("Block request from validator "+ Base58.encode(x.calhash()));
        return x;
    }

    public double getBalance(byte[] public_key)
    {
        return 7.8;
    }
    public static void main(String args[]) throws NumberFormatException, RemoteException
    {
        String port = YamlConfigManager.getConfigValue("blockchain", "port");
        BlockChain BcServ= new BlockChain();
        Registry registry = LocateRegistry.createRegistry(Integer.valueOf(port));
        registry.rebind("BcService", BcServ);
        System.out.println("BlockChain server is ready.");
    }
}
