package Application;

/*Distributed Systems Chord Implementation over LAN*/
/*@author Haris Fokas*/
import Exceptions.AlreadyConnectedException;
import Exceptions.IDNotFoundException;
import Utilities.networking.*;
import FileServices.Share;
import FileServices.ServerThread;
import java.rmi.*;
import java.rmi.registry.*;
import DHash.*;
import FileServices.FileManager;
import Utilities.Log;
import Utilities.MainFrame;
import Utilities.SettingsDialog;
import java.net.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import Utilities.networking.PeerDiscoveryServer;
import Utilities.networking.LocateBootstrapNode;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class NodeClient
{

	static protected Registry rmiReg;
	static NodeImpl thisNode;

	public static void main(String[] argv) throws IOException, InterruptedException
	{

		/*Basic node info declaration*/
		InetAddress lanIP;
		rmiReg = null; //Registry handle
		String runtimeID;
		int processID, FSPort;
		Share share = null;
		NodeProperties myProperties;

		/*Threading declaration*/
		Thread fileServer;

		/*Store processID*/
		runtimeID = ManagementFactory.getRuntimeMXBean().getName();
		System.out.println(runtimeID);
		/*To make it John's IDKey compliant :P*/
		processID = Integer.parseInt(runtimeID.substring(0, runtimeID.indexOf("@")));


		/*Get Own IP Address*/
		lanIP = IPTools.getIP();

		System.setProperty("java.rmi.server.hostname", lanIP.getHostAddress());

		thisNode = new NodeImpl( new IdKey(processID, lanIP.toString()));

		SettingsDialog settingsDialog = new SettingsDialog(new java.awt.Frame(), true);
		settingsDialog.setVisible(true);
		MainFrame mainFrame = new MainFrame(thisNode, 3000, 500);

		/*Get Settings from dialog*/
		/*Share files*/
		String folder = settingsDialog.getShareFolder();
		if (folder == null)
		{
			JOptionPane.showMessageDialog(new JFrame(), "A problem occurred while loading a shared folder.Application is exiting now", "ChordFPG said:", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}

		share = new Share(folder);

		// Lame implementation - To be reviewed
		Random r = new Random();
		r.setSeed(System.currentTimeMillis());

		FSPort = r.nextInt(16383) + 49152;
		fileServer = new Thread(new ServerThread(share, FSPort));

		while (true)
		{

			try
			{
				fileServer.start();
				break;
			}
			catch (NullPointerException nExc)
			{
				r.setSeed(System.currentTimeMillis());
				FSPort = r.nextInt(16383) + 49152;
				fileServer = new Thread(new ServerThread(share, FSPort));
			}

		}

		/*Node creation*/
		myProperties = new NodeProperties(lanIP, FSPort, processID, thisNode.getLocalID().toString());
		thisNode.setProperties(myProperties);


		Log.addMessage("Process ID: " + processID, Log.INFORMATION);
		Log.addMessage("Workstation IP: " + lanIP.toString(), Log.INFORMATION);


		/*Create RMI Registry*/
		try
		{
			rmiReg = LocateRegistry.createRegistry(1099);
			Log.addMessage("RMI registry ready", Log.INFORMATION);
			/*RMIServer rmi = new RMIServer();
			Thread ser = new Thread(rmi);
			ser.start();*/
		}
		catch (java.rmi.server.ExportException ee)
		{
			rmiReg = LocateRegistry.getRegistry();
			Log.addMessage("Registered on local rmi server", Log.INFORMATION);
		}




		NodeProperties bootstrapNode = LocateBootstrapNode.locate();





		if (bootstrapNode == null) /*This is the first node*/
		{
			try
			{

				/*Bind the service to the registry*/
				//rmiReg.bind(String.valueOf(myProperties.getPid()), thisNode);
				Naming.bind(String.valueOf(myProperties.getPid()), thisNode);
			}
			catch (RemoteException ex)
			{
				Logger.getLogger(NodeClient.class.getName()).log(Level.SEVERE, null, ex);
			}
			catch (AlreadyBoundException ex)
			{
				Logger.getLogger(NodeClient.class.getName()).log(Level.SEVERE, null, ex);
			}

			try
			{
				thisNode.create();
			}
			catch (AlreadyConnectedException ex)
			{
				System.out.println("AlreadyConnectedException");
			}
			catch (IDNotFoundException ex)
			{
				System.out.println("IDNotFoundException");
			}

			/*Start peer discovery server*/

			PeerDiscoveryServer peer = new PeerDiscoveryServer(myProperties);
			Thread peerServer = new Thread(peer);
			peerServer.start();



		}
		else /*Existing node to join*/
		{

			Registry remoteReg = LocateRegistry.getRegistry(bootstrapNode.getNodeIP().getHostName());

			/*Testing!*/
			for (String obj : remoteReg.list())
			{
				System.out.println(obj);
			}

			/*Get remote object from its registry*/
			Node startingNode = null;
			try
			{
				//startingNode = (Node) remoteReg.lookup(bootstrapNode.getBindName());
				startingNode = (Node) Naming.lookup("//" + bootstrapNode.getNodeIP().getHostAddress() + ":1099/" + bootstrapNode.getBindName());
			}
			catch (RemoteException ex)
			{
				Logger.getLogger(NodeClient.class.getName()).log(Level.SEVERE, null, ex);
			}
			catch (NotBoundException ex)
			{
				Logger.getLogger(NodeClient.class.getName()).log(Level.SEVERE, null, ex);
			}
			catch (Exception genE)
			{
				genE.printStackTrace();
			}

			try
			{
				System.out.println("---------------------__>" + "//" + lanIP.getHostAddress() + ":1099/" + myProperties.getPid());
				System.out.println(thisNode.getLocalID().getIP());
				Naming.bind("//" + lanIP.getHostAddress() + ":1099/" + myProperties.getPid(), thisNode);

			}
			catch (RemoteException ex)
			{
				Logger.getLogger(NodeClient.class.getName()).log(Level.SEVERE, null, ex);
			}
			catch (AlreadyBoundException ex)
			{
				Logger.getLogger(NodeClient.class.getName()).log(Level.SEVERE, null, ex);
			}

			try
			{
				thisNode.join(startingNode);
			}
			catch (NullPointerException nexc) {}

			/*Set up peer discovery server*/
			PeerDiscoveryServer peer = new PeerDiscoveryServer(myProperties);
			Thread peerServer = new Thread(peer);
			peerServer.start();
		}


		mainFrame.setSize(698, 465);
		mainFrame.setVisible(true);


		FileManager fm = new FileManager(share, thisNode);
		fm.distributeFiles();

		thisNode.setShare(share);

		while (true)
		{

			Thread.sleep(5000);

			Log.addMessage("IdKey : " + thisNode.getLocalID().hashKeytoHexString(), Log.INFORMATION);
			IdKey id;

			if (thisNode.getSucListSize() > 0)
			{
				id = thisNode.getSucList(0);
				if (id == null)
				{
					Log.addMessage("Successor : NULL", Log.WARNING);
				}
				else
				{
					Log.addMessage("Successor : " + id.hashKeytoHexString(), Log.INFORMATION);
				}
			}

			id = thisNode.getPredecessor();
			if (id == null)
			{
				Log.addMessage("Predecessor : NULL", Log.WARNING);
			}
			else
			{
				Log.addMessage("Predecessor : " + id.hashKeytoHexString(), Log.INFORMATION);
			}



			Log.addMessage("Successor List", Log.TABLES);
			for (IdKey temp1 : thisNode.getWholeSucList())
			{
				Log.addMessage(temp1.hashKeytoHexString(), Log.TABLES);
			}

			if (thisNode.getFingerSize() <= 0 || thisNode.getFingers() == null)
			{
				Log.addMessage("Finger List : NULL", Log.WARNING);
			}
			else
			{
				Log.addMessage("Finger List", Log.TABLES);

				List<FingerEntry> fingerList = thisNode.getFingerEntries();
				Iterator<FingerEntry> iter = fingerList.iterator();
				while (iter.hasNext())
				{
					FingerEntry t = iter.next();
					Log.addMessage(t.getKey().hashKeytoHexString() + " [2^" + t.getFirstElement() + " , 2^" + t.getLastElement() + "]", Log.TABLES);
				}
			}
		}

	}


}
