package Chord;

import Application.Node;
import Utilities.Log;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Thread that checks periodically whether predecessor of node is alive
 */
public class CheckPredecessor implements Runnable
{

	private final Node node;
	private boolean isAlive = false;

	/*Why?*/
	public CheckPredecessor(Node node)
	{
		this.node = node;
	}

	public void run()
	{
		Node tempPred = null;
		try
		{
			System.out.println("\nCheck Predecessor called at: " + node.getLocalID().toString());
			if (node.getPredecessor() == null)
			{
				return;
			}
		}
		catch (RemoteException ex)
		{
			Log.addMessage("CheckPredecessor-Rexc[0]: A problem occurred in CheckPredecessor. Will be executed again later..", Log.ERROR);
		}


		try
		{

			try
			{
				try
				{
					tempPred = (Node) Naming.lookup("/" + node.getPredecessor().getIP() + ":1099/" + String.valueOf(node.getPredecessor().getPID()));
				}
				catch (MalformedURLException ex)
				{
					Log.addMessage("MalformedURLException", Log.ERROR);
				}
			}
			catch (NotBoundException ex)
			{
				Log.addMessage("CheckPredecessor-NBexc[0]: Predecessor is not bound or was unbound.", Log.ERROR);
			}
		}
		catch (RemoteException ex)
		{
			Log.addMessage("CheckPredecessor-Rexc[1]: A problem occurred while trying to lookup for predecessor.", Log.ERROR);
		}

		if (tempPred != null)
		{
			try
			{
				isAlive = tempPred.isAlive();
				System.out.println("Node: " + tempPred.getLocalID().toString() + " is alive");
			}
			catch (java.rmi.ConnectException rmiE)
			{
				Log.addMessage("CheckPredecessor-Cexc[0]: Predecessor is dead! Setting predecessor to null", Log.ERROR);
				try
				{
					//predecessor dead, so predecessor variable null
					node.setPredecessor(null);
				}
				catch (RemoteException ex)
				{
					Log.addMessage("CheckPredecessor-Rexc[2]: A problem occurred while setting node's predecessor to null", Log.ERROR);
				}
			}
			catch (RemoteException ex)
			{
				Log.addMessage("CheckPredecessor-Rexc[3]: A problem occurred while checking health(:P) of predecessor", Log.ERROR);
			}


			/*
			 * predecessor didn't respond
			 */
			if (!isAlive)
			{
				try
				{
					//predecessor dead, so predecessor variable null
					node.setPredecessor(null);
				}
				catch (RemoteException ex)
				{
					Log.addMessage("CheckPredecessor-Rexc[4]: A problem occurred while setting node's predecessor to null", Log.ERROR);
				}
			}
		}


		isAlive = false;
	}
}
