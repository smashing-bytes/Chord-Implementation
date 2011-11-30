package Chord;

import Application.Node;
import Application.NodeImpl;
import DHash.BigIntImpl;
import java.rmi.RemoteException;
import DHash.IdKey;
import Utilities.Log;

/**
 * thread that runs periodically and fixes a random finger of the finger table
 */
public class FixFingers implements Runnable
{
	private final Node node;
	//Random random = new Random();
	int nextFinger;

	public FixFingers(Node node)
	{
		this.node = node;
		nextFinger = 0;
	}

	public void run()
	{

		try
		{
			System.out.println("\nFix Fingers called at: " + node.getLocalID().toString());
		}
		catch (RemoteException ex)
		{
			Log.addMessage("A problem occurred in Fix Fingers. Try again later" , Log.ERROR);
		}

		nextFinger += 1;

		if (nextFinger > 160)
			nextFinger = 1;

		/*
		 * get the IdKey of the node that is successor of the id
		 * (node's ID + 2^(nextFinger-1))
		 */
		//IdKey Finger = node.find_successor_ID(new IdKey(node.getLocalID().getHashKey().addition(Chord.toByteArray(2 ^ (nextFinger - 1))), 0, null));
		IdKey Finger = null;


		try
		{


			byte[] array = node.getLocalID().getHashKey().addpowerOfTwo(nextFinger - 1).modM(160);

			IdKey temp = new IdKey(new BigIntImpl(array), node.getLocalID().getPID(), node.getLocalID().getIP());
			System.out.println("temp = " + temp.hashKeytoHexString());

			if (temp.equals(node.getLocalID()))
				return;

			Finger = node.find_successor_ID(temp);
		}
		catch(java.rmi.ConnectException ce)
		{
			Log.addMessage("FixFingers: Could not connect with node... Message from the beyond:P" , Log.ERROR);
		}
		catch (RemoteException ex)
		{
			Log.addMessage("A problem occurred in Fix Fingers. Try again later..." , Log.ERROR);
		}
		catch (NullPointerException nexc)
		{
			((NodeImpl)node).clearFingers();
			Finger = null;
		}
		try
		{
			//set it as the new IdKey in the fingerTable
			System.out.println("START");
			//if (!Finger.equals(node.getImmediateSuccessor()))
			//  node.setFinger(Finger, 0);
			//else
			if (Finger != null)
			{

				node.setFinger(Finger, nextFinger - 1);

			}
			System.out.println("END");
		}
		catch (RemoteException ex)
		{
			Log.addMessage("A problem occurred in Fix Fingers. Try again later..." , Log.ERROR);
		}
		if (Finger != null)
			System.out.println("Finger: " + Finger.toString() + " => " + (nextFinger - 1));
		else
			nextFinger = 0;
	}
}