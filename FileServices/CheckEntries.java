package FileServices;

import Application.Node;
import Application.FIDEntry;
import DHash.FileNameKey;
import DHash.IdKey;
import java.net.MalformedURLException;
import java.rmi.*;
import Utilities.Log;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class CheckEntries implements Runnable
{
	private Node node;
	private Share share;
	private Set<IdKey> toBeRemoved;

	public CheckEntries(Node node, Share share)
	{
		this.node = node;
		this.share = share;
	}

	public CheckEntries(Node node, FileManager fm)
	{
		this.share = fm.share;
		this.node = node;
	}

	public void run()
	{
		toBeRemoved = new HashSet<IdKey>();
		//Log.addMessage("Started Check Entries", Log.WARNING);
		/**
		 * checks whether all files are in the system
		 * if not all files in the ring, add them to their successor
		 */
		String[] filenames = share.getFilenames();

		//remove postfixes
		for (int i =0; i < filenames.length; i++)
		{
			if (filenames[i].lastIndexOf(".") > 0)
				filenames[i] = filenames[i].substring(0, filenames[i].lastIndexOf("."));
		}


		for (int i = 0; i < filenames.length; i++)
		{
			try
			{
				/*Hash every filename*/
				FIDEntry entry = new FIDEntry(filenames[i], node.getLocalID().getIP(), node.getLocalID().getPID(), node.getProperties().getFSPort());

				FileNameKey fnk = new FileNameKey(filenames[i]);

				//find the successor of the filename
				Node successor = node.find_successor(fnk);

				//if filename does not exists in the entry set of the successor
				// add it
				if (!successor.existsInEntries(entry, node.getLocalID()))
				{
					successor.addEntry(node.getLocalID(), entry);
				}
			}
			catch (ConnectException ce)
			{
				System.out.println("Could not connect to successor of " + filenames[i] + "in CheckEntries");
				Log.addMessage("Could not connect to successor of " + filenames[i] + "in CheckEntries", Log.ERROR);
			}
			catch (RemoteException re)
			{
				System.out.println("RemoteException in CheckEntries for " + filenames[i]);
				Log.addMessage("A problem occurred in CheckEntries for " + filenames[i], Log.ERROR);
			}
		}

		/**
		 * Check whether every IdKey in the Map of the Node
		 * belongs to an alive node.
		 * if an entry doesn't, remove the entry from the Map
		 */

		Iterator<Map.Entry<IdKey, Set<FIDEntry>>> it = null;
		try
		{
			it = node.getAllEntries().entrySet().iterator();
		}
		catch (RemoteException ex)
		{
			Log.addMessage("A problem occurred while retrieving the entry set of the node", Log.ERROR);
		}
		Map.Entry<IdKey, Set<FIDEntry>> itEntry = null;
		IdKey id = null;
		Node idNode = null;
		while (it.hasNext())
		{
			itEntry = it.next();
			id = itEntry.getKey();
			try
			{
				System.out.println("Getting " + id.hashKeytoHexString() + " node");
				idNode = (Node) Naming.lookup("/" + id.getIP() + ":1099/" + String.valueOf(id.getPID()));
			}
			catch (NotBoundException ex)
			{
				System.out.println("The node with " + id.hashKeytoHexString() + "is not bound, or was unbound. Problem occurred in CheckEntries");
				Log.addMessage("The node with " + id.hashKeytoHexString() + "is not bound, or was unbound. Problem occurred in CheckEntries", Log.ERROR);
			}
			catch (MalformedURLException ex)
			{
				Log.addMessage("Malformed URL in CheckEntries", Log.ERROR);
			}
			catch (ConnectException ce)
			{
				Log.addMessage("Could not connect to the node in CheckEntries", Log.ERROR);
			}
			catch (RemoteException ex)
			{
				Log.addMessage("A problem occurred in CheckEntries while checking the idNode", Log.ERROR);
			}

			if (idNode != null)
			{
				boolean isAlive = false;
				try
				{
					System.out.println("Checking if " + idNode.getLocalID().hashKeytoHexString() + " is alive");
					isAlive = idNode.isAlive();
					System.out.println(idNode.getLocalID().hashKeytoHexString() + " is alive");
				}
				catch (ConnectException ce)
				{
					Log.addMessage("Could not connect to the node of the id.", Log.ERROR);
					/*try
					{
					    node.removeEntrySet(id);
					}
					catch (RemoteException ex)
					{
					    System.out.println("A problem occurred in CheckEntries, while removing the entries");
					    Log.addMessage("A problem occurred in CheckEntries, while removing the entries, after ConnectException", Log.ERROR);
					}*/
				}
				catch (RemoteException re)
				{
					System.out.println("A problem occurred in CheckEntries, while checking if idKeys are alive");
					Log.addMessage("A problem occurred in CheckEntries, while checking if idKeys are alive", Log.ERROR);
				}

				/**
				 * node didn't respond
				 */

				if (!isAlive)
				{
					System.out.println(id.hashKeytoHexString() + " is not alive, adding to the toBeRemoved Set");
					//add the entry to the toBeRemoved Set
					toBeRemoved.add(id);

				}

			}
		}

		IdKey removing =null;

		Iterator<IdKey> iter = toBeRemoved.iterator();
		while (iter.hasNext())
		{
			removing = iter.next();
			try
			{
				node.removeEntrySet(removing);
			}
			catch (RemoteException ex)
			{
				Log.addMessage("A problem occurred while removing entries, in checkEntries", Log.ERROR);
			}
		}
	}
}
