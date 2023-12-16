package com.aos;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MetronomeInf extends Remote {

    // method to set difficulty
    public void setDifficulty(int diff) throws Exception;

    // method to get difficulty
    public int getDifficulty() throws Exception;

    // method to generate hash
    // public String genHash(String input, int length) throws Exception;

    // method to set new hash
    // public boolean setNewHash(String hash) throws Exception;

    // method to get current hash
    public byte[] getCurrentHash() throws Exception;

    // start creating genblock in a thread for every 6 secs
    public void MetronomeStart() throws RemoteException;

    //
    public void genBlock() throws Exception;

    public void calltoBc(Block b2) throws Exception;

    public void registerV(String ip, int port, String publicKey) throws RemoteException;

    public void sendSignalM() throws RemoteException;

    public void sendSignalV(String publicKey) throws RemoteException;

    public boolean winner(byte[] pkBytes, byte[] fpBytes, int nonce, int threadId, int ndifficulty)
            throws RemoteException;

    public boolean compareByteArrays(byte[] array1, byte[] array2, int numberOfBits) throws Exception;

    public boolean winner(String publicKey) throws RemoteException;

}
