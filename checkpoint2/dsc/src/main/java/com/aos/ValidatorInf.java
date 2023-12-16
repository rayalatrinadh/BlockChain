package com.aos;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ValidatorInf extends Remote {
    public void changeWorkMode() throws RemoteException;

    public void createBlock() throws RemoteException;

    public String Validatortest() throws RemoteException;
}
