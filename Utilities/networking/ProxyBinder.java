/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities.networking;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;
import java.rmi.*;

/**
 *
 * @author mariuska
 */
public class ProxyBinder extends UnicastRemoteObject implements ProxyBinderInt
{

    public ProxyBinder() throws RemoteException
    {
    }

    public void remoteBind(String prefix, Remote omg) throws RemoteException, AlreadyBoundException
    {
        Registry rmiHandle = LocateRegistry.getRegistry();
        rmiHandle.bind(prefix, omg);

    }

    public void remoteUnbind(String prefix) throws RemoteException, NotBoundException
    {
        Registry rmiHandle = LocateRegistry.getRegistry();
        rmiHandle.unbind(prefix);
    }
}