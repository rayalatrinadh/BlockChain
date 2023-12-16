package com.aos;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MetronomeInf extends Remote{
    
    // method to set difficulty
    public void setDifficulty(int diff) throws Exception;

    //method to get difficulty
    public int getDifficulty() throws Exception;

    //method to generate hash
    //public String genHash(String input, int length) throws Exception;

    //method to set new hash
    //public boolean setNewHash(String hash) throws Exception;

    //method to get current hash
    public byte[] getCurrentHash() throws Exception;

    //start creating genblock in a thread for every 6 secs
    public void MetronomeStart() throws RemoteException;

    //
    public void genBlock() throws Exception;

    public void calltoBc(Block b2) throws Exception;

    
}
