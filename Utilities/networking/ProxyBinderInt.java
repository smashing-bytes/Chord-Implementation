/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities.networking;

import java.rmi.*;

/**
 *
 * @author mariuska
 */
public interface ProxyBinderInt extends Remote {

    public void remoteBind(String prefix, Remote omg) throws RemoteException, AlreadyBoundException;

    public void remoteUnbind(String prefix) throws RemoteException, NotBoundException;
}
