package Chord;

import Application.Node;
import DHash.IdKey;
import DHash.Key;
import Utilities.Log;
import java.net.MalformedURLException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * runs periodically, checks who's the immediate successor
 * and checks notifies him
 */
public class Stabilize implements Runnable
{

	private final Node node;

	public Stabilize(Node node)
	{
		this.node = (Node) node;
	}

	public void run()
	{

		Random r = new Random();
		boolean downRmi = false;
		boolean downFRmi = false;
		boolean downSuc = false;
		Registry reg = null;

		try
		{
			reg = LocateRegistry.createRegistry(1099);
			downRmi = true;
			Log.addMessage("New Registry", Log.WARNING);
		}
		catch (ExportException ee)
		{
			downRmi = false;
		}
		catch (RemoteException ee)
		{
			Log.addMessage("A problem occurred. Continue", Log.WARNING);
			downRmi = false;
		}

		if (downRmi)
		{

			try
			{
				r.setSeed(node.getLocalID().getPID());
			}
			catch (RemoteException ex)
			{
				Log.addMessage("What the hell ? I am dead :(", Log.ERROR);
			}

			int time = r.nextInt(5000);

			try
			{
				Thread.sleep(time);
			}
			catch (InterruptedException ex1)
			{
				Log.addMessage("Interrupt exception. Continue", Log.WARNING);
			}

			try
			{

				//----------------------------------//
				List<IdKey> sucList = node.getWholeSucList();
				List<IdKey> fingList = node.getFingerKeys();
				IdKey pred = node.getPredecessor();
				IdKey foundKey = null;
				//-----------------------------------//

				// BIND
				if (reg != null)
				{
					Log.addMessage("Creating rmi ", Log.WARNING);

					try
					{
						reg.bind(String.valueOf(node.getLocalID().getPID()), node);
						reg.bind("starting", node);
						node.clearSuccessorList();
						node.addSuccessor(node.getLocalID(), 0);
						node.setPredecessor(node.getLocalID());

					}
					catch (AlreadyBoundException ex1)
					{
						Log.addMessage("I am already bound.", Log.WARNING);
					}
				}
				Log.addMessage("------CONNECTED-------", Log.ERROR);

				//----------------------------------//
				String rIp = "";
				int rPid = -1;
				boolean found = false;

				for (int i = 0; i < sucList.size(); i++)
				{
					rIp = sucList.get(i).getIP();

					rPid = sucList.get(i).getPID();

					if (!rIp.equals(node.getLocalID().getIP()))
					{
						found = true;
						foundKey = sucList.get(i);
						break;
					}
				}
				if (!found)
				{
					for (int i = 0; i < fingList.size(); i++)
					{
						rIp = fingList.get(i).getIP();

						rPid = fingList.get(i).getPID();

						if (!rIp.equals(node.getLocalID().getIP()))
						{
							found = true;
							foundKey = sucList.get(i);
							break;
						}
					}
				}
				if (!found)
				{
					rIp = pred.getIP();
					rPid = pred.getPID();
					if (!rIp.equals(node.getLocalID().getIP()))
					{
						foundKey = pred;
						found = true;
					}
				}
				if (found)
				{
					Node starting;
					Log.addMessage("Joining => " + rIp + " => " + rPid, Log.WARNING);
					try
					{
						starting = (Node) Naming.lookup("/" + rIp + ":1099/" + String.valueOf(rPid));
						try
						{
							node.join(starting);
						}
						catch (NullPointerException nex)
						{
							Log.addMessage("Successor is dead. Get successor[1]", Log.WARNING);
							node.addSuccessor(foundKey, 0);
						}
					}
					catch (NotBoundException ex)
					{
						Log.addMessage("Starting node does not exist. Continue", Log.WARNING);
					}
					catch (MalformedURLException ex)
					{
						Logger.getLogger(Stabilize.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
				//------------------------------------------------------//
			}
			catch (RemoteException ex)
			{
				Log.addMessage("Oh my God.Registry is alive :D", Log.WARNING);
				downRmi = false;
			}
		}


		if (!downRmi)
		{

			Registry rmiReg = null;
			try
			{
				rmiReg = LocateRegistry.getRegistry(1099);
				rmiReg.lookup(String.valueOf(node.getLocalID().getPID()));
			}
			catch (NotBoundException ntb)
			{
				joinNode(rmiReg, r);
			}
			catch (RemoteException ex)
			{
				joinNode(rmiReg, r);
			}

			try
			{
				System.out.println("\nStabilize called at: " + node.getLocalID().toString());
			}
			catch (RemoteException ex)
			{
				Log.addMessage("What the hell ? I am dead :(", Log.ERROR);
			}


			Node successor = null;
			IdKey succID = null;
			try
			{
				succID = node.getImmediateSuccessor();
				//get the Node that is the successor of node
				try
				{
					//get the Node that is the successor of node
					successor = (Node) Naming.lookup("/" + succID.getIP() + ":1099/" + String.valueOf(succID.getPID()));
				}
				catch (MalformedURLException ex)
				{
					Log.addMessage("MalformedURLException in stabilize", Log.ERROR);
				}
				catch (RemoteException ex)
				{
					Log.addMessage("Successor is down. Try repairing..." , Log.WARNING);

					String host = succID.getIP();
					downSuc = true;

					try
					{
						reg = LocateRegistry.getRegistry("rmi:/" + host);
						downFRmi = false;
					}
					catch (RemoteException rexc)
					{
						downFRmi = true;
					}

				}
				catch (NotBoundException ex)
				{
					Log.addMessage("Successor is not bound or was unbound. Stabilize will be executed again later"
					               + "(maybe should put as successor self, or call find_successor?)", Log.WARNING);
					downSuc = true;
				}
			}
			catch (RemoteException rexc)
			{
				Log.addMessage("Successor is down. Try repairing...", Log.WARNING);
			}



			if (downSuc)
			{

				Log.addMessage("Successor has died in a horrific accident. No worries", Log.WARNING);
				try
				{
					IdKey idSuc = node.getSucList(0);
					Naming.lookup("/" + idSuc.getIP() + ":1099/" + String.valueOf(idSuc.getPID()));
				}
				catch (NotBoundException  nbex)
				{
					updateSuc();
				}
				catch (RemoteException rexc)
				{
					updateSuc();
				}
				catch (MalformedURLException urlExc)
				{
					Log.addMessage("MalformedURLException in stabilize", Log.ERROR);
				}


			}
			else
			{
				if (!downFRmi)
				{
					//get successor's predecessor
					IdKey temp = null;

					try
					{
						temp = successor.getPredecessor();

					}
					catch (java.rmi.ConnectException ce)
					{
						Log.addMessage("Could not connect to successor. He might have died while execution was in process...", Log.WARNING);
					}
					catch (RemoteException ex)
					{
						Log.addMessage("A problem occurred during Stabilize. It will be corrected in the next repetition of the thread", Log.WARNING);
					}

					System.out.println("---------NEW0");
					//NEW!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
					Node newSuccessor = null;
					IdKey newSuccID = null;

					try
					{
						newSuccID = successor.getPredecessor();

						if (newSuccID != null)
						{
							try
							{
								try
								{
									newSuccessor = (Node) Naming.lookup("/" + newSuccID.getIP() + ":1099/" + String.valueOf(newSuccID.getPID()));
								}
								catch (MalformedURLException ex)
								{
									Log.addMessage("MalformedURLException in stabilize", Log.ERROR);
								}
							}
							catch (RemoteException ex)
							{
								Log.addMessage("Predecessor of successor is down.Repairing...", Log.WARNING);
							}
							catch (NotBoundException ex)
							{
								Log.addMessage("Predecessor of successor is not bound or was unbound.Repairing...", Log.WARNING);
							}
						}
					}
					catch (java.rmi.ConnectException ce)
					{
						Log.addMessage("Successor is down. Repairing...", Log.WARNING);
					}
					catch (RemoteException ex)
					{
						Log.addMessage("RemoteException in stabilize", Log.ERROR);
					}

					System.out.println("---------NEW1");
					///NEW!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

					try
					{
						System.out.println("successor ID: " + node.getImmediateSuccessor().toString());

						if (temp != null)
						{
							System.out.println("successor's predecessor is: " + temp.toString());

							//check if temp is between (n, successor)
							if (!node.getLocalID().equals(successor.getLocalID()))
							{
								if (Key.isBetween(temp, node.getLocalID(), successor.getLocalID()))
								{
									node.addSuccessor(temp, 0);

									if (node.getFingerSize() != 0)
									{
										node.removeFinger(0);
									}
									node.setFinger(temp, 0);

									if (newSuccessor != null)
									{
										newSuccessor.setEntries(successor.giveEntries(temp));
									}
								}
							}
							else
							{

								if (node.getFingerSize() != 0)
								{
									node.removeFinger(0);
								}

								node.setFinger(temp, 0);
								node.addSuccessor(temp, 0);

								if (newSuccessor != null)
								{
									System.out.println(newSuccessor.getLocalID().toString());
									newSuccessor.setEntries(successor.giveEntries(temp));
								}
							}
						}
						else
						{
							Log.addMessage("Node " + successor.getLocalID().toString() + " doesn't have a predecessor yet"
							               + "\n wait until stabilize is called from it", Log.WARNING);
						}

					}
					catch (java.rmi.ConnectException ce)
					{
						Log.addMessage("Successor has left the building. Repairing through stabilize", Log.WARNING);
					}
					catch (RemoteException ex)
					{
						Log.addMessage("RemoteException in stabilize", Log.ERROR);
					}

					try
					{
						//notify node
						if (successor != null)
						{
							successor.notifyNode(node);
						}

					}
					catch (java.rmi.ConnectException ce)
					{
						Log.addMessage("Successor has died in a horrific accident. No worries", Log.WARNING);

						updateSuc();
					}
					catch (RemoteException ex)
					{
						Log.addMessage("A problem occurred in Stabilize-notify. Try again later...", Log.WARNING);
					}
				}
			}
		}
		System.out.println("---------Stabilize end!");
	}

	private void updateSuc()
	{
		try
		{
			node.removeSuccessor(0);

			if (node.getSucListSize() == 0)
			{
				node.addSuccessor(node.getLocalID(), 0);

				if (node.getImmediateSuccessor().getHashKey().compareTo(node.getLocalID().getHashKey()) == 0)
				{
					node.setPredecessor(node.getLocalID());
				}
			}
			else
			{
				Node newSuc;
				try
				{

					newSuc = (Node) Naming.lookup("/" + node.getImmediateSuccessor().getIP() + ":1099/" + String.valueOf(node.getImmediateSuccessor().getPID()));
					newSuc.setPredecessor(node.getLocalID());

				}
				catch (NotBoundException ex)
				{
					Log.addMessage("New successor is not bound or was unbound. Continue !!!", Log.WARNING);
				}
				catch (MalformedURLException ex)
				{
					Log.addMessage("MalformedURLException in stabilize", Log.ERROR);
				}
			}
		}
		catch (RemoteException ex)
		{
			Log.addMessage("Your computer will explode in 5... 4 ... 3... 2... 1...Boom (?)", Log.ERROR);
		}
	}

	private void joinNode(Registry rmiReg, Random r)
	{
		Log.addMessage("---------------------1st", Log.WARNING);

		if (rmiReg != null)
		{
			r.setSeed(System.currentTimeMillis());
			int time = r.nextInt(5000);

			try
			{
				Thread.sleep(time);
			}
			catch (InterruptedException ex)
			{
				Log.addMessage("Interrupt exception. Continue", Log.WARNING);
			}

			try
			{
				rmiReg.bind(String.valueOf(node.getLocalID().getPID()), node);

				try
				{

					Node starting;
					starting = (Node) rmiReg.lookup("starting");

					try
					{
						node.join(starting);
					}
					catch (NullPointerException nexc) {}

				}
				catch (NotBoundException ex)
				{

					r.setSeed(System.currentTimeMillis());
					time = r.nextInt(10000);

					if (time < 3000)
					{
						time += 3000;
					}

					try
					{
						Thread.sleep(time);
					}
					catch (InterruptedException ex1)
					{
						Logger.getLogger(Stabilize.class.getName()).log(Level.SEVERE, null, ex1);
					}

					Node starting;

					try
					{
						starting = (Node) rmiReg.lookup("starting");
						Log.addMessage("Join", Log.WARNING);
						try
						{
							node.join(starting);
						}
						catch (NullPointerException nexce) {}
						Log.addMessage("Joined", Log.WARNING);

					}
					catch (NotBoundException ex1)
					{
						Log.addMessage("I am gonna die :(", Log.ERROR);
					}
				}

			}
			catch (AlreadyBoundException ex)
			{
				Log.addMessage("Already Bound. Continue", Log.WARNING);
			}
			catch (AccessException ex)
			{
				Log.addMessage("You don't have the permissions to perform this action :(", Log.ERROR);
			}
			catch (RemoteException re)
			{
				Log.addMessage("Remote exception. Continue", Log.ERROR);
			}

		}
	}
}
