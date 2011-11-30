package Chord;

import java.net.MalformedURLException;
import java.util.Set;
import java.util.Map;
import Application.FIDEntry;
import Utilities.networking.ProxyBinderInt;
import Application.Node;
import Application.NodeImpl;
import DHash.IdKey;
import DHash.Key;
import Exceptions.AlreadyConnectedException;
import Exceptions.IDNotFoundException;
import Utilities.Log;
import java.rmi.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  Chord Library
 */
public class Chord implements Remote
{

	/**
	 *  Creates a new Chord Ring
	 *  adds the node to the ring
	 *  adds the files of the node to the ring
	 *  sets successor of node, itself
	 */
	public static void create(Node node) throws AlreadyConnectedException, IDNotFoundException
	{
		try
		{
			if (node.isConnected())
			{
				throw new AlreadyConnectedException("Node Already connected");
			}
			else if (node.getLocalID() == null)
			{
				throw new IDNotFoundException("Unable to create ring, IDKey not defined");
			}

			node.setConnected(true);
			node.addSuccessor(node.getLocalID(), 0);
			node.setPredecessor(null);
		}
		catch (RemoteException e)
		{
			Log.addMessage("A problem occurred when creating the ring. Please try again later!", Log.ERROR);            //maybe something to renew registry???
		}
	}

	/**
	 * method called by a node that is possible predecessor to the current node
	 * checks whether predecessor of node is not set, or if the possible predecessor
	 * is closer to the current node than the current predecessor
	 * and updates(if needed) the predecessor variable
	 */
	public static void notifyNode(Node self, Node possiblePredecessor) throws RemoteException
	{
		/*
		 *checks if current predecessor is not set(null)
		 * and if possiblePredecessor's ID lies between (currentPredecessor's ID, currentNode's ID
		 */
		if (self.getPredecessor() == null || Key.isBetweenNotify(possiblePredecessor.getLocalID(), self.getPredecessor(), self.getLocalID())) // updates the local variable of current Node
		{
			self.setPredecessor(possiblePredecessor.getLocalID());
		}
	}

	/**
	 * connects node self to the chord ring that contains node other
	 * sets predecessor null and asks other to find its successor
	 */
	public static void join(Node self, Node other) throws RemoteException, MalformedURLException,NullPointerException
	{
		System.out.println("chord join");

		self.setPredecessor(null);

		IdKey successorID = null;

		try
		{
			successorID = other.find_successor_ID(self.getLocalID());
		}
		catch (RemoteException ex)
		{
			Log.addMessage("join-Rexc[0]: A problem occurred while attempting to find my successor. Try connecting later", Log.ERROR);
		}
		self.addSuccessor(successorID, 0);
		if (self.getFingerSize() > 1)
		{
			self.clearFingers();
		}
		self.setFinger(successorID, 0);

		try
		{
			//Node n = (Node) other.getRMIHandle().lookup(String.valueOf(successorID.getPID()));
			//n.setPredecessor(node.getLocalID());
			Node n = (Node) Naming.lookup("/" + other.getLocalID().getIP() + ":1099/" + String.valueOf(successorID.getPID()));
			Chord.notifyNode(self, n);
		}
		catch (NotBoundException ex)
		{
			Log.addMessage("Node's  successor is not bound or was unbound." , Log.ERROR);
		}
	}

	/**
	 * assigns node to find the successor Node of id
	 */
	public static Node find_successor(Node node, Key id) throws RemoteException, MalformedURLException ,NullPointerException
	{
		long startTime = System.currentTimeMillis();

		System.out.println("chord find_successor");
		Node tempNode = find_predecessor(node, id);
		System.out.println("Predecessor is :  " + tempNode.getLocalID().hashKeytoHexString());

		IdKey successorsID = tempNode.getImmediateSuccessor();
		java.rmi.registry.Registry remote = java.rmi.registry.LocateRegistry.getRegistry("rmi:/" + node.getLocalID().getIP());
		try
		{
			node.addTime(System.currentTimeMillis()-startTime);
			// returns the node that is successor of the id
			return (Node) Naming.lookup("/" + successorsID.getIP() + ":1099/" + String.valueOf(successorsID.getPID()));
		}
		catch (RemoteException ex)
		{
			Log.addMessage("find_successor-Rexc[0]: Remote Exception from Find Successor", Log.ERROR);
		}
		catch (NotBoundException ex)
		{
			Log.addMessage("find_successor-NBExc[0]: A problem occurred when returning the successor for: " + id.toString() + "\n" + successorsID.toString() + " is not bound or was unbound."
			               + "Try connecting later", Log.ERROR);
		}

		//something weird happened
		return null;
	}

