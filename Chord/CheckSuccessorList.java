package Chord;

import Application.Node;
import DHash.IdKey;
import Utilities.Log;
import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 *
 * @author gasparosoft
 */
public class CheckSuccessorList implements Runnable
{

	private Node node;

	public CheckSuccessorList(Node node)
	{
		this.node = node;
	}

	public void run()
	{

		Node imNode = null;
		Node tempNode = node;
		IdKey id = null;
		IdKey[] sucArray = new IdKey[3];


		try
		{
			System.out.println("CheckSuccessorList called at : " + node.getLocalID().toString());
		}
		catch (ConnectException ce)
		{
			Log.addMessage(" CheckSuccessorList-CExc[0]: Could not connect to the node when fixing Successor List. Try again later", Log.ERROR);
		}
		catch (RemoteException ex)
		{
			Log.addMessage(" CheckSuccessorList-Rexc[0]: A problem occured when fixing Succesosr List. Try again later", Log.ERROR);
		}

		int i = 0;

		for (i = 0; i < 3; i++)
		{
			try
			{
				id = tempNode.getImmediateSuccessor();
			}
			catch (ConnectException ce)
			{
				Log.addMessage(" CheckSuccessorList-Cexc[1]: Could not connect to node, when updating list.", Log.ERROR);
			}
			catch (RemoteException ex)
			{
				Log.addMessage(" CheckSuccessorList-Rexc[1]: A problem occurred when updating successor list. Try again later", Log.ERROR);
			}
			try
			{
				try
				{
					// imNode = (Node) tempNode.getRMIHandle().lookup(String.valueOf(id.getPID()));
					imNode = (Node) Naming.lookup("/" + id.getIP() + ":1099/" + String.valueOf(id.getPID()));
					sucArray[i] = id;
				}
				catch (NotBoundException ex)
				{
					try
					{
						IdKey key = node.getImmediateSuccessor();
						node.clearSuccessorList();
						node.addSuccessor(key, 0);
					}
					catch (RemoteException exr)
					{
						Log.addMessage("Successor is dead", Log.ERROR);
					}
				}
				catch (MalformedURLException ex)
				{
					Log.addMessage("MalformedURLException", Log.ERROR);
				}


				if (!node.getWholeSucList().contains(id) && (!node.getLocalID().equals(id)))
				{
					node.addSuccessor(id, i);
				}

				tempNode = imNode;


			}
			catch (ConnectException ce)
			{
				Log.addMessage(" CheckSuccessorList-Cexc[2]: Could not connect to node " + id.toString() + ", when updating list. Stabilize will correct(?) the problem.", Log.ERROR);

				IdKey key;
				if (i == 0)
				{

					try
					{
						key = node.getWholeSucList().get(i + 1);
						node.addSuccessor(key, i);
						node.setFinger(key, 0);
						node.removeFinger(0);

					}
					catch (RemoteException ex)
					{
						Log.addMessage("Remote Exception", Log.ERROR);
					}

				}
				else
				{
					try
					{
						key = node.getImmediateSuccessor();
						node.clearSuccessorList();
						node.addSuccessor(key, 0);
					}
					catch (RemoteException ex)
					{
						Log.addMessage("Remote Exception", Log.ERROR);
					}
				}
			}
			catch (RemoteException ex)
			{
				Log.addMessage(" CheckSuccessorList-Rexc[2]: A problem occurred when updating successor list of " + id.toString() + " Try again later", Log.ERROR);
			}
		}
		try
		{
			//node.checkIntegrityOfSucList(sucArray);
			//boolean found = false;
			node.checkIntegrityOfSucList();
			boolean found = false;
			int j;
			/* for (j = 0; j < 3; j++) {
			     if (sucArray[j] == null)
			         Log.addMessage("[" + j+"] => NULL",Log.ERROR);
			     else
			        Log.addMessage("[" + j+"] => " + sucArray[j].hashKeytoHexString(),Log.ERROR);
			 }*/
			if (sucArray != null && node.getWholeSucList().size() > 1)
			{
				for (i = 0; i < node.getWholeSucList().size(); i++)
				{


					for (j = 0; j < 3; j++)
					{
						if (sucArray[j] != null)
						{
							if (sucArray[j].equals(node.getSucList(i)))
							{
								found = true;
								break;
							}
						}
					}
					if (!found)
					{
						node.removeSuccessor(i);
					}
					found = false;

				}
			}

		}
		catch (RemoteException ex)
		{
			Log.addMessage(" CheckSuccessorList-Rexc[3]: A problem occurred when checking the integrity of successor list of the node!", Log.ERROR);
		}
	}
}