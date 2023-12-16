package com.aos;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Enumeration;

public class roughtest {
    public static void main(String[] args) throws RemoteException, NotBoundException {
        System.out.println(Integer.valueOf(YamlConfigManager.getConfigValue("validator", "port")));
        Registry registry = LocateRegistry.getRegistry("127.0.0.1", 10004);
        ValidatorInf validatorStub = (ValidatorInf) registry.lookup("validator");
        System.out.println(validatorStub.Validatortest());
    }

    static String currentIP() throws UnknownHostException {
        String currentIP = InetAddress.getLocalHost().getHostAddress(); // Always server starts on the current host
        if (currentIP.startsWith("127."))
            try {
                currentIP = getLocalIPv4Address();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                System.err.println("Unable to get IP Address of Validator");
            }
        return currentIP;
    }

    public static String getLocalIPv4Address() throws Exception {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            if (iface.isLoopback() || !iface.isUp())
                continue;

            Enumeration<InetAddress> addresses = iface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();

                if (addr.isLoopbackAddress() || !(addr.getHostAddress().contains(".")))
                    continue; // This filters out IPv6 addresses

                return addr.getHostAddress();
            }
        }
        return null;
    }
}