	/**
	 * searches for the predecessor of the id
	 */
	public static Node find_predecessor(Node node, Key id) throws RemoteException, MalformedURLException
	{

		Node successor = null;
		Node startNode = node;

		Registry remote = LocateRegistry.getRegistry("rmi:/" + node.getLocalID().getIP() + ":1099/");
		System.out.println(node.getLocalID().getIP());
		try
		{
			IdKey succID = node.getImmediateSuccessor();
			try
			{
				// sets at the local variable successor, successor's node(from the rmiRegistry)
				successor = (Node) Naming.lookup("/" + succID.getIP() + ":1099/" +String.valueOf(succID.getPID()));
			}
			catch (MalformedURLException ex)
			{
				Log.addMessage("MalformedURLException", Log.ERROR);
			}
		}
		catch (RemoteException ex)
		{
			Log.addMessage("find_predecessor-RExc[0]:A problem occurred while attempting to find  the appropriate predecessor. It will be corrected in the next repetition of the stabilize.", Log.ERROR);
			throw new NullPointerException();
			//return null;
		}
		catch (NotBoundException ex)
		{
			Log.addMessage("find_predecessor-NBexc[0]:A problem occurred when finding the appropriate predecessor for: " + id.toString() + "\n" + "The successor of " + node.getLocalID().toString() + " is not bound or was unbound. "
			               + "Try connecting later", Log.ERROR);
			throw new NullPointerException();
		}

		//holds the value of node at every repeatition
		Node checkNode = null;

		//holds the number of hops in the repeatition
		int hops = 0;

		try
		{
			// loop continues, until id is betwenn (tempNode, tempNode.successor)
			while (!Key.isBetweenSuccessor(id, node.getLocalID(), node.getImmediateSuccessor()))
			{
				if (!(checkNode == null))
				{
					if (checkNode.getLocalID().equals(node.getLocalID()))
					{
						break;
					}
				}
				hops++;
				checkNode = node;
				//refresh node variable

				node = (Node) closest_preceding_finger(node, id);
				//refresh successor variable

				try
				{
					IdKey key = node.getImmediateSuccessor();

					successor = (Node) Naming.lookup("/" + key.getIP() + ":1099/" + String.valueOf(node.getImmediateSuccessor().getPID()));
				}
				catch (RemoteException ex)
				{
					Log.addMessage("find_predecessor-RExc[1]: A problem occurred while attempting to find  the appropriate successor of predecessor. It will be corrected in the next repetition of the stabilize.", Log.ERROR);

				}
				catch (NotBoundException ex)
				{
					Log.addMessage("find_predecessor-NBExc[1]: A problem occurred when finding the appropriate predecessor for: " + id.toString() +
					               "\n" + "The successor of " + node.getLocalID().toString() + " is not bound or was unbound. "
					               + "Try connecting later", Log.ERROR);
				}
			}
		}
		catch (RemoteException rexc)
		{
			Log.addMessage("find_predecessor-RExc[2]: A problem occurred while attempting to find  the appropriate successor of predecessor. It will be corrected in the next repetition of the stabilize.", Log.ERROR);
			//return null;
			throw new NullPointerException();
		}
		startNode.addHops(hops);
		return node;
	}

