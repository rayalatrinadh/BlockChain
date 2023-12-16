package com.aos;

import java.rmi.Remote;

public interface BlockChainInf extends Remote {
    // method to ADD new block
    public void addblock(Block nb) throws Exception;

    public void addblock(Block nb, String publickey) throws Exception;

    // get the last block
    public Block previousblock() throws Exception;

    // cal balance of a last block
    public double getBalance(byte[] public_key) throws Exception;

    public double getBalance(String walletAddress) throws Exception;
}