	/**
	 * returns the closest preceding Node of id recorded in the node's FingerTable
	 */
	public static Node closest_preceding_finger(Node node, Key id) throws RemoteException
	{
		IdKey finger;

		//for i = fingertable.size-1 downto 0
		if (node.getFingerSize() == 0)
		{
			return node;
		}
		for (int i = (node.getFingerSize() - 1); i >= 0; i--)
		{
			finger = node.getFinger(i);

			if (finger == null)
			{
				continue;
			}

			//returns the first node that is between (node, id)
			if (Key.isBetween(finger, node.getLocalID(), id))
			{
				try
				{
					try
					{
						return (Node) Naming.lookup("/" + finger.getIP() + ":1099/" + String.valueOf(finger.getPID()));
					}
					catch (MalformedURLException ex)
					{
						Log.addMessage("MalformedURLException", Log.ERROR);
					}
					// java.rmi.registry.Registry remote = java.rmi.registry.LocateRegistry.getRegistry("rmi:/" + node.getLocalID().getIP());
					// return (Node) remote.lookup(String.valueOf(finger.getPID()));
				}
				catch (RemoteException ex)
				{
					Log.addMessage("closest_preceding_finger:Rexc[0]: A problem occurred while attempting to find the closest preceding finger of node: "
					               + node.getLocalID().toString() + "and id :" + id.toString() + " It will be corrected in on of the next repetitions of the fix fingers.", Log.ERROR);
				}
				catch (NotBoundException ex)
				{
					Log.addMessage("closest_preceding_finger-NBExc[0]: A problem occurred  while attempting to find the closest preceding finger. The finger:  is not bound or was unbound. Try connecting later.", Log.ERROR);
				}
			}

		}
		return node;
	}

	/**
	 * called when a node leaves the ring
	 * redistributes the keys that the leaving node is successor to its predecessor.
	 * sets predecessor's successor as the leaving node's successor
	 * sets node's successor's predecessor, as the node's predecessor
	 */
	public static void redistribute_keys(Node node) throws RemoteException
	{
		NodeImpl thisNode = (NodeImpl) node;
		IdKey predecessor = thisNode.getPredecessor();
		IdKey successor = thisNode.getImmediateSuccessor();

		byte[] step = toByteArray(1);

		Node predecessorNode = null, successorNode = null;

		try //set predecessorNode
		{
			try
			{
				predecessorNode = (Node) Naming.lookup("/" + predecessor.getIP() + ":1099/" + String.valueOf(predecessor.getPID()));
			}
			catch (MalformedURLException ex)
			{
				Log.addMessage("Malformed URL in redistribute_keys, while looking up for predecessor", Log.ERROR);
			}
		}
		catch (ConnectException ce)
		{
			System.out.println("Connect Exception in redistribute_keys, while looking up for predecessor");
			Log.addMessage("Could not connect to predecessor in redistribute_keys", Log.ERROR);
		}
		catch (NotBoundException ex)
		{
			System.out.println("A problem occurred in redistribute_keys. Predecessor is not bound or was unbound");
			Log.addMessage("A problem occurred in redistribute_keys. Predecessor is not bound or was unbound", Log.ERROR);
		}
		catch (AccessException ex)
		{
			System.out.println("Access Exception in redistribute_keys, while looking up for predecessor");
			Log.addMessage("Could not access the predecessor in redistribute_keys.", Log.ERROR);
		}

		try //set successorNode
		{
			try
			{
				successorNode = (Node) Naming.lookup("/" + successor.getIP() + ":1099/" +String.valueOf(successor.getPID()));
			}
			catch (MalformedURLException ex)
			{
				Log.addMessage("Malformed URL in redistribute_keys, while looking up for successor", Log.ERROR);
			}
		}
		catch (ConnectException ce)
		{
			System.out.println("Connect Exception in redistribute_keys, while looking up for successor");
			Log.addMessage("Could not connect to successor in redistribute_keys", Log.ERROR);
		}
		catch (NotBoundException ex)
		{
			System.out.println("A problem occurred in redistribute_keys. Successor is not bound or was unbound");
			Log.addMessage("A problem occurred in redistribute_keys. Successor is not bound or was unbound", Log.ERROR);
		}
		catch (AccessException ex)
		{
			System.out.println("Access Exception in redistribute_keys, while looking up for successor");
			Log.addMessage("Could not access the successor in redistribute_keys.", Log.ERROR);
		}

		/*
		 * for every IdKey in the successor, pass them to successor
		 */
		Map<IdKey, Set<FIDEntry>> entries = node.getAllEntries();

		for (IdKey counter : entries.keySet())
		{
			successorNode.addEntries(counter, entries.get(counter));
		}

		successorNode.setPredecessor(predecessor);//successor's predecessor = node's predecessor
		predecessorNode.addSuccessor(successor, 0); // add successor to the sucList
		predecessorNode.removeSuccessor(1);//remove Node from the sucList

	}

	/**
	 * converts number's value to byteArray
	 */
	public static byte[] toByteArray(int number)
	{
		Integer num = (Integer) number;
		byte[] tempArray = new byte[1];
		tempArray[0] = num.byteValue();
		return tempArray;
	}

	public static void disconnect(Node node) throws RemoteException
	{
		Node predecessor = null, successor = null;
		try
		{
			try
			{
				predecessor = (Node) Naming.lookup("/" + node.getPredecessor().getIP() + ":1099/" + String.valueOf(node.getPredecessor().getPID()));
			}
			catch (MalformedURLException ex)
			{
				Log.addMessage("Malformed URL in disconnect, while looking up for predecessor.", Log.ERROR);
			}
		}
		catch (ConnectException ce)
		{
			System.out.println("Connect Exception in disconnect, while looking up for predecessor");
			Log.addMessage("Could not connect to predecessor in disconnect", Log.ERROR);
		}
		catch (NotBoundException ex)
		{
			System.out.println("A problem occurred in disconnect. Predecessor is not bound or was unbound");
			Log.addMessage("A problem occurred in disconnect. Predecessor is not bound or was unbound", Log.ERROR);
		}
		catch (AccessException ex)
		{
			System.out.println("Access Exception in disconnect, while looking up for predecessor");
			Log.addMessage("Could not access the predecessor in disconnect.", Log.ERROR);
		}

		try
		{
			try
			{
				successor = (Node) Naming.lookup("/" + node.getImmediateSuccessor().getIP() + ":1099/" + String.valueOf(node.getImmediateSuccessor().getPID()));
			}
			catch (MalformedURLException ex)
			{
				Log.addMessage("Malformed URL in disconnect, while looking up for successor", Log.ERROR);
			}
		}
		catch (ConnectException ce)
		{
			System.out.println("Connect Exception in disconnect, while looking up for successor");
			Log.addMessage("Could not connect to successor in disconnect", Log.ERROR);
		}
		catch (NotBoundException ex)
		{
			System.out.println("A problem occurred in disconnect. Successor is not bound or was unbound");
			Log.addMessage("A problem occurred in disconnect. Successor is not bound or was unbound", Log.ERROR);
		}
		catch (AccessException ex)
		{
			System.out.println("Access Exception in disconnect, while looking up for successor");
			Log.addMessage("Could not access the successor in disconnect.", Log.ERROR);
		}

		successor.setPredecessor(predecessor.getLocalID());
		predecessor.removeSuccessor(0);
		predecessor.addSuccessor(successor.getLocalID(), 0);

		System.out.println("Exiting node " + node.getLocalID().hashKeytoHexString());
		System.out.println("New successor of " + predecessor.getLocalID().hashKeytoHexString() + " is " + predecessor.getImmediateSuccessor().hashKeytoHexString());
		System.out.println("New predecessor of " + successor.getLocalID().hashKeytoHexString() + " is " + successor.getPredecessor().hashKeytoHexString());

		//delete duplicate
		predecessor.removeSuccessor(1);

		Chord.redistribute_keys(node);

		//NOW!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		try
		{
			Thread.sleep(5000);
		}
		catch (InterruptedException ex)
		{
			Logger.getLogger(Chord.class.getName()).log(Level.SEVERE, null, ex);
		}
		//END!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		try
		{
			Registry local  = LocateRegistry.getRegistry();
			local.unbind(node.getLocalID().getPID() +"");
		}
		catch (NotBoundException ex)
		{
			System.out.println("Node is not bound or was unbound. Message from the beyond...");
			Log.addMessage("Node is not bound or was unbound. Message from the beyond...", Log.ERROR);

		}
		catch (AccessException ex)
		{
			System.out.println("Node cannot access. Node's a slave");
			Log.addMessage("Node cannot access. Node's a slave", Log.ERROR);
		}
		finally
		{
			System.out.println("D.O.A.");
			System.exit(0);
		}
	}
}